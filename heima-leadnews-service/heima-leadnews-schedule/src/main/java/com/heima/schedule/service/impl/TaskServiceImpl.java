package com.heima.schedule.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.heima.common.redis.CacheService;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.schedule.pojos.Taskinfo;
import com.heima.model.schedule.pojos.TaskinfoLogs;
import com.heima.schedule.mapper.TaskinfoLogsMapper;
import com.heima.schedule.mapper.TaskinfoMapper;
import com.heima.schedule.service.ITaskService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Set;

@Service
@Slf4j
public class TaskServiceImpl implements ITaskService {

    @Autowired
    private TaskinfoMapper taskinfoMapper;
    @Autowired
    private TaskinfoLogsMapper taskinfoLogsMapper;
    @Autowired
    private CacheService cacheService;
    @Autowired
    private KafkaTemplate<String, Object> kafkaTemplate;
    @Autowired
    private TaskServiceImpl taskServiceImpl;
    @Autowired
    private RedissonClient redissonClient;

    private static final String KEY_PREFIX = "FUTURE";

    @Override
    public Long addTask(Task task) {
        // 1.写数据库
        Taskinfo taskinfo = new Taskinfo();
        BeanUtils.copyProperties(task, taskinfo);
        taskinfoMapper.insert(taskinfo);

        task.setTaskId(taskinfo.getTaskId());

        TaskinfoLogs taskinfoLogs = new TaskinfoLogs();
        BeanUtils.copyProperties(taskinfo, taskinfoLogs);
        taskinfoLogs.setStatus(TaskinfoLogs.SCHEDULED);
        taskinfoLogs.setVersion(1);
        taskinfoLogsMapper.insert(taskinfoLogs);

        // 2.判断执行时间 <= 当前时间则立即执行，<= 预设时间则加入 zset
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long time = calendar.getTime().getTime();

        if (task.getExecuteTime().getTime() <= System.currentTimeMillis()) {
            taskServiceImpl.sendMessage(task);
        } else if (task.getExecuteTime().getTime() <= time) {
            String key = KEY_PREFIX + task.getTaskType() + "_" + task.getPriority();
            cacheService.zAdd(key, JSON.toJSONString(task), task.getExecuteTime().getTime());
        }
        return task.getTaskId();
    }

    @Scheduled(cron = "0 0/5 * * * ?")
    public void syncDB() {
        log.info("开始执行定时同步数据库中的任务到 zset 中");
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.MINUTE, 5);
        long time = calendar.getTime().getTime();
        List<Taskinfo> taskinfos = taskinfoMapper.selectList(Wrappers.<Taskinfo>lambdaQuery().le(Taskinfo::getExecuteTime, time));
        if (CollectionUtils.isEmpty(taskinfos)) {
            log.info("没有需要同步的任务");
        }

        for (Taskinfo taskinfo : taskinfos) {
            Task task = new Task();
            BeanUtils.copyProperties(taskinfo, task);
            String key = KEY_PREFIX + task.getTaskType() + "_" + task.getPriority();
            cacheService.zAdd(key, JSON.toJSONString(task), task.getExecuteTime().getTime());
        }
    }

//    @Scheduled(cron = "0/30 * * * * ?")
    public void refreshZset() {
        log.info("开始执行定时提交 zset 中的任务");

        //分布式锁保证集群部署时只有一台机器可以执行，防止重复执行
        RLock lock = redissonClient.getLock("refreshZset");
        if (lock.tryLock()) {
            log.error("未获取到锁");
            return;
        }

        Set<String> futureKeys = cacheService.scan(KEY_PREFIX + "*");
        if (CollectionUtils.isEmpty(futureKeys)) {
            log.info("没有任务队列");
            return;
        }
        for (String futureKey : futureKeys) {
            Set<String> tasks = cacheService.zRangeByScore(futureKey, 0, System.currentTimeMillis());
            if (CollectionUtils.isEmpty(tasks)) {
                continue;
            }
            for (String task : tasks) {
                taskServiceImpl.sendMessage(JSON.parseObject(task, Task.class));
            }
        }

        lock.unlock();
        log.info("结束执行定时提交 zset 中的任务");
    }

    @Transactional
    public void sendMessage(Task task) {
        //TODO
        kafkaTemplate.send("TASK_EXEC", JSON.toJSONString(task));

        taskinfoMapper.deleteById(task.getTaskId());

        TaskinfoLogs taskinfoLogs = taskinfoLogsMapper.selectById(task.getTaskId());
        taskinfoLogs.setStatus(TaskinfoLogs.EXECUTED);
        taskinfoLogsMapper.updateById(taskinfoLogs);

        String key = KEY_PREFIX + task.getTaskType() + "_" + task.getPriority();
        cacheService.zRemove(key, JSON.toJSONString(task));
    }
}

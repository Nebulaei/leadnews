package com.heima.wemedia.service.impl;

import com.heima.feign.schedule.IScheduleClient;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.schedule.dtos.Task;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.service.WmTaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

@Service
public class WmTaskServiceImpl implements WmTaskService {

    @Autowired
    private IScheduleClient scheduleClient;
    @Autowired
    private WmNewsMapper wmNewsMapper;

    @Override
    public Long addTask(WmNews news) {
        Task task = Task.builder()
                .taskType(200)
                .priority(1)
                .executeTime(news.getPublishTime())
                .parameters(news.getId() + "")
                .build();

        ResponseResult responseResult = scheduleClient.addTask(task);
        if (responseResult.getCode() == AppHttpCodeEnum.SUCCESS.getCode()) {
            return (Long) responseResult.getData();
        }
        return null;
    }

    @KafkaListener(topics = "TASK_EXEC")
    public void execTask(String message) {
        WmNews news = wmNewsMapper.selectById(Long.parseLong(message));

    }
}

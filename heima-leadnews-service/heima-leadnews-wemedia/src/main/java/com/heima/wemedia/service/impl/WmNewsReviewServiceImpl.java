package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.heima.common.aliyun.GreenImageScan;
import com.heima.common.aliyun.GreenTextScan;
import com.heima.feign.article.IArticleClient;
import com.heima.file.service.FileStorageService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.pojos.WmChannel;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmUser;
import com.heima.wemedia.mapper.WmChannelMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmUserMapper;
import com.heima.wemedia.service.WmNewsReviewService;
import com.heima.wemedia.service.WmTaskService;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WmNewsReviewServiceImpl implements WmNewsReviewService {

    @Autowired
    private GreenTextScan greenTextScan;
    @Autowired
    private GreenImageScan greenImageScan;
    @Autowired
    private WmNewsMapper wmNewsMapper;
    @Autowired
    private FileStorageService fileStorageService;
    @Qualifier("com.heima.feign.article.IArticleClient")
    @Autowired
    private IArticleClient articleClient;
    @Autowired
    private WmUserMapper wmUserMapper;
    @Autowired
    private WmChannelMapper wmChannelMapper;
    @Autowired
    private WmTaskService wmTaskService;

    @Override
    @Async("asyncPool")
    @GlobalTransactional
    public void review(WmNews news, List<String> contentImages) {
//        // 文本审核不通过直接返回
//        if (!textReview(news)) {
//            log.info("文本审核不通过或需要人工审核");
//            return;
//        }
//        // 图片审核不通过直接返回
//        if (!imageReview(news, contentImages)) {
//            log.info("图片审核不通过或需要人工审核");
//            return;
//        }
        // 审核成功，同步到 app 端
        if (news.getPublishTime() != null) {
            log.info("发布延时任务");
            wmTaskService.addTask(news);
        } else {
            reviewSuccess(news);
        }
        syncToApp(news);
    }

    public boolean textReview(WmNews news) {
        boolean flag = true;
        try {
            String text = getTextFromContent(news);
            Map map = greenTextScan.greeTextScan(text);
            System.out.println(map);
            if (map == null) {
                authReview(news);
                return flag = false;
            }

            flag = parseScan(news, map, flag);
        } catch (Exception e) {
            log.error("审核失败", e);
            authReview(news);
            flag = false;
        }
        return flag;
    }

    public boolean imageReview(WmNews news, List<String> contentImages) {
        boolean flag = true;
        try {
            List<String> images = new ArrayList<>(contentImages);
            String coverImagesStr = news.getImages();
            List<String> coverImages = Arrays.asList(coverImagesStr.split(","));
            images.addAll(coverImages);
            images = images.stream().distinct().collect(Collectors.toList());

            if (!CollectionUtils.isEmpty(images)) {
                return flag = true;
            }

            for (String image : images) {
                byte[] bytes = fileStorageService.downLoadFile(image);
                Map map = greenImageScan.imageScan(Arrays.asList(bytes));

                if (map == null) {
                    authReview(news);
                    return flag = false;
                }

                if (!(flag = parseScan(news, map, flag))) {
                    return flag;
                }
            }
        } catch (Exception e) {
            log.error("审核失败", e);
            authReview(news);
            flag = false;
        }
        return flag;
    }

    public String getTextFromContent(WmNews news) {
        StringBuilder text = new StringBuilder(news.getTitle() + "-");
        String content = news.getContent();
        List<Map> contentMap = JSONArray.parseArray(content, Map.class);
        for (Map map : contentMap) {
            if ("text".equals(map.get("type"))) {
                text.append(map.get("value"));
            }
        }
        return text.toString();
    }

    public boolean parseScan(WmNews news, Map map, boolean flag) {
        String suggestion = map.get("suggestion") + "";
        String label = map.get("label") + "";
        if ("block".equals(suggestion)) {
            reviewFail(news, label);
            flag = false;
        } else if ("review".equals(suggestion)) {
            authReview(news);
            flag = false;
        }
        return flag;
    }

    public void authReview(WmNews news) {
        news.setStatus(WmNews.Status.FAIL.getCode());
        wmNewsMapper.updateById(news);
    }

    public void reviewFail(WmNews news, String label) {
        news.setStatus(WmNews.Status.FAIL.getCode());
        news.setReason(label);
        wmNewsMapper.updateById(news);
    }

    private void reviewSuccess(WmNews news) {
        news.setStatus(WmNews.Status.SUCCESS.getCode());
        wmNewsMapper.updateById(news);
    }

    public void syncToApp(WmNews news) {
        ArticleDto articleDto = new ArticleDto();
        BeanUtils.copyProperties(news, articleDto);

        articleDto.setId(news.getArticleId());

        articleDto.setLayout(news.getType());

        articleDto.setAuthorId(news.getUserId());

        WmUser user = wmUserMapper.selectById(news.getUserId());
        articleDto.setAuthorName(user == null ? "" : user.getName());

        WmChannel channel = wmChannelMapper.selectById(news.getChannelId());
        articleDto.setChannelName(channel == null ? "" : channel.getName());

        articleDto.setCreatedTime(new Date());
        articleDto.setPublishTime(new Date());

        ResponseResult result = articleClient.save(articleDto);
        if (result.getCode() == AppHttpCodeEnum.SUCCESS.getCode()) {
            news.setStatus(WmNews.Status.PUBLISHED.getCode());
            news.setArticleId((Long) result.getData());
            wmNewsMapper.updateById(news);
        }
    }
}

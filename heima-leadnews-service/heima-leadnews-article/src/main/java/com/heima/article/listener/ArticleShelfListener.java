package com.heima.article.listener;

import com.alibaba.fastjson.JSONArray;
import com.heima.article.service.ApArticleConfigService;
import com.heima.common.constants.WemediaConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Map;

@Component
@Slf4j
public class ArticleShelfListener {

    @Autowired
    private ApArticleConfigService apArticleConfigService;

    @KafkaListener(topics = WemediaConstants.WM_NEWS_SHELF_TOPIC)
    public void onMessage(String message) {
        Map map = JSONArray.parseObject(message, Map.class);
        apArticleConfigService.updateByMap(map);
        log.info("article端文章配置修改，articleId={}",map.get("articleId"));
    }
}

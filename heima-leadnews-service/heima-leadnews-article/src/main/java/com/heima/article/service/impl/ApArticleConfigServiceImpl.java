package com.heima.article.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.service.ApArticleConfigService;
import com.heima.model.article.pojos.ApArticleConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class ApArticleConfigServiceImpl extends ServiceImpl<ApArticleConfigMapper, ApArticleConfig> implements ApArticleConfigService {

    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;

    @Override
    public void updateByMap(Map map) {
//        ApArticleConfig articleConfig = getOne(Wrappers.<ApArticleConfig>lambdaQuery().eq(ApArticleConfig::getArticleId, map.get("articleId")));
//        if (articleConfig != null) {
//            articleConfig.setIsDown((Integer) map.get("enable"));
//            updateById(articleConfig);
//        }

        int isDown = (Integer) map.get("enable");
        update(Wrappers.<ApArticleConfig>lambdaUpdate()
                .eq(ApArticleConfig::getArticleId, map.get("articleId"))
                .set(ApArticleConfig::getIsDown, isDown));
    }
}

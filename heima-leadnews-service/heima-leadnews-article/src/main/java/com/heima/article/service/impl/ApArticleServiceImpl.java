package com.heima.article.service.impl;

import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApArticleServiceImpl implements ApArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;

    public static final int DEFAULT_PAGE_SIZE = 10;
    public static final int MAX_PAGE_SIZE = 20;
    public static final int MIN_PAGE_SIZE = 1;

    @Override
    public ResponseResult load(ArticleHomeDto articleHomeDto, int loadType) {
        if (articleHomeDto.getSize() == null ||
            articleHomeDto.getSize() <= MIN_PAGE_SIZE ||
            articleHomeDto.getSize() > MAX_PAGE_SIZE) {
            articleHomeDto.setSize(DEFAULT_PAGE_SIZE);
        }

        if (articleHomeDto.getMinBeHotTime() == null) articleHomeDto.setMinBeHotTime(new Date());
        if (articleHomeDto.getMaxBeHotTime() == null) articleHomeDto.setMinBeHotTime(new Date());

        List<ApArticle> articles = apArticleMapper.loadArticles(articleHomeDto, loadType);

        return ResponseResult.okResult(articles);
    }
}

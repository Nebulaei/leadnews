package com.heima.article.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.article.mapper.ApArticleConfigMapper;
import com.heima.article.mapper.ApArticleContentMapper;
import com.heima.article.mapper.ApArticleMapper;
import com.heima.article.service.ApArticleService;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import com.heima.model.article.pojos.ApArticleConfig;
import com.heima.model.article.pojos.ApArticleContent;
import com.heima.model.common.dtos.ResponseResult;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;

@Service
public class ApArticleServiceImpl extends ServiceImpl<ApArticleMapper, ApArticle> implements ApArticleService {

    @Autowired
    private ApArticleMapper apArticleMapper;
    @Autowired
    private ApArticleConfigMapper apArticleConfigMapper;
    @Autowired
    private ApArticleContentMapper apArticleContentMapper;

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

    @Override
    public ResponseResult save(ArticleDto dto) {
        ApArticle article = new ApArticle();
        BeanUtils.copyProperties(dto, article);
        if (dto.getId() == null) {
            super.save(article);

            ApArticleConfig articleConfig = new ApArticleConfig();
            articleConfig.setArticleId(dto.getId());
            articleConfig.setIsForward(0);
            articleConfig.setIsDelete(0);
            articleConfig.setIsDown(0);
            articleConfig.setIsComment(0);
            apArticleConfigMapper.insert(articleConfig);

            ApArticleContent articleContent = new ApArticleContent();
            articleContent.setArticleId(article.getId());
            articleContent.setContent(dto.getContent());
            apArticleContentMapper.insert(articleContent);
        } else {
            updateById(article);

            ApArticleContent articleContent = new ApArticleContent();
            articleContent.setArticleId(article.getId());
            articleContent.setContent(dto.getContent());
            apArticleContentMapper.updateById(articleContent);
        }

        return ResponseResult.okResult(dto.getId());
    }
}

package com.heima.article.service;

import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApArticleService {

    ResponseResult load(ArticleHomeDto articleHomeDto, int loadType);

    // 同步文章，自媒体端调用
    ResponseResult save(ArticleDto dto);
}

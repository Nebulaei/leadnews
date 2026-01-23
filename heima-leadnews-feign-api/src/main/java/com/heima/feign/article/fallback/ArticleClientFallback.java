package com.heima.feign.article.fallback;

import com.heima.feign.article.IArticleClient;
import com.heima.model.article.dtos.ArticleDto;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import org.springframework.stereotype.Component;

@Component
public class ArticleClientFallback implements IArticleClient {
    // 降级逻辑
    @Override
    public ResponseResult save(ArticleDto dto) {
        return ResponseResult.errorResult(AppHttpCodeEnum.SERVER_ERROR);
    }
}

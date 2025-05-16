package com.heima.search.service;

import com.heima.model.search.dtos.UserSearchDto;
import com.heima.model.common.dtos.ResponseResult;

public interface ApArticleSearchService {

    /**
     ES文章分页搜索
     @return
     */
    ResponseResult search(UserSearchDto userSearchDto);
}
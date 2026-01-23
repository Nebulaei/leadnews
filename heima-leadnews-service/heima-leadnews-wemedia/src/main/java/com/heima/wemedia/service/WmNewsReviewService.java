package com.heima.wemedia.service;

import com.heima.model.wemedia.pojos.WmNews;

import java.util.List;

public interface WmNewsReviewService {

    void review(WmNews news, List<String> contentImages);
}

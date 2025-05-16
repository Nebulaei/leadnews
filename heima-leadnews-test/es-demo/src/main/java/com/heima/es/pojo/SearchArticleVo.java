package com.heima.es.pojo;

import lombok.Data;

import java.util.Date;

@Data
public class SearchArticleVo {

    private Long id;

    private String title;

    private Date publishTime;

    private Short layout;

    private String images;

    private Long authorId;

    private String authorName;

    private String staticUrl;

    private String content;

}

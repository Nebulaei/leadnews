package com.heima.model.article.dtos;

import lombok.Data;

import java.util.Date;

@Data
public class ArticleHomeDto {

    Date maxBeHotTime;

    Date minBeHotTime;

    Integer size;

    String tag;
}

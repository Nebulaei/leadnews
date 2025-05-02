package com.heima.article.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.heima.model.article.dtos.ArticleHomeDto;
import com.heima.model.article.pojos.ApArticle;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * <p>
 * 文章信息表，存储已发布的文章 Mapper 接口
 * </p>
 *
 * @author kanade
 */
@Mapper
public interface ApArticleMapper extends BaseMapper<ApArticle> {

    List<ApArticle> loadArticles(@Param("dto")ArticleHomeDto dto,
                                 @Param("loadType") int loadType);
}

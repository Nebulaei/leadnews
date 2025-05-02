package com.heima.model.article.pojos;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * <p>
 * APP已发布文章配置表
 * </p>
 *
 * @author kanade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("ap_article_config")
@ApiModel(value="ApArticleConfig", description="APP已发布文章配置表")
public class ApArticleConfig implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @ApiModelProperty(value = "文章ID")
    @TableField("article_id")
    private Long articleId;

    @ApiModelProperty(value = "是否可评论")
    @TableField("is_comment")
    private Integer isComment;

    @ApiModelProperty(value = "是否转发")
    @TableField("is_forward")
    private Integer isForward;

    @ApiModelProperty(value = "是否下架")
    @TableField("is_down")
    private Integer isDown;

    @ApiModelProperty(value = "是否已删除")
    @TableField("is_delete")
    private Integer isDelete;


}

package com.heima.model.wemedia.pojos;

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
import java.util.Date;

/**
 * <p>
 * 自媒体图文素材信息表
 * </p>
 *
 * @author kanade
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("wm_material")
@ApiModel(value="WmMaterial", description="自媒体图文素材信息表")
public class WmMaterial implements Serializable {

    private static final long serialVersionUID = 1L;

    @ApiModelProperty(value = "主键")
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;

    @ApiModelProperty(value = "自媒体用户ID")
    @TableField("user_id")
    private Integer userId;

    @ApiModelProperty(value = "图片地址")
    @TableField("url")
    private String url;

    @ApiModelProperty(value = "素材类型  0 图片  1 视频")
    @TableField("type")
    private Integer type;

    @ApiModelProperty(value = "是否收藏")
    @TableField("is_collection")
    private Integer isCollection;

    @ApiModelProperty(value = "创建时间")
    @TableField("created_time")
    private Date createdTime;
}

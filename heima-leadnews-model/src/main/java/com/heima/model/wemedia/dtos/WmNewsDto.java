package com.heima.model.wemedia.dtos;

import lombok.Data;

import javax.validation.constraints.NotBlank;
import java.util.Date;
import java.util.List;

@Data
public class WmNewsDto {

    private Integer id;
    /**
     * 标题
     */
    @NotBlank(message = "标题不能为空")
    private String title;
    /**
     * 频道id
     */
    @NotBlank(message = "标题不能为空")
    private Integer channelId;
    /**
     * 标签
     */
    @NotBlank(message = "标签不能为空")
    private String labels;
    /**
     * 发布时间
     */
    private Date publishTime;
    /**
     * 文章内容
     */
    @NotBlank(message = "文章内容不能为空")
    private String content;
    /**
     * 文章封面类型  0 无图 1 单图 3 多图 -1 自动
     */
    @NotBlank(message = "封面类型不能为空")
    private Short type;
    /**
     * 提交时间
     */
    private Date submitedTime;
    /**
     * 状态 提交为1  草稿为0
     */
    private Short status;

    /**
     * 封面图片列表 多张图以逗号隔开
     */
    private List<String> images;

    /**
     * 上下架 0 下架  1 上架
     */
    @NotBlank(message = "上下架状态不能为空")
    private Short enable;
}
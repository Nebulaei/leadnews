package com.heima.wemedia.service.impl;

import com.alibaba.fastjson.JSONArray;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.constants.WemediaConstants;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.wemedia.dtos.WmNewsDto;
import com.heima.model.wemedia.dtos.WmNewsPageReqDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.model.wemedia.pojos.WmNews;
import com.heima.model.wemedia.pojos.WmNewsMaterial;
import com.heima.utils.common.UserThreadLocal;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.mapper.WmNewsMapper;
import com.heima.wemedia.mapper.WmNewsMaterialMapper;
import com.heima.wemedia.service.WmNewsReviewService;
import com.heima.wemedia.service.WmNewsService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
public class WmNewsServiceImpl extends ServiceImpl<WmNewsMapper, WmNews> implements WmNewsService {

    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Autowired
    private WmNewsMaterialMapper wmNewsMaterialMapper;
    @Autowired
    private WmNewsReviewService wmNewsReviewService;

    @Override
    public ResponseResult findAll(WmNewsPageReqDto dto) {

        //检查参数
        if(dto == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        //分页参数检查
        dto.checkParam();
        //获取当前登录人的信息
        Integer userId = UserThreadLocal.getUserId();
        if(userId == null){
            return ResponseResult.errorResult(AppHttpCodeEnum.NEED_LOGIN);
        }

        int pageNum = dto.getPage();
        int pageSize = dto.getSize();

        IPage page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<WmNews> wrapper = new LambdaQueryWrapper<>();
        if(dto.getStatus() != null){
            wrapper.eq(WmNews::getStatus,dto.getStatus());
        }
        //频道精确查询
        if(dto.getChannelId() != null){
            wrapper.eq(WmNews::getChannelId,dto.getChannelId());
        }
        //时间范围查询
        if(dto.getBeginPubDate()!=null && dto.getEndPubDate()!=null){
            wrapper.between(WmNews::getPublishTime,dto.getBeginPubDate(),dto.getEndPubDate());
        }
        //关键字模糊查询
        if(StringUtils.isNotBlank(dto.getKeyword())){
            wrapper.like(WmNews::getTitle,dto.getKeyword());
        }
        wrapper.eq(WmNews::getUserId, UserThreadLocal.getUserId())
                .orderByDesc(WmNews::getCreatedTime);

        super.page(page, wrapper);
        ResponseResult result = new PageResponseResult(pageNum, pageSize, (int) page.getTotal());
        result.setData(page.getRecords());

        return result;
    }

    @Override
    public ResponseResult submitNews(WmNewsDto dto) {

        if (StringUtils.isEmpty(dto.getTitle()) || StringUtils.isEmpty(dto.getContent())) {
            return ResponseResult.errorResult(AppHttpCodeEnum.PARAM_INVALID);
        }
        // 保存文章
        List<String> contentImages = getImagesFromContent(dto);
        WmNews news = saveOrUpdate(dto, contentImages);
        // 保存为草稿时直接返回
        if (dto.getStatus() == WmNews.Status.NORMAL.getCode()) {
            return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
        }
        // 保存内容素材和文章的关联关系
        saveRelations(news.getId(), contentImages, WemediaConstants.REFERENCE_TYPE_CONTENT);
        // 保存封面素材和文章的关联关系
        String images = news.getImages();
        List<String> coverImages = images.isEmpty() ? new ArrayList<>()
                : Arrays.asList(images.split(","));
        saveRelations(news.getId(), coverImages, WemediaConstants.REFERENCE_TYPE_COVER);
        // 审核文章内容
        wmNewsReviewService.review(news, contentImages);

        return ResponseResult.okResult(AppHttpCodeEnum.SUCCESS);
    }

    public WmNews saveOrUpdate(WmNewsDto dto, List<String> contentImages) {

        WmNews news = new WmNews();
        // 获取封面图片
        List<String> coverImages = getCoverImages(dto, contentImages);
        String images = StringUtils.join(coverImages, ",");

        BeanUtils.copyProperties(dto, news);
        news.setUserId(UserThreadLocal.getUserId());
        news.setCreatedTime(new Date());
        news.setSubmitedTime(new Date());
        news.setImages(images);
        // 新增或修改
        if (dto.getId() == null) {
            save(news);
        } else {
            LambdaUpdateWrapper<WmNewsMaterial> wrapper = new LambdaUpdateWrapper<>();
            wrapper.eq(WmNewsMaterial::getNewsId, dto.getId());
            wmNewsMaterialMapper.delete(wrapper);
            updateById(news);
        }

        return news;
    }

    public List<String> getCoverImages(WmNewsDto dto, List<String> contentImages) {
        // 默认为前端传的图片
        List<String> images = dto.getImages();
        // 若为自动则手动处理
        if (dto.getType() == WemediaConstants.COVER_TYPE_AUTO) {
            int size = contentImages.size();
            if (size >= 3) {
                images = contentImages.stream().limit(3).collect(Collectors.toList());
                dto.setType(WemediaConstants.COVER_TYPE_MULTI);
            } else if (size >= 1) {
                images = contentImages.stream().limit(1).collect(Collectors.toList());
                dto.setType(WemediaConstants.COVER_TYPE_SINGLE);
            } else {
                dto.setType(WemediaConstants.COVER_TYPE_NONE);
            }
        }
        return images;
    }

    public List<String> getImagesFromContent(WmNewsDto dto) {
        // 获取文章中的图片素材
        List<String> images = new ArrayList<>();
        String content = dto.getContent();
        List<Map> contentMap = JSONArray.parseArray(content, Map.class);

        if (CollectionUtils.isEmpty(contentMap)) {
            return images;
        }

        for (Map map : contentMap) {
            String type = map.get("type") + "";
            if ("image".equals(type)) {
                images.add(map.get("value") + "");
            }
        }
        return images;
    }

    private void saveRelations(int newsId, List<String> images, short referenceType) {
        System.out.println(images);
        // 保存素材与文章的关联关系
        if (CollectionUtils.isEmpty(images) || images.isEmpty()) {
            log.info("没有图片");
            return;
        }
        LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();
        wrapper.in(WmMaterial::getUrl, images);

        List<WmMaterial> materialList = wmMaterialMapper.selectList(wrapper);
        List<Integer> materialIds = materialList.stream().map(WmMaterial::getId).collect(Collectors.toList());

        wmNewsMaterialMapper.saveRelations(materialIds, newsId, referenceType);
    }
}

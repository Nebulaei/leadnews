package com.heima.wemedia.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.file.constant.FileTypeEnum;
import com.heima.file.service.FileStorageService;
import com.heima.model.common.dtos.PageResponseResult;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.wemedia.dtos.WmMaterialDto;
import com.heima.model.wemedia.pojos.WmMaterial;
import com.heima.utils.common.UserThreadLocal;
import com.heima.wemedia.mapper.WmMaterialMapper;
import com.heima.wemedia.service.WmMaterialService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.UUID;

@Service
@Slf4j
public class WmMaterialServiceImpl extends ServiceImpl<WmMaterialMapper, WmMaterial> implements WmMaterialService {

    @Autowired
    private WmMaterialMapper wmMaterialMapper;
    @Autowired
    private FileStorageService fileStorageService;

    @Override
    public ResponseResult uploadPicture(MultipartFile file) {

        String url = "";
        try {
            String filename = file.getOriginalFilename();
            String prefix = UUID.randomUUID().toString();
            String postfix = filename.substring(filename.lastIndexOf("."));

            url = fileStorageService.uploadFile("", prefix + postfix, file.getInputStream(), FileTypeEnum.IMAGE);
        } catch (Exception e) {
            log.error(e.getMessage());
            throw new RuntimeException("上传用户异常");
        }

        WmMaterial wmMaterial = new WmMaterial();
        wmMaterial.setUrl(url);
        wmMaterial.setCreatedTime(new Date());
        wmMaterial.setType(0);
        wmMaterial.setIsCollection(0);
        wmMaterial.setUserId(UserThreadLocal.getUserId());

        save(wmMaterial);

        return ResponseResult.okResult(wmMaterial);
    }

    @Override
    public ResponseResult list(WmMaterialDto dto) {
        dto.checkParam();
        Integer pageNum = dto.getPage();
        Integer pageSize = dto.getSize();

        IPage page = new Page<>(pageNum, pageSize);

        LambdaQueryWrapper<WmMaterial> wrapper = new LambdaQueryWrapper<>();

        if (dto.getIsCollection() != null) {
            wrapper.eq(WmMaterial::getIsCollection, dto.getIsCollection());
        }
        wrapper.eq(WmMaterial::getUserId, UserThreadLocal.getUserId());

        super.page(page, wrapper);
        ResponseResult result = new PageResponseResult(pageNum, pageSize, (int) page.getTotal());
        result.setData(page.getRecords());

        return result;
    }
}

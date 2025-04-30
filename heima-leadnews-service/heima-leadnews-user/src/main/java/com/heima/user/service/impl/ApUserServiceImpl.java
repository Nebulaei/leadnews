package com.heima.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.heima.common.exception.CustomException;
import com.heima.model.common.dtos.ResponseResult;
import com.heima.model.common.enums.AppHttpCodeEnum;
import com.heima.model.user.dtos.LoginDto;
import com.heima.model.user.pojos.ApUser;
import com.heima.user.mapper.ApUserMapper;
import com.heima.user.service.IApUserService;
import com.heima.utils.common.AppJwtUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import java.util.HashMap;

@Service
public class ApUserServiceImpl extends ServiceImpl<ApUserMapper, ApUser> implements IApUserService {

    @Autowired
    private ApUserMapper apUserMapper;

    @Override
    public ResponseResult login(LoginDto loginDto) {
        String phone = loginDto.getPhone();
        String password = loginDto.getPassword();

        HashMap<String, Object> result = new HashMap<>();

        // 游客登录
        if (StringUtils.isEmpty(phone) || StringUtils.isEmpty(password)) {
            result.put("token", AppJwtUtil.getToken(0L));
            return ResponseResult.okResult(result);
        }

        LambdaQueryWrapper<ApUser> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ApUser::getPhone, phone);
        ApUser apUser = apUserMapper.selectOne(wrapper);
        if (apUser == null) {
            throw new CustomException(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }

        String key = password + apUser.getSalt();
        String keyMd5 = DigestUtils.md5DigestAsHex(key.getBytes());
        if (!keyMd5.equals(apUser.getPassword())) {
            throw new CustomException(AppHttpCodeEnum.AP_USER_DATA_NOT_EXIST);
        }

        apUser.setPassword(null);
        result.put("user", apUser);
        result.put("token", AppJwtUtil.getToken(apUser.getId().longValue()));

        return ResponseResult.okResult(result);
    }
}

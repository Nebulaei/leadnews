package com.heima.user.controller;

import com.heima.user.service.IApUserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * <p>
 * APP用户信息表 前端控制器
 * </p>
 *
 * @author kanade
 */
@Slf4j
@RestController
@RequestMapping("apUser")
public class ApUserController {

    @Autowired
    private IApUserService apUserService;
}

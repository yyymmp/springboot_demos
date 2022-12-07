package com.satoken.controller;

import cn.dev33.satoken.annotation.SaCheckLogin;
import cn.dev33.satoken.annotation.SaCheckRole;
import cn.dev33.satoken.annotation.SaIgnore;
import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/user/")
public class AnnoAuthController {
    // 登录校验：只有登录之后才能进入该方法
    @SaCheckLogin
    @RequestMapping("info")
    public String info() {
        return "查询用户信息";
    }

    // 角色校验：必须具有指定角色才能进入该方法
    @SaCheckRole("super-admin")
    @RequestMapping("add")
    public String add() {
        return "用户增加";
    }

    @SaIgnore
    @RequestMapping("getList")
    public SaResult getList() {
        // ...
        return SaResult.ok();
    }
}

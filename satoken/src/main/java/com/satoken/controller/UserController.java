package com.satoken.controller;

import cn.dev33.satoken.session.SaSession;
import cn.dev33.satoken.stp.StpUtil;
import java.lang.reflect.Array;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author jlz
 * @date 2022年12月04日 22:39
 */
@RestController
@RequestMapping("/user/")
public class UserController {
    // 测试登录，浏览器访问： http://localhost:8081/user/doLogin?username=zhang&password=123456
    @RequestMapping("doLogin")
    public String doLogin(String username, String password) {
        // 此处仅作模拟示例，真实项目需要从数据库中查询数据进行比对
        if("zhang".equals(username) && "123456".equals(password)) {
            StpUtil.login(10001);
            return "登录成功";
        }
        return "登录失败";
    }

    // 查询登录状态，浏览器访问： http://localhost:8081/user/isLogin
    @RequestMapping("isLogin")
    public String isLogin() {
        return "当前会话是否登录：" + StpUtil.isLogin();
    }

    /**
     * 获取token详情
     */
    @RequestMapping("getToken")
    public String getToken() {
        // 获取当前会话的token值
        return "信息参数: "+StpUtil.getTokenInfo() +" token: "+StpUtil.getTokenValue() + "\n token名称: "+StpUtil.getTokenName()+"\n" +" 当前会话剩余有效期: "+"\n"+  StpUtil.getTokenTimeout();
    }

    @RequestMapping("getPermission")
    public String getPermission() {
        // 校验：当前账号是否含有指定角色标识, 如果验证未通过，则抛出异常: NotRoleException
        StpUtil.checkRole("super-admin");
        // 获取当前会话的token值
        return StpUtil.getPermissionList().toString();
    }

    @RequestMapping("logout")
    public void logout() {
        SaSession session = StpUtil.getSession();
        //强制注销
        StpUtil.logout(10001);
    }

    @RequestMapping("kickout")
    public void kickout() {
        //踢人下线
        StpUtil.kickout(10001);
    }
}

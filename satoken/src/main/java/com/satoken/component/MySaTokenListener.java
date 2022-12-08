package com.satoken.component;

import cn.dev33.satoken.listener.SaTokenListener;
import cn.dev33.satoken.stp.SaLoginModel;
import org.springframework.stereotype.Component;

//被 SpringBoot 扫描并自动注册到事件中心
@Component
public class MySaTokenListener  implements SaTokenListener {

    @Override
    public void doLogin(String s, Object o, String s1, SaLoginModel saLoginModel) {
        System.out.println("监听到登录事件");
    }

    @Override
    public void doLogout(String s, Object o, String s1) {

    }

    @Override
    public void doKickout(String s, Object o, String s1) {

    }

    @Override
    public void doReplaced(String s, Object o, String s1) {

    }

    @Override
    public void doDisable(String s, Object o, String s1, int i, long l) {

    }

    @Override
    public void doUntieDisable(String s, Object o, String s1) {

    }

    @Override
    public void doOpenSafe(String s, String s1, String s2, long l) {

    }

    @Override
    public void doCloseSafe(String s, String s1, String s2) {

    }

    @Override
    public void doCreateSession(String s) {

    }

    @Override
    public void doLogoutSession(String s) {

    }

    @Override
    public void doRenewTimeout(String s, Object o, long l) {

    }
}

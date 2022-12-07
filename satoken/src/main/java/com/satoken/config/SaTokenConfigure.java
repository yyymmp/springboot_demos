package com.satoken.config;

import cn.dev33.satoken.interceptor.SaInterceptor;
import cn.dev33.satoken.stp.StpUtil;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class SaTokenConfigure implements WebMvcConfigurer {

    //    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //使用注解鉴权  将注解加在方法或者类上   注册 Sa-Token 拦截器，打开注解式鉴权功能
//        registry.addInterceptor(new SaInterceptor()).addPathPatterns("/**");

        //使用路由鉴权
        registry.addInterceptor(new SaInterceptor(handle -> StpUtil.checkLogin()))
                //要拦截的路由
                .addPathPatterns("/**")
                //放行
                .excludePathPatterns("/user/doLogin");

    }


}

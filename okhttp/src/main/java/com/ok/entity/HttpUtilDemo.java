package com.ok.entity;

import cn.zhxu.data.Array;
import cn.zhxu.data.Mapper;
import cn.zhxu.okhttps.HTTP;
import cn.zhxu.okhttps.HttpResult;
import cn.zhxu.okhttps.OkHttps;
import cn.zhxu.okhttps.gson.GsonMsgConvertor;

import java.util.UUID;
import java.util.concurrent.locks.LockSupport;

public class HttpUtilDemo {
    //OkHttps 提供了两个开箱即用的工具类，让你从此告别封装工具类的烦恼：

    //OkHttps	2.0.0.RC	支持自动注入MsgConvertor，支持 SPI 方式注入配置，推荐用于主应用中的网络开发
    //HttpUtils	1.0.0	自2.0.0.RC开始支持自动注入MsgConvertor，不建议再做其它配置，推荐用于第三方依赖包中的网络开发

    public static void main(String[] args) {
        Thread thread = Thread.currentThread();
        OkHttps.async("https://jsonplaceholder.typicode.com/posts")
                //添加请求头
                //.addHeader()
                //Content-Type 请求头 是一个比较特殊的请求头 ，有一个专用的设定方法
                //.bodyType("application/json")
                //addBody 表单参数
                .addBodyPara("title","foo")
                .addBodyPara("body","bar")
                .addBodyPara("userId", UUID.randomUUID())
                //指定报文类型 默认是json  可显示指定为form表单
                .bodyType(OkHttps.FORM)
                .setOnResponse(result -> {
                    //使用mapper 减少一个实体类使用
                    Mapper mapper = result.getBody().toMapper();
                    System.out.println(mapper);
                    LockSupport.unpark(thread);
                })
                .post();
        LockSupport.park();

        //路径参数
        Mapper id = OkHttps.sync("https://jsonplaceholder.typicode.com/posts/{id}")
                .addPathPara("id", 1)
                .get().getBody().toMapper();
        System.out.println(id);

        //查询参数 ：拼接在 URL 的?之后（查询参数）
        Array postId = OkHttps.sync("http://jsonplaceholder.typicode.com/comments")
                .addUrlPara("postId", 4)
                .get().getBody().toArray();
        System.out.println(postId);

        //文件参数 无论当前的默认bodyType是什么，和文件参数一起添加的Body参数都将以form（表单）模式提交。
        OkHttps.sync("http://jsonplaceholder.typicode.com/comments")
                .addFilePara("image","D:/image/avatar.jpg")
                .post();

        //异常处理   nothrow() 让异常不直接抛出
        HTTP http = HTTP.builder()
                //根地址
                .baseUrl("https://jsonplaceholder.typicode.com")
                .addMsgConvertor(new GsonMsgConvertor())
                .build();
        HttpResult result = http.sync("/users/1").nothrow().get();
        System.out.println(result.getState());
        System.out.println(result.getError());

    }
}


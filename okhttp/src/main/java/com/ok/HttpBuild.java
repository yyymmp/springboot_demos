package com.ok;

import cn.zhxu.okhttps.HTTP;
import cn.zhxu.okhttps.gson.GsonMsgConvertor;
import com.ok.entity.User;

import java.util.Scanner;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.LockSupport;

public class HttpBuild {
    public static void main(String[] args) throws InterruptedException {
        Thread thread = Thread.currentThread();
        HTTP http = HTTP.builder()
                //根地址
                .baseUrl("https://jsonplaceholder.typicode.com")
                .addMsgConvertor(new GsonMsgConvertor())
                .build();
        //sync 发起get同步请求  同时将body转成对象
        //System.out.println( http.sync("/users").get().getBody().toList(User.class));

        //异步请求  设置回调函数
        http.async("/users").setOnResponse(httpResult -> {
            System.out.println(httpResult.getBody().toList(User.class));
            LockSupport.unpark(thread);
        }).get();
        System.out.println("main thread park");
        LockSupport.park();
        System.out.println("main thread unpark");

        //websocket 这里是websocket客户端
        http = HTTP.builder()
                //根地址
                .baseUrl("ws://121.40.165.18:8800")
                .addMsgConvertor(new GsonMsgConvertor())
                .build();
        http.webSocket("")
                .setOnOpen((webSocket,result)->{
                    webSocket.send("你好,我是ok http");
                })
                .setOnMessage(((webSocket, message) -> {
                    System.out.println("接受消息"+ message.toString());


                })).listen();


    }
}

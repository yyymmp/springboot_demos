package com.ok.entity;

import cn.zhxu.okhttps.Download;
import cn.zhxu.okhttps.OkHttps;

import java.io.File;

public class FileDownLoad {
    public static void main(String[] args) {
        //同步下载
        Download.Ctrl ctrl = OkHttps.sync("/download/test.zip")
                .get() //请求方式，根据服务器
                .getBody()
                .toFile("D:/download/test.zip")  //本地下载路径
                .start(); //启动下载
        //下载状态 各类下载通用
        ctrl.status();      // 下载状态
        ctrl.pause();       // 暂停下载
        ctrl.resume();      // 恢复下载
        ctrl.cancel();      // 取消下载（同时会删除文件，不可恢复）

        //异步下载
        OkHttps.async("/download/test.zip")
                .setOnResponse(result -> {
                    result.getBody().toFolder("D:/download").start();
                })
                .get();

        //下载进度监听
        OkHttps.sync("/download/test.zip")
                .get()
                .getBody()
                .stepBytes(1024)   // 设置每接收 1024 个字节执行一次进度回调（不设置默认为 8192）
                //     .stepRate(0.01)    // 设置每接收 1% 执行一次进度回调（不设置以 StepBytes 为准）
                .setOnProcess(( cn.zhxu.okhttps.Process process) -> {           // 下载进度回调
                    long doneBytes = process.getDoneBytes();   // 已下载字节数
                    long totalBytes = process.getTotalBytes(); // 总共的字节数
                    double rate = process.getRate();           // 已下载的比例
                    boolean isDone = process.isDone();         // 是否下载完成
                })
                .toFolder("D:/download/")        // 指定下载的目录，文件名将根据下载信息自动生成
                //     .toFile("D:/download/test.zip")  // 指定下载的路径，若文件已存在则覆盖
                .setOnSuccess((File file) -> {   // 下载成功回调

                })
                .start();

    }
}

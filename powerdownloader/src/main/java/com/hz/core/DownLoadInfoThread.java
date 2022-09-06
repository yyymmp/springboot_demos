package com.hz.core;

/**
 * 展示下载信息
 * @author jlz
 * @date 2022年09月05日 23:17
 */
public class DownLoadInfoThread implements Runnable{
    //总大小
    private long httpFileContentLength;

    //本地已下载文件大小
    public double finishSize;

    //前一次下载大小
    public volatile  double preSize;

    //本地累计下载的大小
    public volatile  double downSize;
    @Override
    public void run() {

    }
}

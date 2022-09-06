package com.hz.core;

import com.hz.constant.Constant;
import java.util.concurrent.atomic.LongAdder;

/**
 * 展示下载信息
 *
 * @author jlz
 * @date 2022年09月05日 23:17
 */
public class DownLoadInfoThread implements Runnable {

    //总大小
    private long httpFileContentLength;

    //本地已下载文件大小
    public static LongAdder finishSize = new LongAdder();

    //前一次下载大小
    public volatile double preSize;

    //本地累计下载的大小
    public static volatile LongAdder downSize = new LongAdder();

    public DownLoadInfoThread(int contentLength) {
        this.httpFileContentLength = contentLength;
    }

    @Override
    public void run() {
        //计算文件总大小
        String size = String.format("%.2f", httpFileContentLength / Constant.MB);
        //每秒下载速度 本线程一秒执行一次
        int v = (int) ((downSize.doubleValue() - preSize) / 1024d);
        preSize = downSize.doubleValue();

        //剩余文件大小
        double remain = httpFileContentLength - downSize.doubleValue() - finishSize.doubleValue();

        //剩余时间
        String remainTime = String.format("%.2f", remain / 2014d / v);
        if ("Infinity".equalsIgnoreCase(remainTime)) {
            remainTime = "-";
        }

        //已下载大小
        String currentSize = String.format("%.2f", (downSize.doubleValue() - finishSize.doubleValue()) / Constant.MB);

        String info = String.format("已下载 %smb/%smb,速度 %skb/s,剩余时间%ss", currentSize, size, v, remainTime);

        System.out.print("\r");
        System.out.print(info);
    }
}

package com.hz.core;

import cn.hutool.core.util.EnumUtil;
import com.hz.Main;
import com.hz.constant.Constant;
import com.hz.util.FileUtils;
import com.hz.util.HttpUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.net.HttpURLConnection;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jlz
 * @date 2022年09月05日 21:32
 */
public class DownLoader {

    private static final Logger logger = LoggerFactory.getLogger(DownLoader.class);

    public ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    public ThreadPoolExecutor poolExecutor = new ThreadPoolExecutor(Constant.THREAD_NUM, Constant.THREAD_NUM, 0, TimeUnit.SECONDS, new ArrayBlockingQueue<>(5));

    public void downLoad(String url) {
        String fileName = HttpUtils.getFileName(url);
        //文件下载路径
        fileName = Constant.PATH + fileName;
        //获取本地文件大小
        long fileContentLength = FileUtils.getFileContentLength(fileName);

        HttpURLConnection httpURLConnection = null;
        DownLoadInfoThread downLoadInfoThread = null;
        try {
            httpURLConnection = HttpUtils.get(url);
            //下载文件带大小
            int contentLength = httpURLConnection.getContentLength();
            if (contentLength == fileContentLength) {
                logger.info("{}已经下载完毕,无需重新下载", fileName);
                return;
            }

            //
            downLoadInfoThread = new DownLoadInfoThread(contentLength);
            scheduledExecutorService.scheduleAtFixedRate(downLoadInfoThread, 1, 1, TimeUnit.SECONDS);

            //切分任务
            ArrayList<Future> list = new ArrayList<>();
            //分割任务
            split(url, list);

            for (Future future : list) {
                try {
                    future.get();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
            ;
           if (merge(fileName)){
               //清理文件
               clearTemp(fileName);
           }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            httpURLConnection.disconnect();
            System.out.println("下载完成");
            poolExecutor.shutdown();
            scheduledExecutorService.shutdown();
        }

        //try {
        //    inputStream = httpURLConnection.getInputStream();
        //    bis = new BufferedInputStream(inputStream);
        //    //输出流输出到文件目录
        //    fileOutputStream = new FileOutputStream(fileName);
        //    bos = new BufferedOutputStream(fileOutputStream);
        //
        //    int len = -1;
        //    byte[] buffer = new byte[Constant.BYTE_SIZE];
        //    while ((len = bis.read(buffer)) != -1) {
        //        //累计下载量
        //        downLoadInfoThread.downSize += len;
        //        bos.write(buffer, 0, len);
        //    }
        //} catch (IOException ioException) {
        //    System.out.println("下载失败");
        //    ioException.printStackTrace();
        //} finally {
        //    System.out.print("\r");
        //    System.out.print("下载完成");
        //    try {
        //        bos.close();
        //        fileOutputStream.close();
        //        bis.close();
        //        inputStream.close();
        //        httpURLConnection.disconnect();
        //    } catch (IOException ioException) {
        //        ioException.printStackTrace();
        //    }
        //
        //    scheduledExecutorService.shutdown();
        //}

    }

    /**
     * @param url         下载地址
     * @param futureArray 任务返回值
     */
    public void split(String url, ArrayList<Future> futureArray) {
        long httpFileContentLength = HttpUtils.getHttpFileContentLength(url);
        //分块文件带线啊哦
        long size = httpFileContentLength / Constant.THREAD_NUM;
        for (int i = 0; i < Constant.THREAD_NUM; i++) {
            //计算下载起始位置
            long start = i * size;
            //结束位置
            long end;
            if (i == Constant.THREAD_NUM - 1) {
                //下载最后一块 则下载剩余的部分
                end = 0;
            } else {
                end = start + size;
            }

            //如果不是第一块 起始位置要+1
            if (start != 0) {
                start++;
            }

            //创建任务对象
            DownloadTask downloadTask = new DownloadTask(url, start, end, i);
            //提交
            Future<Boolean> future = poolExecutor.submit(downloadTask);

            futureArray.add(future);
        }
    }

    public boolean merge(String fileName){
        logger.info("开始合并文件{}",fileName);
        byte[] buff = new byte[Constant.BYTE_SIZE];
        int len = -1;
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(fileName, "rw");
            for (int i = 0; i < Constant.THREAD_NUM; i++) {
                BufferedInputStream bis = new BufferedInputStream(new FileInputStream(fileName + ".tmp" + i));
                while ((len = bis.read(buff)) != -1){
                    randomAccessFile.write(buff,0,len);
                }
            }
            logger.info("文件合并完毕 {}",fileName);
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            try {
                randomAccessFile.close();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }
        return true;
    }

    public boolean clearTemp(String fileName){
        for (int i = 0; i < Constant.THREAD_NUM; i++) {
            File file = new File(fileName + ".temp" + i);
            logger.info("删除文件:{}",file.getName());
            file.delete();
        }
        return true;
    }
}

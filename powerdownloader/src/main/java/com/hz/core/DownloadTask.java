package com.hz.core;

import com.hz.constant.Constant;
import com.hz.util.FileUtils;
import com.hz.util.HttpUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.util.concurrent.Callable;

/**
 * 分块下载任务
 *
 * @author jlz
 * @date 2022年09月06日 21:31
 */
public class DownloadTask implements Callable<Boolean> {

    private String url;
    //下载其实位置
    private long start;
    //下载结束位置
    private long end;
    //第几部门
    private int part;

    public DownloadTask(String utl, long start, long end, int part) {
        this.url = utl;
        this.start = start;
        this.end = end;
        this.part = part;
    }

    @Override
    public Boolean call() throws Exception {
        String fileName = HttpUtils.getFileName(url);
        fileName = fileName + ".tmp" + part;
        //下载路径
        fileName = Constant.PATH + fileName;
        //获取分块下载链接
        HttpURLConnection httpURLConnection = HttpUtils.getHttpURLConnection(url, start, end);
        InputStream inputStream = null;
        RandomAccessFile accessFile = null;
        BufferedInputStream bis = null;
        try {
            inputStream = httpURLConnection.getInputStream();
            bis = new BufferedInputStream(inputStream);
            accessFile = new RandomAccessFile(fileName,"rw");
            byte[] buffer = new byte[Constant.BYTE_SIZE];
            int len = -1;
            while ((len = bis.read(buffer)) != -1){
                //累计下载之和
                DownLoadInfoThread.downSize.add(len);
                accessFile.write(buffer,0,len);
            }
            return true;
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }finally {
            System.out.print("\r");
            try {
                accessFile.close();
                bis.close();
                inputStream.close();
                httpURLConnection.disconnect();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }

    }
}

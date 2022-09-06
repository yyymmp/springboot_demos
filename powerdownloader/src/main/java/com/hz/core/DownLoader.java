package com.hz.core;

import cn.hutool.core.util.EnumUtil;
import com.hz.constant.Constant;
import com.hz.util.HttpUtils;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;

/**
 * @author jlz
 * @date 2022年09月05日 21:32
 */
public class DownLoader {


    public void downLoad(String url) {
        String fileName = HttpUtils.getFileName(url);
        //文件下载路径
        fileName = Constant.PATH + fileName;
        HttpURLConnection httpURLConnection = null;
        InputStream inputStream = null;
        BufferedInputStream bis = null;
        FileOutputStream fileOutputStream = null;
        BufferedOutputStream bos = null;
        try {
            httpURLConnection = HttpUtils.get(url);
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }

        try {
            inputStream = httpURLConnection.getInputStream();
            bis = new BufferedInputStream(inputStream);
            //输出流输出到文件目录
            fileOutputStream = new FileOutputStream(fileName);
            bos = new BufferedOutputStream(fileOutputStream);

            int len = -1;
            while ((len = bis.read()) != -1) {
                bos.write(len);
            }
        } catch (IOException ioException) {
            System.out.println("下载失败");
            ioException.printStackTrace();
        } finally {
            try {
                bos.close();
                fileOutputStream.close();
                bis.close();
                inputStream.close();
                httpURLConnection.disconnect();
            } catch (IOException ioException) {
                ioException.printStackTrace();
            }
        }


    }
}

package com.hz.util;

import com.hz.core.DownLoader;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jlz
 * @date 2022年09月05日 21:25
 */
public class HttpUtils {

    private static final Logger logger = LoggerFactory.getLogger(HttpUtils.class);

    public static long getHttpFileContentLength(String url) {
        HttpURLConnection httpURLConnection = null;
        try {
            httpURLConnection = get(url);
            return httpURLConnection.getContentLength();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        } finally {
            httpURLConnection.disconnect();
        }

        return 0;
    }

    /**
     * 获取HttpURLConnection
     *
     * @param url
     * @return
     */
    public static HttpURLConnection get(String url) throws IOException {
        URL httpUrl = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
        httpURLConnection.setRequestProperty("User-Agent",
                "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 Aoyou/UksmeGVdeT5We0gmZjQKYAppAr9fYDivFRsjSlx687H5fvuC8GnJy2xR");

        return httpURLConnection;
    }

    public static String getFileName(String url) {
        int i = url.lastIndexOf("/");
        return url.substring(i + 1);
    }

    /**
     * 分块下载
     *
     * @param url   文件地址
     * @param start 开始文职
     * @param end   结束位置
     * @return
     */
    public static HttpURLConnection getHttpURLConnection(String url, long start, long end) throws IOException {
        HttpURLConnection httpURLConnection = get(url);
        logger.info("下载区间:{} -- {}", start, end);
        if (end != 0) {
            //说明是中间数据块  http支持
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + start + "-" + end);
        } else {
            //下载剩下所有数据
            httpURLConnection.setRequestProperty("RANGE", "bytes=" + start + "-");
        }

        return httpURLConnection;
    }
}

package com.hz.util;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * @author jlz
 * @date 2022年09月05日 21:25
 */
public class HttpUtils {

    /**
     * 获取HttpURLConnection
     * @param url
     * @return
     */
    public static HttpURLConnection get(String url) throws IOException {
        URL httpUrl = new URL(url);
        HttpURLConnection httpURLConnection = (HttpURLConnection) httpUrl.openConnection();
        httpURLConnection.setRequestProperty("User-Agent","Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/105.0.0.0 Safari/537.36 Aoyou/UksmeGVdeT5We0gmZjQKYAppAr9fYDivFRsjSlx687H5fvuC8GnJy2xR");

        return httpURLConnection;
    }

    public static String getFileName(String url){
        int i = url.lastIndexOf("/");
        return url.substring(i+1);
    }
}

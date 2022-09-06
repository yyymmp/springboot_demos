package com.hz.util;

import java.io.File;

/**
 * @author jlz
 * @date 2022年09月06日 20:22
 */
public class FileUtils {

    //获取本地文件大小
    public static long getFileContentLength(String path){
        File file = new File(path);
        return file.exists() && file.isFile() ? file.length() : 0;
    }
}

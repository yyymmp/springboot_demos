package com.hz;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.hz.core.DownLoader;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;
import java.util.Scanner;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author jlz
 * @date 2022年09月05日 21:09
 */
public class Main {
    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {

        String url = null;
        if (CollectionUtil.isEmpty(Arrays.asList(args))){
            while (true){
                System.out.println("请下载地址");
                Scanner scanner = new Scanner(System.in);
                url = scanner.next();
                if (url != null){
                    break;
                }
            }

        }else {
            //取出地址
            url = args[0];
        }

        DownLoader downLoader = new DownLoader();
        downLoader.downLoad(url);
        logger.info("下载成功");
    }
}

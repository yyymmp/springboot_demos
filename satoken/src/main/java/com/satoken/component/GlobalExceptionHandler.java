package com.satoken.component;

import cn.dev33.satoken.util.SaResult;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author jlz
 * @date 2022年12月04日 23:37
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 全局异常拦截
     * @param e
     * @return
     */
    @ExceptionHandler
    public SaResult handlerException(Exception e) {
        e.printStackTrace();
        return SaResult.error(e.getMessage());
    }
}

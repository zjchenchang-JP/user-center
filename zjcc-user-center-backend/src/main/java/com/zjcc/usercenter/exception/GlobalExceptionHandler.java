package com.zjcc.usercenter.exception;

import com.zjcc.usercenter.common.BaseResponse;
import com.zjcc.usercenter.common.ErrorCode;
import com.zjcc.usercenter.common.ResponseResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * @author zjchenchang
 * @createDate 2026/1/12 16:03
 * @description 全局异常处理器 捕捉代码所有异常 集中处理 让前端获得更详细的信息
 * 否则代码中抛异常，前端只返回500(http状态码--服务器内部错误)
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    public BaseResponse<?> businessExceptionHandler(BusinessException e) {
        log.error("businessException: " + e.getMessage(), e);
        return ResponseResult.error(e.getCode(), e.getMessage(), e.getDescription());
    }

    @ExceptionHandler(RuntimeException.class)
    public BaseResponse<?> businessExceptionHandler(RuntimeException e) {
        log.error("runtimeException", e);
        return ResponseResult.error(ErrorCode.SYSTEM_ERROR,e.getMessage(),"");
    }

}

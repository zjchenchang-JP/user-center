package com.zjcc.usercenter.common;

/**
 * @author zjchenchang
 * @createDate 2026/1/12 14:25
 * @description 返回工具类
 */
public class ResponseResult {

    /**
     * 成功
     * @param data 数据
     * @return 通用返回类
     * @param <T> 响应数据
     */
    public static <T> BaseResponse<T> ok(T data) {
        return new BaseResponse<>(ErrorCode.SUCCESS.getCode(), data, "ok");
    }

    /**
     * 失败
     *
     * @param errorCode 自定义响应码-枚举类
     * @return 通用返回类
     */
    public static BaseResponse error(ErrorCode errorCode) {
        return new BaseResponse<>(errorCode);
    }

    /**
     * 失败
     *
     * @param code
     * @param message
     * @param description
     * @return 通用返回类
     */
    public static BaseResponse error(int code, String message, String description) {
        return new BaseResponse(code, null, message, description);
    }

    /**
     * 失败
     *
     * @param errorCode 自定义响应码-枚举类
     * @return 通用返回类
     */
    public static BaseResponse error(ErrorCode errorCode, String message, String description) {
        return new BaseResponse(errorCode.getCode(), null, message, description);
    }


    /**
     * 失败
     *
     * @param errorCode 自定义响应码-枚举类
     * @return 通用返回类
     */
    public static BaseResponse error(ErrorCode errorCode, String description) {
        return new BaseResponse(errorCode.getCode(), errorCode.getMessage(), description);
    }

}

package com.zjcc.usercenter.common;

import lombok.Data;
import java.io.Serializable;

/**
 * @author zjchenchang
 * @createDate 2026/1/12 14:07
 * @description 通用返回类--封装返回给前端的数据对象
 */
@Data
public class BaseResponse<T> implements Serializable {

    private static final long serialVersionUID = 2181839821713738094L;

    // 响应状态码
    private int code;

    // 提示信息
    private String message;

    // 数据
    private T data;

    // 详细描述
    private String description;

    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.message = message;
        this.data = data;
        this.description = description;
    }

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    public BaseResponse(int code, T data) {
        this(code, data, "", "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }
}

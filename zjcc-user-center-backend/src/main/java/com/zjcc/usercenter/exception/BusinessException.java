package com.zjcc.usercenter.exception;

import com.zjcc.usercenter.common.ErrorCode;
import lombok.Getter;

/**
 * @author zjchenchang
 * @createDate 2026/1/12 15:51
 * @description 自定义 业务异常
 */
@Getter
public class BusinessException extends RuntimeException {

    private static final long serialVersionUID = -778075236835744867L;

    /**
     * 异常码
     */
    private final int code;

    /**
     * 描述信息
     */
    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

}

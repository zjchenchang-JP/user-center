package com.zjcc.usercenter.common;

import lombok.Getter;

/**
 * @author zjchenchang
 * @createDate 2026/1/12 14:21
 * @description 返回错误码
 */
@Getter
public enum ErrorCode {
    SUCCESS(0, "ok", ""),
    PARAMS_ERROR(40000, "请求参数错误", ""),
    NULL_ERROR(40001, "请求数据为空", ""),
    NOT_LOGIN(40100, "未登录", ""),
    NO_AUTH(40101, "无权限", ""),
    USER_DUPLICATE(40002, "用户账户已存在", ""),
    USER_ACCOUNT_INVALID(40003, "用户账户包含特殊字符，不合法", ""),
    USER_PASSWORD_SHORT(40004, "用户密码长度不能小于8位", ""),
    PASSWORD_NOT_MATCH(40005, "两次输入密码不一致", ""),
    PLANET_CODE_TOO_LONG(40006, "用户编号长度不能超过5位", ""),
    PLANET_CODE_DUPLICATE(40007, "星球编号已存在", ""),
    USER_SAVE_FAILED(40008, "用户信息保存失败", ""),
    USER_ACCOUNT_SHORT(40009, "账户长度不能小于4位", ""),
    SYSTEM_ERROR(50000, "系统内部异常", "");

    /**
     * 状态码
     */
    private final int code;

    /**
     * 状态码信息
     */
    private final String message;

    /**
     * 状态码描述（详情）
     */
    private final String description;

    ErrorCode(int code, String message, String description) {
        this.code = code;
        this.message = message;
        this.description = description;
    }

}

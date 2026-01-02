package com.zjcc.usercenter.model.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体--类似DTO
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -4998439624886568904L;
    // 登录账号
    private String userAccount;

    // 密码
    private String userPassword;
}

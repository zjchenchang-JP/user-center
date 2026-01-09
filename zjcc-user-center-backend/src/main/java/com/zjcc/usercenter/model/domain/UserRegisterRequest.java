package com.zjcc.usercenter.model.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户注册请求体
 */
@Data
public class UserRegisterRequest implements Serializable {

    private static final long serialVersionUID = 118304375888268941L;
    // 登录账号
    private String userAccount;

    // 密码
    private String userPassword;

    // 校验密码
    private String checkPassword;

    // 用户编号
    private String planetCode;
}

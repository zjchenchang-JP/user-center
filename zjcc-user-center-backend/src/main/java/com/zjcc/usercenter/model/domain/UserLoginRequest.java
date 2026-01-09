package com.zjcc.usercenter.model.domain;

import lombok.Data;

import java.io.Serializable;

/**
 * 用户登录请求体--类似DTO
 */
@Data
public class UserLoginRequest implements Serializable {

    private static final long serialVersionUID = -4998439624886568904L;
    // 登录账号
    private String userAccount;

    // 密码
    private String userPassword;
}
//@Data
//public class UserLoginRequest implements Serializable {
//    private static final long serialVersionUID = -4998439624886568904L;
//
//    // 用@JsonProperty指定前端传入的字段名  前后端联调变量字段名不一致时
//    @JsonProperty("account") // 前端传account，映射到userAccount
//    private String userAccount;
//
//    @JsonProperty("password") // 前端传password，映射到userPassword
//    private String userPassword;
//}
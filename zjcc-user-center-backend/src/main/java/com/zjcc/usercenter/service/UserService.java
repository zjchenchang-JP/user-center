package com.zjcc.usercenter.service;

import com.zjcc.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author 86187
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2025-12-31 18:59:01
*/
public interface UserService extends IService<User> {

    // 用户注册
    long userRegister(String userAccount, String userPassword, String checkPassword);

    // 用户登录
    User doLogin(String userAccount, String userPassword, HttpServletRequest request);
}

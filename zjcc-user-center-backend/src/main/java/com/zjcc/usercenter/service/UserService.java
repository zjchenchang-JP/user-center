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

    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);

    /**
     * 登录方法
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request  servlet请求对象
     * @return 已登录用户信息
     */
    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户注销
     *
     * @param request servlet请求对象
     * @return 注销成功标志
     */
    boolean userLogout(HttpServletRequest request);
}

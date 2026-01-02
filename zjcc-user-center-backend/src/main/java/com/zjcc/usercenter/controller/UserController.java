package com.zjcc.usercenter.controller;

import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.model.domain.UserLoginRequest;
import com.zjcc.usercenter.model.domain.UserRegisterRequest;
import com.zjcc.usercenter.service.UserService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    UserService userService;

    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 请求体为空
        if (userRegisterRequest == null) {
            return null;
        }
        String userPassword = userRegisterRequest.getUserPassword();
        String userAccount = userRegisterRequest.getUserAccount();
        String checkPassword = userRegisterRequest.getCheckPassword();
        // 参数不能为空
        if (StringUtils.isAnyBlank(userPassword, userAccount, checkPassword)) {
            return null;
        }
        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("/login")
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        System.out.println("===== 进入userLogin方法 ====="); // 新增日志
        // 请求体为空
        if (userLoginRequest == null) {
            return null;
        }
        String userPassword = userLoginRequest.getUserPassword();
        String userAccount = userLoginRequest.getUserAccount();
        // 参数不能为空
        if (StringUtils.isAnyBlank(userPassword, userAccount)) {
            return null;
        }
        return userService.userLogin(userAccount, userPassword, request);
    }
}

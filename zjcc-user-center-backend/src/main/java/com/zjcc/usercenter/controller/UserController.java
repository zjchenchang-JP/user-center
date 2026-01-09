package com.zjcc.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.sun.org.apache.bcel.internal.generic.NEW;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.model.domain.UserLoginRequest;
import com.zjcc.usercenter.model.domain.UserRegisterRequest;
import com.zjcc.usercenter.service.UserService;
import com.zjcc.usercenter.service.impl.UserServiceImpl;
import com.zjcc.usercenter.utils.StaticConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.zjcc.usercenter.utils.StaticConst.ADMIN_ROLE;
import static com.zjcc.usercenter.utils.StaticConst.USER_LOGIN_STATE;

@RestController
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true") // 临时解决跨越问题
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    // 用户注册
    @PostMapping("/register")
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 请求体为空
        if (userRegisterRequest == null) {
            return null;
        }
        String userPassword = userRegisterRequest.getUserPassword();
        String userAccount = userRegisterRequest.getUserAccount();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();
        // 参数不能为空
        if (StringUtils.isAnyBlank(userPassword, userAccount, checkPassword,planetCode)) {
            return null;
        }
        return userService.userRegister(userAccount, userPassword, checkPassword,planetCode);
    }

    // 用户登录
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

    // 获取当前用户
    @GetMapping("/current")
    public User getCurrentUser(HttpServletRequest request) {
        // 从session获取当前登录用户
        Object stateUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        // null 不是任何类型：stateUser instanceof User 在 stateUser 为 null 时会返回 false
        // 安全的类型检查：instanceof 操作符本身就包含了 null 值检查
        // 条件不成立：当 stateUser 为 null 时，if (stateUser instanceof User) 条件为 false
        if (stateUser instanceof User) {
            // instanceof 检查通过，已确保 stateUser 不为 null
            return (User) stateUser;
        }
        return null;
    }

    // 用户注销
    @PostMapping("/logout")
    public Integer userLogout(HttpServletRequest request) {
        return userService.userLogout(request);
    }


    /**
     * 用户管理接口 (仅管理员可见)
     */
    // 查询所有用户
    @GetMapping("/search")
    public List<User> searchUser(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            log.warn("缺少管理员权限!");
            return new ArrayList<>();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            // 前端传入了userName 参数 才设置查询条件
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        // 脱敏后返回
        List<User> users = userList.stream().map(UserServiceImpl::getSafetyUser).collect(Collectors.toList());
        return users;
    }

    // 删除用户
    @DeleteMapping("/delete")
    public boolean deleteUser(@RequestBody User user, HttpServletRequest request) {
        if (!isAdmin(request)) {
            log.warn("缺少管理员权限");
            return false;
        }
        return userService.removeById(user);
    }


    // 鉴权 必须管理者角色才能操作‘用户管理接口’
    private boolean isAdmin(HttpServletRequest request) {
        // 从session获取当前登录用户
        Object stateUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        // null 不是任何类型：stateUser instanceof User 在 stateUser 为 null 时会返回 false
        // 安全的类型检查：instanceof 操作符本身就包含了 null 值检查
        // 条件不成立：当 stateUser 为 null 时，if (stateUser instanceof User) 条件为 false
        if (stateUser instanceof User) {
            // instanceof 检查通过，已确保 stateUser 不为 null
            User currentUser = (User) stateUser;
            return ADMIN_ROLE == (currentUser.getUserRole());
        }
        // 类型转换失败 或 stateUser = null
        return false;
    }

}

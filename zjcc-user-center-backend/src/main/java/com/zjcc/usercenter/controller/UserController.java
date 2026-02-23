package com.zjcc.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zjcc.usercenter.common.BaseResponse;
import com.zjcc.usercenter.common.ErrorCode;
import com.zjcc.usercenter.common.ResponseResult;
import com.zjcc.usercenter.exception.BusinessException;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.model.domain.UserLoginRequest;
import com.zjcc.usercenter.model.domain.UserRegisterRequest;
import com.zjcc.usercenter.service.UserService;
import com.zjcc.usercenter.service.impl.UserServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.zjcc.usercenter.utils.StaticConst.ADMIN_ROLE;
import static com.zjcc.usercenter.utils.StaticConst.USER_LOGIN_STATE;

@RestController
@CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000", "https://tracheoscopic-collectedly-barb.ngrok-free.dev"}, allowCredentials = "true") // 临时解决跨越问题
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Resource
    UserService userService;

    // 用户注册
    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest) {
        // 仅做请求体非空判断（避免后续获取属性空指针）
        if (userRegisterRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userPassword = userRegisterRequest.getUserPassword();
        String userAccount = userRegisterRequest.getUserAccount();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        return ResponseResult.ok(result);
    }

    // 用户登录
    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        // System.out.println("===== 进入userLogin方法 ====="); // 新增日志
        // 请求体为空
        if (userLoginRequest == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        String userPassword = userLoginRequest.getUserPassword();
        String userAccount = userLoginRequest.getUserAccount();
        User loginUser = userService.userLogin(userAccount, userPassword, request);
        return ResponseResult.ok(loginUser);
    }

    // 获取当前用户
    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        // 从session获取当前登录用户
        Object stateUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        // null 不是任何类型：stateUser instanceof User 在 stateUser 为 null 时会返回 false
        // 安全的类型检查：instanceof 操作符本身就包含了 null 值检查
        // 条件不成立：当 stateUser 为 null 时，if (stateUser instanceof User) 条件为 false
        if (stateUser instanceof User) {
            // instanceof 检查通过，已确保 stateUser 不为 null
            User curremtUser = (User) stateUser;
            return ResponseResult.ok(curremtUser);
        }
        throw new BusinessException(ErrorCode.NOT_LOGIN);
    }

    // 用户注销
    @PostMapping("/logout")
    public BaseResponse<Boolean> userLogout(HttpServletRequest request) {
        boolean result = userService.userLogout(request);
        return ResponseResult.ok(result);
    }


    /**
     * 用户管理接口 (仅管理员可见)
     */
    // 查询所有用户
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUser(String username, HttpServletRequest request) {
        if (!isAdmin(request)) {
            log.warn("缺少管理员权限!");
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            // 前端传入了userName 参数 才设置查询条件
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        // TODO 全查询后 应该添加分页机制
        // 脱敏后返回
        List<User> users = userList.stream().map(UserServiceImpl::getSafetyUser).collect(Collectors.toList());
        return ResponseResult.ok(users);
    }

    // 删除用户
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody Long id, HttpServletRequest request) {
        if (!isAdmin(request)) {
            log.warn("缺少管理员权限");
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        boolean b = userService.removeById(id);
        return ResponseResult.ok(b);
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
        // 类型转换失败
        if (stateUser != null) {
            return false;
        }
        // stateUser = null 未登录
        throw new BusinessException(ErrorCode.NOT_LOGIN);
    }
}

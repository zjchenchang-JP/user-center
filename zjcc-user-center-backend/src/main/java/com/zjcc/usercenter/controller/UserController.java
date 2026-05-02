package com.zjcc.usercenter.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjcc.usercenter.common.BaseResponse;
import com.zjcc.usercenter.common.ErrorCode;
import com.zjcc.usercenter.common.ResponseResult;
import com.zjcc.usercenter.exception.BusinessException;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.model.domain.UserLoginRequest;
import com.zjcc.usercenter.model.domain.UserRegisterRequest;
import com.zjcc.usercenter.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.stream.Collectors;

import static com.zjcc.usercenter.utils.StaticConst.USER_LOGIN_STATE;

@RestController
// 临时解决跨越问题
// @CrossOrigin // 默认支持所有网站都能跨域访问 *
// @CrossOrigin(origins = {"http://localhost:5173","http://localhost:3000", "http://43.163.195.79","https://tracheoscopic-collectedly-barb.ngrok-free.dev"}, allowCredentials = "true")
@RequestMapping("/api/user")
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

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
            User sessionUser = (User) stateUser;
            // 从数据库查最新数据，避免 Session 中的对象是旧数据
            // 否则前端显示刚更新成功，返回用户界面，此时查询的是session中的旧数据。
            User freshUser = userService.getById(sessionUser.getId());
            if (freshUser != null) {
                // 更新cookie
                request.getSession().setAttribute(USER_LOGIN_STATE, freshUser);
                return ResponseResult.ok(freshUser);
            }
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
        if (!userService.isAdmin(request)) {
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
        List<User> users = userList.stream()
                .map(user -> userService.getSafetyUser(user))
                .collect(Collectors.toList());
        return ResponseResult.ok(users);
    }

    // 根据标签名搜索用户
    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersByTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            log.warn("未传入标签 !");
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // TODO 分页
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResponseResult.ok(userList);
    }

    /**
     * 更新用户信息
     * @param user 待更新用户信息
     * @param request
     * @return 修改成功的记录数
     */
    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        // 参数校验
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getCurrentUser(request);
        int result = userService.updateUser(user, loginUser);
        return ResponseResult.ok(result);
    }

    /**
     * 主页推荐
     * @param request
     * @return 符和条件的用户集合
     */
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(
            @RequestParam(defaultValue = "10") long pageSize,
            @RequestParam(defaultValue = "1") long pageNum,
            HttpServletRequest request) {
        User loginUser = userService.getCurrentUser(request);
        Page<User> userPage = userService.recommendUsers(loginUser, pageSize, pageNum);
        return ResponseResult.ok(userPage);
    }


    /**
     * 删除用户
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody Long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            log.warn("缺少管理员权限");
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        return ResponseResult.ok(userService.removeById(id));
    }

}

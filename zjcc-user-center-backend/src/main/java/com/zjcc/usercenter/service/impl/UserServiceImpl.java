package com.zjcc.usercenter.service.impl;
import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.service.UserService;
import com.zjcc.usercenter.mapper.UserMapper;
import com.zjcc.usercenter.utils.StaticConst;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;


/**
* @author zjcc
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-12-31 18:59:01
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

    @Resource
    UserMapper userMapper;

    // ^[a-zA-Z0-9_]+$ 表示从开头到结尾只能是大小写字母、数字或下划线的组合
    String validPattern = "^[a-zA-Z0-9_]+$";

    /**
     * 用户注册
     * @param userAccount 用户账户
     * @param userPassword 用户密码
     * @param checkPassword 确认密码
     * @return 新用户id
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1.校验参数
        // 非null, 非空串
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            log.warn("账户密码不能为空！");
            // TODO 修改为全局自定义异常
            return -1;
        }

        // 账户长度不小于4
        if (userAccount.length() < 4) {
            return -1;
        }

        // 密码长度不小于8
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            return -1;
        }

        // 密码和确认密码必须相同
        if (!userPassword.equals(checkPassword)) {
            log.warn("密码和确认密码必须相同");
            return -1;
        }

        // 账户不包含特殊字符
        if (!userAccount.matches(validPattern)) {
            return -1;
        }

        // 账户不能重复
        // 有数据库查询操作，放到最后。性能优化
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if(count > 0) {
            log.warn("账户不能重复");
            return -1;
        }

        // 2.密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((StaticConst.SALT + userPassword).getBytes());

        // 3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        // 代码到这 说明注册并插入成功
        return user.getId();
    }

    /**
     * 登录方法
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @param request  servlet请求对象
     * @return 已登录用户信息
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        // 1.校验参数
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            log.warn("账户密码不能为空！");
            return null;
        }
        // 账户长度不小于4
        if (userAccount.length() < 4) {
            return null;
        }

        // 密码长度不小于8
        if (userPassword.length() < 8) {
            return null;
        }

        // 账户不包含特殊字符
        if (!userAccount.matches(validPattern)) return null;

        // 2.验证密码
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        String encryptPassword = DigestUtils.md5DigestAsHex((StaticConst.SALT + userPassword).getBytes());
        queryWrapper.eq("userPassword", encryptPassword);
        queryWrapper.eq("userAccount", userAccount);
        User loginUser = userMapper.selectOne(queryWrapper);
        // User loginUser = this.getOne(queryWrapper);
        if (loginUser == null) {
            log.warn("user login failed, useAccount cannot match userPassword");
            return null;
        }
        // 3.记录session状态
        request.getSession().setAttribute(StaticConst.USER_LOGIN_STATE, loginUser);

        // 4. 返回脱敏用户
        return getSafetyUser(loginUser);
    }


    /**
     * Desensitization 用户脱敏
     * @param loginUser 登录用户
     * @return safetyUser
     */
    private static User getSafetyUser(User loginUser) {
        User safetyUser = new User();
        safetyUser.setId(loginUser.getId());
        safetyUser.setUsername(loginUser.getUsername());
        safetyUser.setUserAccount(loginUser.getUserAccount());
        safetyUser.setAvatarUrl(loginUser.getAvatarUrl());
        safetyUser.setGender(loginUser.getGender());
        safetyUser.setPhone(loginUser.getPhone());
        safetyUser.setEmail(loginUser.getEmail());
        safetyUser.setUserStatus(loginUser.getUserStatus());
        safetyUser.setCreateTime(loginUser.getCreateTime());
        return safetyUser;
    }
}





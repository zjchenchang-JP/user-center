package com.zjcc.usercenter.service.impl;

import java.util.Date;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjcc.usercenter.common.ErrorCode;
import com.zjcc.usercenter.exception.BusinessException;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.service.UserService;
import com.zjcc.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import static com.zjcc.usercenter.utils.StaticConst.*;


/**
 * @author zjcc
 * @description 针对表【user(用户表)】的数据库操作Service实现
 * @createDate 2025-12-31 18:59:01
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
        implements UserService {

    @Resource
    UserMapper userMapper;

    // 用户注册
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
        // 1.校验参数
        // 非null, 非空串
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            log.warn("用户注册参数非空校验失败，账户：{}，密码：{}，确认密码：{}，星球编号：{}",
                    userAccount, userPassword, checkPassword, planetCode);
            // 全局自定义异常
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 账户长度不小于4
        if (userAccount.length() < 4) {
            log.warn("用户账户长度小于4位：{}", userAccount);
            throw new BusinessException(ErrorCode.USER_ACCOUNT_SHORT);
        }

        // 账户不包含特殊字符
        if (!userAccount.matches(VALID_PATTERN)) {
            log.warn("用户账户包含特殊字符，不合法：{}", userAccount);
            throw new BusinessException(ErrorCode.USER_ACCOUNT_INVALID);
        }


        // 密码长度不小于8
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            log.warn("用户密码长度小于8位");
            throw new BusinessException(ErrorCode.USER_PASSWORD_SHORT);
        }

        // 密码和确认密码必须相同
        if (!userPassword.equals(checkPassword)) {
            log.warn("两次输入密码不一致");
            throw new BusinessException(ErrorCode.PASSWORD_NOT_MATCH);
        }

        // 用户编号不能超过5位
        if (planetCode.length() > 5) {
            log.warn("星球编号长度超过5位");
            throw new BusinessException(ErrorCode.PLANET_CODE_TOO_LONG);
        }

        // 账户不能重复 唯一性校验
        // // 有数据库查询操作，放到校验流程的最后。性能优化
        // QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // queryWrapper.eq("userAccount", userAccount);
        // long count = this.count(queryWrapper);
        // if (count > 0) {
        //     log.warn("账户不能重复");
        //     return -1;
        // }
        //
        // // 用户编号不能重复
        // queryWrapper = new QueryWrapper<>();
        // queryWrapper.eq("planetCode", planetCode);
        // count = this.count(queryWrapper);
        // if (count > 0) {
        //     return -1;
        // }

        // 唯一性校验 优化 只查询一次数据库
        // 合并查询，一次数据库IO，统一异常提示
        long duplicateCount = this.count(new QueryWrapper<User>()
                .or()
                .eq("userAccount", userAccount)
                .eq("planetCode", planetCode));
        if (duplicateCount > 0) {
            log.warn("用户注册参数重复，账户：{}，星球编号：{}", userAccount, planetCode);
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账户或星球编号已存在");
        }

        // 2.密码加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());


        // 3.插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        user.setPlanetCode(planetCode);
        boolean saveResult = this.save(user);
        if (!saveResult) {
            log.error("用户信息保存失败，账户：{}", userAccount);
            throw new BusinessException(ErrorCode.USER_SAVE_FAILED);
        }
        // 注册成功，返回用户ID
        return user.getId();
    }

    // 用户登录
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
        if (!userAccount.matches(VALID_PATTERN)) return null;

        // 2.验证密码
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        queryWrapper.eq("userPassword", encryptPassword);
        queryWrapper.eq("userAccount", userAccount);
        User loginUser = userMapper.selectOne(queryWrapper);
        // User loginUser = this.getOne(queryWrapper);
        if (loginUser == null) {
            log.warn("user login failed, useAccount cannot match userPassword");
            return null;
        }
        // 3.记录session状态
        request.getSession().setAttribute(USER_LOGIN_STATE, loginUser);

        // 4. 返回脱敏用户
        return getSafetyUser(loginUser);
    }

    // 注销方法
    @Override
    public int userLogout(HttpServletRequest request) {
        if (request == null) {
            return -1;
        }
        // 删除session 数据
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        // 注销成功
        return 1;
    }


    /**
     * Desensitization 用户脱敏
     *
     * @param loginUser 登录用户
     * @return safetyUser  信息脱敏后的用户
     */
    public static User getSafetyUser(User loginUser) {
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
        safetyUser.setUserRole(loginUser.getUserRole());
        safetyUser.setPlanetCode(loginUser.getPlanetCode());
        return safetyUser;
    }
}





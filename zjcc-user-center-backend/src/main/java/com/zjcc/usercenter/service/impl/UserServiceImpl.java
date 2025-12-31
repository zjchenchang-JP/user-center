package com.zjcc.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.service.UserService;
import com.zjcc.usercenter.mapper.UserMapper;
import com.zjcc.usercenter.utils.StaticConst;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;


/**
* @author zjcc
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2025-12-31 18:59:01
*/
@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService{

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
            System.out.println("账户密码不能为空！");
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
            System.out.println("密码和确认密码必须相同");
            return -1;
        }

        // 账户不包含特殊字符
        // ^[a-zA-Z0-9_]+$ 表示从开头到结尾只能是大小写字母、数字或下划线的组合
        String validPattern = "^[a-zA-Z0-9_]+$";
        if (!userAccount.matches(validPattern)) {
            return -1;
        }

        // 账户不能重复
        // 有数据库查询操作，放到最后。性能优化
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = this.count(queryWrapper);
        if(count > 0) {
            System.out.println("账户不能重复");
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
}





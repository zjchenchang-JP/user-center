package com.zjcc.usercenter.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.zjcc.usercenter.common.ErrorCode;
import com.zjcc.usercenter.exception.BusinessException;
import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.service.UserService;
import com.zjcc.usercenter.mapper.UserMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

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

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

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
        // 合并查询，只一次数据库IO，统一异常提示
        // 注意Mybatis语法
        // QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
        //         .and(wrapper -> wrapper
        //                 .eq("userAccount", userAccount)
        //                 .or()
        //                 .eq("planetCode", planetCode));
        QueryWrapper<User> queryWrapper = new QueryWrapper<User>()
                        .eq("userAccount", userAccount)
                        .or()
                        .eq("planetCode", planetCode);
        // // 打印生成的 SQL
        // System.out.println("SQL: " + queryWrapper.getCustomSqlSegment());
        // System.out.println("参数: " + queryWrapper.getParamNameValuePairs());

        long duplicateCount = this.count(queryWrapper);
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
            // TODO 此处应该判断幂等？？
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
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 账户长度不小于4
        if (userAccount.length() < 4) {
            log.warn("用户账户长度小于4位：{}", userAccount);
            throw new BusinessException(ErrorCode.USER_ACCOUNT_SHORT);
        }

        // 密码长度不小于8
        if (userPassword.length() < 8) {
            log.warn("用户密码长度小于8位");
            throw new BusinessException(ErrorCode.USER_PASSWORD_SHORT);
        }

        // 账户不包含特殊字符
        if (!userAccount.matches(VALID_PATTERN)) throw new BusinessException(ErrorCode.USER_ACCOUNT_INVALID);

        //  第一步：先根据用户账户查询用户（仅查询账户，不涉及密码）
        QueryWrapper<User> accountQueryWrapper = new QueryWrapper<>();
        accountQueryWrapper.eq("userAccount", userAccount);
        User loginUser = userMapper.selectOne(accountQueryWrapper);

        // 判断账户是否存在
        if (loginUser == null) {
            log.warn("user login failed, userAccount [{}] does not exist", userAccount);
            throw new BusinessException(ErrorCode.USER_ACCOUNT_NOT_EXIST); // 对外提示可模糊，内部日志明确
        }

        // 3. 第二步：账户存在时，单独验证密码
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        if (!encryptPassword.equals(loginUser.getUserPassword())) {
            log.warn("user login failed, userAccount [{}] password is incorrect", userAccount);
            throw new BusinessException(ErrorCode.ACCOUNT_OR_PASSWORD_ERROR); // 对外可模糊提示，避免泄露密码错误细节
        }
        // 3.记录session状态
        // 后端创建的 Session 绑定到域名，无法跨域共享!!
        request.getSession().setAttribute(USER_LOGIN_STATE, loginUser);

        // 4. 返回脱敏用户
        return getSafetyUser(loginUser);
    }

    // 注销方法
    @Override
    public boolean userLogout(HttpServletRequest request) {
        // TODO
        // try {
        //     HttpSession session = request.getSession(false);
        //     // 场景1：会话不存在（容器都没有）
        //     if (session == null) {
        //         return UserOperateResultEnum.SESSION_NOT_EXIST;
        //     }
        //     // 场景2：会话存在，但无登录属性（容器有，无目标数据）
        //     Object loginState = session.getAttribute(USER_LOGIN_STATE);
        //     if (loginState == null) {
        //         return UserOperateResultEnum.USER_NOT_LOGIN;
        //     }
        //     // 场景3：会话存在且有登录属性，正常注销
        //     session.removeAttribute(USER_LOGIN_STATE);
        //     // 可选：若无需保留会话其他数据，可销毁整个会话
        //     // session.invalidate();
        //     return UserOperateResultEnum.LOGOUT_SUCCESS;
        // } catch (Exception e) {
        //     // log.error("用户注销业务处理失败，异常信息：", e);
        //     return UserOperateResultEnum.LOGOUT_FAILED;
        // }

        // ❌ 不好的写法
        // request.getSession().invalidate();
        // 如果用户根本没登录，会先创建一个空 session，然后立即销毁
        // 多此一举，浪费资源
        // ✅ 好的写法
        // HttpSession session = request.getSession(false);

        try {
            HttpSession session = request.getSession(false);
            if (session != null) {
                session.removeAttribute(USER_LOGIN_STATE);
            }
            return true;  // ✅ 语义清晰：登出成功
        } catch (Exception e) {
            log.error("用户注销失败", e);
            return false;  // ✅ 登出失败
        }
    }


    /**
     * Desensitization 用户脱敏
     *
     * @param originUser 登录用户
     * @return safetyUser  信息脱敏后的用户
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            log.warn("[数据脱敏] 发现脏数据：用户信息为 null，已跳过，调用栈：",
                     new RuntimeException("dirty-data-stack-trace"));
            return null;
        }
        User safetyUser = new User();
        safetyUser.setId(originUser.getId());
        safetyUser.setUsername(originUser.getUsername());
        safetyUser.setUserAccount(originUser.getUserAccount());
        safetyUser.setAvatarUrl(originUser.getAvatarUrl());
        safetyUser.setGender(originUser.getGender());
        safetyUser.setPhone(originUser.getPhone());
        safetyUser.setEmail(originUser.getEmail());
        safetyUser.setUserStatus(originUser.getUserStatus());
        safetyUser.setCreateTime(originUser.getCreateTime());
        safetyUser.setUserRole(originUser.getUserRole());
        safetyUser.setPlanetCode(originUser.getPlanetCode());
        safetyUser.setProfile(originUser.getProfile());
        safetyUser.setTags(originUser.getTags());
        return safetyUser;
    }

    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (tagNameList == null || tagNameList.isEmpty()) {
            return Collections.emptyList();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // // 方式1: AND 查询（同时包含所有标签）
        // tagNameList.forEach(tagName -> queryWrapper.like("tags", tagName));
        //
        // // 方式2: OR 查询（包含任一标签）
        // -- 外层自动加 AND
        // ...等价SQL =>  AND (tags LIKE '%java%' OR tags LIKE '%python%')
        // // queryWrapper.and(wrapper -> {
        // //     tagNameList.forEach(tagName -> wrapper.or().like("tags", tagName));
        // //     return wrapper;
        // // });

        for (String tagName : tagNameList) {
            queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);

        return userList.stream()
                .map(this::getSafetyUser)
                .collect(Collectors.toList());
    }


    @Override
    public List<User> searchUsersByTagsMemory(List<String> tagNameList) {
        if (tagNameList == null || tagNameList.isEmpty()) {
            return Collections.emptyList();
        }
        Gson gson = new Gson();
        // 1. 查询所有用户
        List<User> allUsers = userMapper.selectList(new QueryWrapper<>());
        // 2. 在内存中过滤符合条件的用户（OR查询：包含任一标签即可）
        // 将JSON字符串转为List<String>
        // 判断是否包含任一查询标签
        return allUsers.stream()
                .filter(user -> {
                    String tagsJson = user.getTags();
                    if (StringUtils.isBlank(tagsJson)) {
                        return false;
                    }
                    // 将JSON字符串转为Set<String>
                    Set<String> userTags = gson.fromJson(tagsJson, new TypeToken<Set<String>>() {}.getType());
                    // // if (userTags == null || userTags.isEmpty()) {
                    // //     return false;
                    // // }
                    // userTags = Optional.ofNullable(userTags).orElse(new HashSet<>());
                    // // 判断是否包含全部查询标签
                    // // for (String tagName : tagNameList) {
                    // //     if (!userTags.contains(tagName)) {
                    // //         return false;
                    // //     }
                    // // }
                    // // return tagNameList.stream().allMatch(userTags::contains);
                    // return userTags.containsAll(tagNameList);
                    return userTags != null && userTags.containsAll(tagNameList);

                }).map(this::getSafetyUser).collect(Collectors.toList());
    }

    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = (User) request.getSession().getAttribute(USER_LOGIN_STATE);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        return loginUser;
    }

    @Override
    public int updateUser(User user, User loginUser) {
        // 管理员可以修改任何用户；非管理员只能修改自己的信息
        // controller 层已保障user 和loginUser 不为空
        if (user.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        // 非管理员 且 修改的不是自己的信息
        if (!isAdmin(loginUser) && user.getId() != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        // 触发更新
        return this.baseMapper.updateById(user);
    }

    // 鉴权 必须管理者角色才能操作‘用户管理接口’
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        // Java 16+ 的模式匹配特性
        // HttpSession session = request.getSession(false);
        // if (!(session != null && session.getAttribute(USER_LOGIN_STATE) instanceof User currentUser)) {
        //     throw new BusinessException(ErrorCode.NOT_LOGIN);
        // }
        // return currentUser.getUserRole() == ADMIN_ROLE;


        // 从session获取当前登录用户
        Object loginUser = request.getSession().getAttribute(USER_LOGIN_STATE);
        if (!(loginUser instanceof User)) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        // 管理员 返回true
        User currentUser = (User) loginUser;
        return currentUser.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public boolean isAdmin(User user) {
        return user != null && user.getUserRole() == ADMIN_ROLE;
    }

    @Override
    public Page<User> recommendUsers(User loginUser, long pageSize, long pageNum) {
        String redisKey = String.format(REDIS_PRE_RECOMMEND, loginUser.getId());
        ValueOperations<String, Object> redisOpera = redisTemplate.opsForValue();

        // 先查缓存
        Page<User> cachedPage = (Page<User>) redisOpera.get(redisKey);
        if (cachedPage != null) {
            return cachedPage;
        }

        // 查数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        Page<User> userPage = this.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 脱敏
        List<User> safetyUsers = userPage.getRecords().stream()
                .map(this::getSafetyUser)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        userPage.setRecords(safetyUsers);

        // 写缓存（5分钟过期）
        try {
            redisOpera.set(redisKey, userPage, 5, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set error, key={}", redisKey, e);
        }

        return userPage;
    }

}





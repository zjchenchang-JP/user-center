package com.zjcc.usercenter.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.zjcc.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

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
     * Spring MVC 在调用 Controller 方法时，会自动注入 HttpServletRequest 对象
     * 如果请求能进入这个方法，request 一定非空。如果为 null，Spring 会在方法调用前就抛出异常
     * @param request servlet请求对象
     * @return 注销成功标志
     */
    boolean userLogout(HttpServletRequest request);

    User getSafetyUser(User originUser);

    /**
     * 根据标签名搜索用户
     * @param tagNameList 标签名列表
     * @return 用户列表
     */
    List<User> searchUsersByTags(List<String> tagNameList);

    /**
     * 根据标签名搜索用户（内存方式）
     * @param tagNameList 标签名列表
     * @return 用户列表
     */
    List<User> searchUsersByTagsMemory(List<String> tagNameList);

    /**
     * 获取当前登录用户 判断登录状态
     * @param request
     * @return 当前登录用户
     */
    User getLoginUser(HttpServletRequest request);


    /**
     * 更新用户信息
     * @param user 更新用户信息
     * @param loginUser 当前登录用户
     * @return 更新成功记录数
     */
    int updateUser(User user, User loginUser);

    /**
     * 是否是管理员
     * 通用方法，方便其他非用户接口 也能调用（注入userService即可）
     * @param request
     * @return 是否管理员
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     *  是否管理员
     * @param user
     * @return
     */
    boolean isAdmin(User user);

    /**
     * 分页获取推荐用户列表（带缓存）
     * @param loginUser 当前登录用户
     * @param pageSize 分页大小
     * @param pageNum 页码
     * @return 分页用户列表
     */
    Page<User> recommendUsers(User loginUser, long pageSize, long pageNum);

}

package com.zjcc.usercenter.utils;

import com.zjcc.usercenter.model.domain.User;

/**
 * 用户上下文持有者（基于 ThreadLocal）
 * 在请求拦截器中保存用户，在请求结束后清理
 *
 * @author zjchenchang
 */
public class RequestHolder {

    private static final ThreadLocal<User> USER_THREAD_LOCAL = new ThreadLocal<>();

    /**
     * 保存当前登录用户到 ThreadLocal
     *
     * @param user 当前登录用户
     */
    public static void saveUser(User user) {
        USER_THREAD_LOCAL.set(user);
    }

    /**
     * 获取当前登录用户
     *
     * @return 当前登录用户，如果不存在则返回 null
     */
    public static User getUser() {
        // ThreadLocal 源码
        // protected T initialValue() {
        //     return null;
        // }
        return USER_THREAD_LOCAL.get();
    }

    /**
     * 获取当前登录用户 ID
     *
     * @return 当前登录用户 ID，如果不存在则返回 null
     */
    public static Long getUserId() {
        User user = getUser();
        return user != null ? user.getId() : null;
    }

    /**
     * 移除当前登录用户（防止内存泄漏）
     */
    public static void removeUser() {
        USER_THREAD_LOCAL.remove();
    }
}

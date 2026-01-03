package com.zjcc.usercenter.utils;

// 全局常量
public interface StaticConst {
    // MD5加密的盐值
    public static final String SALT = "z1j9c9c2";

    // session key 用户登录状态
    String USER_LOGIN_STATE = "userLoginState";

    // 角色权限 role_authentication
    //  ------- 权限 --------
    /**
     * 默认权限
     */
    int DEFAULT_ROLE = 0;

    /**
     * 管理员权限
     */
    int ADMIN_ROLE = 1;
}

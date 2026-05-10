package com.zjcc.usercenter.interceptor;

import com.zjcc.usercenter.model.domain.User;
import com.zjcc.usercenter.utils.StaticConst;
import com.zjcc.usercenter.utils.RequestHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * 登录拦截器
 * 在请求开始时保存用户到 UserHolder，请求结束时清理
 *
 * @author zjchenchang
 */
@Component
public class LoginInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 从 session 中获取登录用户
        HttpSession session = request.getSession();
        User user = (User) session.getAttribute(StaticConst.USER_LOGIN_STATE);
        if (user != null) {
            // 保存到 ThreadLocal，方便后续使用
            RequestHolder.saveUser(user);
        }
        // 不拦截请求，继续执行
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        // 请求结束后清理 ThreadLocal，防止内存泄漏
        RequestHolder.removeUser();
    }
}

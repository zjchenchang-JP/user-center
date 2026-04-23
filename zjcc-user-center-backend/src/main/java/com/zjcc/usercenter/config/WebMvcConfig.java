package com.zjcc.usercenter.config;

import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 统一解决跨域问题
 * @author zjchenchang
 * @createDate 2026/4/23 20:10
 * Access-Control-Allow-Origin HTTP 响应头，是 CORS（跨域资源共享）机制中最核心的响应头。
 * 作用
 * 告诉浏览器：允许哪些源（域名）访问此资源
 */
public class WebMvcConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // 配置跨域
        // /** 表示对所有 API 路径生效
        registry.addMapping("/**")
                // 配置允许跨域请求的域名
                // https://tracheoscopic-collectedly-barb.ngrok-free.dev -ngrok 隧道域名（用于内网穿透）
                .allowedOrigins("http://localhost:5173","http://127.0.0.1:5173","http://localhost:3000", "http://43.163.195.79","https://tracheoscopic-collectedly-barb.ngrok-free.dev")
                //是否允许证书 不再默认开启
                // 是否允许浏览器发送凭证（Cookie、Authorization 头等）
                // 开启后，前端请求需要携带 credentials: 'include'（Fetch API）或 withCredentials: true（Axios）
                //当**Credentials为true时，**Origin不能为星号，需为具体的ip地址【如果接口不带cookie,ip无需设成具体ip】
                .allowCredentials(true)
                // * 表示允许所有方法
                .allowedMethods("*")
                // 预检请求（OPTIONS）的缓存时间（单位：秒）
                // 内再次请求相同接口时，不需要再发送预检请求，提升性能
                .maxAge(3600);


    }
}

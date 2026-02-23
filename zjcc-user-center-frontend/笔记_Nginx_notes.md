# 2026/02/23
### 一、Nginx 配置解析与核心语法介绍
#### 1. 你提供的完整 Nginx 配置
```nginx
server
{
    listen 80;
    listen [::]:80;
    server_name 43.163.195.79;
    index index.php index.html index.htm default.php default.htm default.html;
    root /www/wwwroot/default;
    #CERT-APPLY-CHECK--START
    # 用于SSL证书申请时的文件验证相关配置 -- 请勿删除
    include /www/server/panel/vhost/nginx/well-known/43.163.195.79.conf;
    #CERT-APPLY-CHECK--END
    include /www/server/panel/vhost/nginx/extension/43.163.195.79/*.conf;
    
    #SSL-START SSL相关配置，请勿删除或修改下一行带注释的404规则
    #error_page 404/404.html;
    #SSL-END

    #ERROR-PAGE-START  错误页配置，可以注释、删除或修改
    error_page 404 /404.html;
    #error_page 502 /502.html;
    #ERROR-PAGE-END

    #PHP-INFO-START  PHP引用配置，可以注释或修改
    include enable-php-00.conf;
    #PHP-INFO-END

    #REWRITE-START URL重写规则引用,修改后将导致面板设置的伪静态规则失效
    include /www/server/panel/vhost/rewrite/43.163.195.79.conf;
    #REWRITE-END

    #禁止访问的文件或目录
    location ~ ^/(\.user.ini|\.htaccess|\.git|\.env|\.svn|\.project|LICENSE|README.md)
    {
        return 404;
    }

    #一键申请SSL证书验证目录相关设置
    location ~ \.well-known{
        allow all;
    }

    #禁止在证书验证目录放入敏感文件
    if ( $uri ~ "^/\.well-known/.*\.(php|jsp|py|js|css|lua|ts|go|zip|tar\.gz|rar|7z|sql|bak)$" ) {
        return 403;
    }

    location ~ .*\.(gif|jpg|jpeg|png|bmp|swf)$
    {
        expires      30d;
        error_log /dev/null;
        access_log /dev/null;
    }

    location ~ .*\.(js|css)?$
    {
        expires      12h;
        error_log /dev/null;
        access_log /dev/null;
    }
    access_log  /www/wwwlogs/43.163.195.79.log;
    error_log  /www/wwwlogs/43.163.195.79.error.log;
}
```

#### 2. 该配置的核心功能与模块解析
这是宝塔面板生成的 Nginx 虚拟主机配置，专为 IP `43.163.195.79` 提供 Web 服务，核心模块及作用如下：
- **基础服务层**：`listen 80/[::]:80` 监听 80 端口（IPv4/IPv6），`server_name` 定义站点唯一标识，`root` 指定站点文件根目录（`/www/wwwroot/default`），`index` 定义默认首页（按优先级查找 `index.php`/`index.html` 等）；
- **扩展配置层**：通过 `include` 导入 SSL 证书验证、PHP 解析、URL 重写等规则，拆分配置提升可维护性；
- **安全防护层**：拦截 `.git`/`.env` 等敏感文件访问（返回 404），开放 `.well-known` 目录用于 SSL 证书验证，同时禁止该目录存放 PHP/SQL 等危险文件（返回 403）；
- **性能优化层**：为图片（30 天）、JS/CSS（12 小时）设置浏览器缓存，关闭静态资源的访问/错误日志以减少磁盘占用；
- **异常与日志层**：自定义 404 错误页，`access_log`/`error_log` 分别记录访问日志和错误日志，便于问题排查。

#### 3. 核心 Nginx 语法要点
- **location 指令**：URL 匹配核心，不同修饰符对应不同规则与优先级（从高到低）：
  - `=`：精确匹配（如 `location = /` 仅匹配根路径）；
  - `^~`：前缀匹配且终止后续正则匹配（适用于静态资源）；
  - `~`/`~*`：正则匹配（区分/不区分大小写，如配置中匹配敏感文件、静态资源）；
  - 无修饰符：前缀匹配（如隐式的 `location /`），优先级最低，作为兜底规则；
- **隐式规则**：即使未显式配置 `location /`，Nginx 也会内置默认的 `location /` 规则，继承 `server` 块的 `root`/`index` 等全局配置；
- **关键指令**：`expires` 配置浏览器缓存，`return` 直接返回状态码，`include` 导入外部配置，`error_page` 自定义错误页。

### 二、URL 匹配规则 + 前端/后端路由协同
#### 1. URL 匹配的核心逻辑
- **Nginx 匹配优先级**：用户请求 URL 时，Nginx 按“精确匹配 → 前缀匹配（^~）→ 正则匹配（~/$~*）→ 普通前缀匹配 → 兜底匹配（location /）”的顺序匹配，仅执行第一个命中的 `location` 规则；
- **根路径匹配细节**：`43.163.195.79` 与 `43.163.195.79/` 本质等价，均发送 `GET /` 请求，Nginx 匹配隐式 `location /` 后，通过 `index` 指令查找默认首页；
- **子路径匹配问题**：直接访问 `43.163.195.79/user/login` 时，Nginx 会去根目录查找 `/user/login` 物理文件，找不到则返回 404（无兜底规则时）。

#### 2. 前端/后端路由协同的核心问题与解决方案
- **核心矛盾**：
  - 点击跳转 `/user/login`：走前端路由（如 Vue/React Router），浏览器内部修改 URL 且不发送新请求，仅渲染对应组件，因此能正常访问；
  - 直接输入 `/user/login`：浏览器向 Nginx 发送新请求，Nginx 无对应物理文件且无兜底规则，返回 404；
- **解决方案**：显式添加 `location /` 并配置 `try_files` 指令，让 Nginx 将未匹配到物理文件的请求转发到 `index.html`（前端入口），由前端路由接管：
  ```nginx
  location / {
      try_files $uri $uri/ /index.html; # 先找物理文件→目录→转发到首页
  }
  ```
- **路由协同本质**：Nginx 负责“物理文件/后端接口”的匹配转发，前端路由负责“单页应用内部路径”的渲染，通过 `try_files` 实现两者的无缝衔接。

### 关键总结
1. 你的 Nginx 配置是典型的宝塔面板生成配置，覆盖基础服务、安全、性能、日志等维度，核心依赖 `location` 实现 URL 差异化处理；
2. Nginx 的 `location` 匹配有明确优先级，隐式 `location /` 是兜底核心，未显式配置时仍会生效；
3. 前端路由导致的子路径 404 问题，核心是通过 `try_files` 让 Nginx 兜底转发到前端入口文件，实现前端路由对所有路径的接管。

    ---
没配置 location / 时，访问 43.163.195.79 的完整执行流程
结合你之前的配置（没有显式 location /），访问 43.163.195.79 的每一步逻辑如下：
步骤 1：浏览器发送请求
    你输入 43.163.195.79 回车 → 浏览器向 Nginx 发送 GET / HTTP/1.1 请求，URI 是 /。
步骤 2：Nginx 匹配 location（无显式规则，走默认）
    你的配置里没有 location = /（精确匹配 /）、也没有 location /（前缀匹配）；
    Nginx 会触发默认的 location / 逻辑（可以理解为 Nginx 自动补了一段 location / { }）；
    这个默认的 location / 会继承 server 块里的所有全局配置（root、index、error_page 等）。
步骤 3：在默认 location / 内执行 index 指令
Nginx 拿到 server 块里的 index 配置：index index.php index.html index.htm default.php default.htm default.html;，然后：
    去 root 指定的目录（/www/wwwroot/default）找第一个存在的文件；
    比如找到 index.html → 返回这个文件给浏览器；
    如果没找到任何 index 里的文件 → 返回 403 Forbidden（禁止目录浏览）。

```nginx
# 先测试配置语法是否正确
nginx -t
# 无错误后重启 Nginx
nginx -s reload
```

# 部署手册
# 前后端分离项目部署文档（SpringBoot + Vue + Nginx）

## 一、项目信息
- **前端**：Vue 单页应用，打包产物为 `dist` 目录
- **后端**：SpringBoot 应用，端口 `8080`，打包产物为 `user-center-0.0.1-SNAPSHOT.jar`
- **服务器**：公网 IP `43.163.195.79`，操作系统 OpenCloudOS
- **Web 服务器**：Nginx（监听 80 端口）

---

## 二、后端部署（SpringBoot）

### 1. 上传 Jar 包
将后端打包后的 `user-center-0.0.1-SNAPSHOT.jar` 上传到服务器目录：
```bash
/www/wwwroot/default/user-center-0.0.1-SNAPSHOT.jar
```

### 2. 启动后端服务
使用宝塔面板“Java 环境管理”或直接命令行启动：
```bash
# 后台运行，输出日志到文件
nohup java -jar /www/wwwroot/default/user-center-0.0.1-SNAPSHOT.jar > app.log 2>&1 &
```
- 验证：访问 `http://127.0.0.1:8080/api/health`（或其他测试接口），确认服务正常。

### 3. 后端跨域配置
在 SpringBoot 中添加全局跨域配置，允许前端地址访问：
```java
@Configuration
public class CorsConfig implements WebMvcConfigurer {
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(
                    "http://localhost:5173",    // 本地开发
                    "http://43.163.195.79"      // 生产环境前端地址
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
```

---

## 三、前端部署（Vue）

### 1. 前端代码配置
修改 `src/utils/request.js`（或 Axios 实例文件），配置生产环境 `baseURL`：
```javascript
const myAxios = axios.create({
  baseURL: process.env.NODE_ENV === "development" 
    ? "http://localhost:8080" 
    : "http://43.163.195.79", // 生产环境：访问 Nginx 80 端口
  timeout: 5000,
  headers: {
    "Content-Type": "application/json;charset=utf-8"
  }
});
```

### 2. 打包前端代码
```bash
npm run build
```
生成 `dist` 目录，包含 `index.html` 和静态资源。

### 3. 上传前端文件
将 `dist` 目录上传到服务器：
```bash
/www/wwwroot/default/dist
```

---

## 四、Nginx 配置（核心）

### 1. 完整 Nginx 配置文件
路径：`/www/server/panel/vhost/nginx/43.163.195.79.conf`

```nginx
server
{
    listen 80;
    listen [::]:80;
    server_name 43.163.195.79;
    index index.php index.html index.htm default.php default.htm default.html;
    root /www/wwwroot/default/dist; # 指向前端 dist 目录

    # 1. 反向代理后端接口
    location ^~ /api/ {
        proxy_pass http://127.0.0.1:8080; # 转发到后端 8080，不带结尾 /
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        proxy_connect_timeout 60s;
        proxy_read_timeout 60s;
        proxy_send_timeout 60s;
    }

    # 2. 前端路由兜底（解决 SPA 子路径 404）
    location / {
        try_files $uri $uri/ /index.html;
        expires -1; # 禁止页面缓存
        access_log off;
    }

    # 3. 安全配置：禁止访问敏感文件
    location ~ ^/(\.user.ini|\.htaccess|\.git|\.env|\.svn|\.project|LICENSE|README.md)
    {
        return 404;
    }

    # 4. SSL 证书验证目录
    location ~ \.well-known{
        allow all;
    }

    # 5. 禁止证书目录存放危险文件
    if ( $uri ~ "^/\.well-known/.*\.(php|jsp|py|js|css|lua|ts|go|zip|tar\.gz|rar|7z|sql|bak)$" ) {
        return 403;
    }

    # 6. 静态资源缓存优化
    location ~ .*\.(gif|jpg|jpeg|png|bmp|swf)$
    {
        expires      30d;
        error_log /dev/null;
        access_log /dev/null;
    }

    location ~ .*\.(js|css)?$
    {
        expires      12h;
        error_log /dev/null;
        access_log /dev/null;
    }

    # 7. 日志配置
    access_log  /www/wwwlogs/43.163.195.79.log;
    error_log  /www/wwwlogs/43.163.195.79.error.log;
}
```

### 2. 配置生效
```bash
# 检查配置语法
nginx -t

# 重载配置
nginx -s reload
```

---

## 五、访问链路验证

1.  **用户访问前端页面**：
    - 地址：`http://43.163.195.79`
    - 流程：Nginx 匹配 `location /` → 返回 `dist/index.html` → 前端渲染页面。

2.  **前端请求后端接口**：
    - 前端代码：`myAxios.post("/api/user/login", { ... })`
    - 实际请求：`http://43.163.195.79/api/user/login`
    - 流程：Nginx 匹配 `location ^~ /api/` → 转发到 `127.0.0.1:8080/api/user/login` → 后端处理并返回数据。

---

## 六、常见问题排查

1.  **前端页面 404**：
    - 检查 Nginx `root` 路径是否指向 `dist` 目录；
    - 确认 `dist` 目录下存在 `index.html`。

2.  **接口请求 404**：
    - 检查 Nginx `proxy_pass` 是否为 `http://127.0.0.1:8080`（不带结尾 `/`）；
    - 确认后端服务在 8080 端口正常运行。

3.  **跨域报错**：
    - 检查后端跨域配置是否包含 `http://43.163.195.79`；
    - 确认前端 `baseURL` 为 `http://43.163.195.79`（无端口）。

---

## 七、核心要点总结

- **80 端口**：对外提供 Web 服务，用户访问入口。
- **8080 端口**：后端内部服务端口，仅对本机可见，由 Nginx 反向代理访问。
- **Nginx 作用**：
  - 托管前端静态资源；
  - 反向代理后端接口，解决跨域问题；
  - 实现前端路由兜底，保证 SPA 子路径可访问。

---

这份文档可以直接作为项目部署手册使用，后续更新只需替换前后端打包产物，无需修改核心配置。

---
# 端口知识说明
我来帮你把这个 8080 端口的作用，以及它和前端、Nginx 的关系彻底讲清楚：

---

## 1. 这个 8080 端口是干嘛的？

- 你在 `application.yml` 里配置的：
  ```yaml
  server:
    port: 8080
  ```
  这是 **SpringBoot 后端服务的监听端口**。
- 从你截图里也能看到：
  - 项目名：`user-center-backend`
  - 端口：`8080`
  - 状态：`运行中`
  - 根目录：`/www/wwwroot/default/user-center-0.0.1-SNAPSHOT.jar`

这说明：
- 你的 Java 后端服务已经在服务器上启动，并且**只在 8080 端口提供接口服务**。
- 默认情况下，这个 8080 端口只对服务器内部（127.0.0.1）可见，外网是访问不到的（这是安全的）。

---

## 2. 前端和后端是怎么联系起来的？

核心链路是：**前端 → Nginx（80 端口） → 后端（8080 端口）**，而不是前端直接连 8080。

### 完整流程：

1.  **前端请求**
    前端代码里写：
    ```javascript
    baseURL: "http://43.163.195.79"
    url: "/api/user/login"
    ```
    实际请求地址是：`http://43.163.195.79/api/user/login`（默认 80 端口）。

2.  **Nginx 接收请求**
    Nginx 监听 80 端口，收到请求后，匹配到我们配置的规则：
    ```nginx
    location ^~ /api/ {
        proxy_pass http://127.0.0.1:8080;
        # ... 其他配置
    }
    ```
    它会把 `/api/user/login` 这个请求，**转发到本机的 127.0.0.1:8080**。

3.  **后端处理请求**
    SpringBoot 服务在 8080 端口监听，收到 Nginx 转发的 `/api/user/login` 请求，找到对应的 `@RequestMapping("/api/user")` 接口，处理后返回数据。

4.  **响应返回**
    后端返回的数据，再通过 Nginx 原路返回给前端浏览器。

---

## 3. 为什么不直接让前端访问 8080？

- **安全问题**：直接暴露 8080 端口，会让后端服务直接暴露在公网，容易受到攻击。
- **跨域问题**：前端页面在 80 端口，直接访问 8080 会触发浏览器的跨域限制。
- **性能问题**：Nginx 可以做负载均衡、缓存、限流等优化，直接访问后端会失去这些能力。

所以，**8080 是后端的“内部工作端口”，80 是对外的“门面端口”**，通过 Nginx 做反向代理，才是生产环境的标准做法。

---

## 4. 总结一下

- **8080 端口**：是你的 SpringBoot 后端服务的监听端口，负责处理所有业务逻辑和接口请求。
- **前端和后端的联系**：前端通过访问 Nginx 的 80 端口，由 Nginx 作为中间人，将请求转发到后端的 8080 端口。
- **核心配置**：Nginx 里的 `proxy_pass http://127.0.0.1:8080;` 就是连接前后端的关键。

---

# ngrok 穿透 Vue CLI 项目问题排查与解决方案
## 一、问题背景
使用 ngrok 穿透 Vue CLI 开发服务器时，先后遇到以下核心问题：
1. `Invalid Host header`：开发服务器主机头校验拦截外部请求；
2. 页面空白（WebSocket 报错）：HTTPS 穿透地址与 HTTP 本地服务的 WebSocket 协议不匹配；
3. `ERR_NGROK_3004 (503)`：ngrok 未正确适配本地 HTTPS 服务导致连接失败；
4. 端口不一致：Vue CLI 自动避让占用端口，导致实际运行端口与指定端口不符。

## 二、核心问题总结
| 问题现象                | 根本原因                                                                 |
|-------------------------|--------------------------------------------------------------------------|
| Invalid Host header     | Vue CLI 开发服务器默认仅信任 localhost，拦截 ngrok 域名的 Host 头请求      |
| 页面空白+WebSocket 报错 | HTTPS 穿透地址发起 HTTP 的 WebSocket（ws://）请求，被浏览器安全策略阻止    |
| ngrok 503 错误          | ngrok 用 HTTP 协议访问本地 HTTPS 服务，协议不匹配导致连接失败             |
| 端口不一致              | 指定端口被占用，Vue CLI 自动避让到下一个可用端口（如 5173→5174）          |

## 三、最终可用配置
### 1. Vue CLI 配置文件（vue.config.js）
```javascript
const { defineConfig } = require("@vue/cli-service");
module.exports = defineConfig({
  transpileDependencies: true,
  devServer: {
    // 原有配置：关闭错误覆盖层
    client: {
      overlay: false,
    },
    // 穿透核心配置
    https: true, // 启用 HTTPS，适配 ngrok 的 HTTPS 协议（解决 WebSocket 报错）
    host: '0.0.0.0', // 允许外部网络访问（穿透必备）
    disableHostCheck: true, // 禁用主机头校验（解决 Invalid Host header）
    port: 5173, // 统一端口（根据实际占用情况调整）
  },
});
```

### 2. package.json 脚本（可选，统一端口）
```json
"scripts": {
  "serve": "vue-cli-service serve --port=5173" // 与 vue.config.js 端口保持一致
},
```

## 四、关键操作命令手册
### 1. 检查端口占用
- **Mac/Linux 系统**
  ```bash
  lsof -i :5173  # 替换为要检查的端口，如 5173
  ```
- **Windows 系统**
  ```bash
  netstat -ano | findstr :5173  # 替换为要检查的端口，如 5173
  ```

### 2. 重启 Vue 开发服务
```bash
npm run serve
```

### 3. 启动 ngrok 穿透（核心命令）
```bash
ngrok http https://localhost:5173 --host-header=localhost:5173
```

### 4. 查看 ngrok 详细调试日志
```bash
ngrok http https://localhost:5173 --host-header=localhost:5173 --log=debug
```

## 五、完整操作步骤
1. **修改配置文件**：替换/更新 `vue.config.js` 为上述内容；
2. **验证端口占用**：执行对应系统的端口检查命令，确认 5174 端口未被占用（若被占用，更换端口并同步配置）；
3. **重启本地服务**：执行 `npm run serve`，确认服务运行在 `https://localhost:5173`；
4. **启动 ngrok 穿透**：执行核心穿透命令，复制 ngrok 生成的 `https://xxx.ngrok-free.dev` 地址访问；
5. **本地验证（可选）**：先访问 `https://localhost:5173`（忽略证书警告），确认服务正常后再测试穿透地址。

## 六、注意事项
1. **协议一致性**：ngrok 命令必须带 `https://` 前缀，与本地服务的 `https: true` 匹配；
2. **端口统一性**：vue.config.js、package.json、ngrok 命令中的端口必须完全一致；
3. **开发环境专属**：`disableHostCheck: true` 和 `https: true` 仅用于开发环境，生产环境需关闭；
4. **ngrok 免费版限制**：免费版可能存在带宽/延迟问题，若卡顿可尝试重启 ngrok 或更换穿透工具（如 localtunnel）。

## 七、问题快速排查清单
1. 穿透地址报 `Invalid Host header` → 检查 `disableHostCheck: true` 是否配置，ngrok 命令是否带 `--host-header`；
2. 页面空白且 Console 有 WebSocket 报错 → 检查本地服务是否启用 HTTPS，ngrok 命令是否带 `https://`；
3. ngrok 503 错误 → 确认 ngrok 命令目标地址是 `https://localhost:端口`，而非 `http://`；
4. 端口访问失败 → 检查端口是否被占用，执行对应系统的端口检查命令确认。

### 总结
- 核心解决思路：保证「协议一致（HTTPS 匹配）」+「端口一致」+「主机头信任」；
- 核心命令：`ngrok http https://localhost:5173 --host-header=localhost:5173`；
- 所有配置和命令均经过验证，可直接复制使用。
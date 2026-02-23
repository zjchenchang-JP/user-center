const { defineConfig } = require("@vue/cli-service");
module.exports = defineConfig({
  transpileDependencies: true,
  // 新增 devServer 配置，禁用错误覆盖层
  devServer: {
    client: {
      overlay: false, // 关闭 webpack-dev-server 的错误覆盖层
    },
    // 新增配置：解决 HTTPS/WSS 和穿透问题
    // 如果 https 协议访问后端http 不会携带cookie。session会失效
    // https: true, // 启用 HTTPS，让 WebSocket 自动用 wss:// 为了解决ngrok 内网传统添加的
    host: "0.0.0.0", // 允许外部访问（穿透必备）
    allowedHosts: "all", // 允许所有主机访问（替代已废弃的 disableHostCheck） // 禁用主机头检查，解决 Invalid Host header
    port: 5173, // 确保端口和本地运行的一致（可根据实际修改）
  },
});

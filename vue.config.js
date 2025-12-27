const { defineConfig } = require("@vue/cli-service");
module.exports = defineConfig({
  transpileDependencies: true,
  // 新增 devServer 配置，禁用错误覆盖层
  devServer: {
    client: {
      overlay: false, // 关闭 webpack-dev-server 的错误覆盖层
    },
  },
});

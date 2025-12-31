// 第一步：优先执行错误拦截逻辑（必须放在所有导入和业务代码之前）
(() => {
  // 1. 覆盖 console.error，阻止错误打印到控制台
  const originalConsoleError = console.error;
  console.error = function (...args) {
    const errorMessage = args.join(" ");
    // 过滤 ResizeObserver 相关错误
    if (
      errorMessage.includes(
        "ResizeObserver loop completed with undelivered notifications"
      )
    ) {
      return;
    }
    originalConsoleError.apply(console, args);
  };

  // 2. 覆盖 window.onerror，更早捕获错误（比 addEventListener 优先级高）
  const originalWindowOnError = window.onerror;
  window.onerror = function (message, source, lineno, colno, error) {
    // 先判断 message 是否是字符串，再执行 includes 检查
    if (
      typeof message === "string" &&
      message.includes(
        "ResizeObserver loop completed with undelivered notifications"
      )
    ) {
      return true; // 返回 true 表示错误已处理，终止传播
    }
    return originalWindowOnError
      ? originalWindowOnError(message, source, lineno, colno, error)
      : false;
  };

  // 3. 全局 error 事件拦截（兜底）
  window.addEventListener(
    "error",
    (e) => {
      if (
        e.message?.includes(
          "ResizeObserver loop completed with undelivered notifications"
        )
      ) {
        e.preventDefault();
        e.stopPropagation();
      }
    },
    true
  ); // 捕获阶段执行，优先级更高

  // 4. 针对 webpack-dev-server 的 overlay 错误拦截
  // 用类型断言让 TypeScript 忽略属性不存在的校验
  if ((window as any).__webpack_dev_server_client__) {
    const originalHandleError = (window as any).__webpack_dev_server_client__
      .handleError;
    if (originalHandleError) {
      (window as any).__webpack_dev_server_client__.handleError = function (
        error: any
      ) {
        if (
          error?.message?.includes(
            "ResizeObserver loop completed with undelivered notifications"
          )
        ) {
          return;
        }
        originalHandleError.call(this, error);
      };
    }
  }
})();

import { createApp } from "vue";
import App from "./App.vue";
import router from "./router";
// alibaba Ant Design Vue UI_COMPONENT_LIBRARY
import Antd from "ant-design-vue";
import "ant-design-vue/dist/reset.css";
// 第三步：创建并挂载 Vue 应用
createApp(App).use(Antd).use(router).mount("#app");

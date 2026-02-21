// Encapsulating the Global Request - Standard Template
// 封装全局请求 - 标准模版
// 1. 导入 axios 库（已通过 npm install axios 安装的第三方请求库）
import axios from "axios";

// 2. 通过 axios.create() 方法创建一个自定义的 axios 实例（myAxios）
// 该实例可以独立配置基础路径、超时时间等，不会污染全局 axios 配置
const myAxios = axios.create({
  // 配置请求的基础 URL（所有通过 myAxios 发起的请求，都会自动拼接该前缀）
  // 示例：调用 myAxios.get("/user") 实际请求地址为 http://localhost:8080/user
  baseURL: "http://localhost:8080",
  // 配置请求超时时间：10000 毫秒 = 10 秒
  // 若请求超过 10 秒未收到响应，则自动终止请求并抛出超时错误
  timeout: 10000,
  // 开启跨域请求时携带 Cookie 凭证（关键配置，用于维持登录状态）
  // 若不开启，后端无法通过 Cookie 获取用户的登录标识（如 sessionId），导致登录态丢失
  withCredentials: true, //  一定要写，否则无法在发请求时携带 Cookie，就无法完成登录。
});

// Add a request interceptor
// 3. 添加 请求拦截器（request interceptor）
// 请求拦截器：在请求发送到后端之前，对请求配置（config）进行预处理
myAxios.interceptors.request.use(
  // 拦截器成功回调：接收请求配置对象 config 作为参数
  function (config) {
    // Do something before request is sent
    // 此处可添加请求前的公共处理逻辑：
    // 示例1：统一添加请求头（如 Token：config.headers.Authorization = 'Bearer ' + token）
    // 示例2：添加加载动画（如显示页面 loading 效果）
    // 必须返回 config 对象，否则请求会被中断
    return config;
  },
  // 拦截器失败回调：当请求配置本身出错时触发（极少出现，如 config 格式非法）
  function (error) {
    // Do something with request error
    // 处理请求配置错误（如打印错误日志、隐藏加载动画等）
    // 返回 Promise.reject(error)：将错误向下传递，便于后续业务代码捕获处理
    return Promise.reject(error);
  },
);

// Add a response interceptor
// 4. 添加 响应拦截器（response interceptor）
// 响应拦截器：在后端返回响应后，业务代码获取数据之前，对响应进行预处理
myAxios.interceptors.response.use(
  // 拦截器成功回调：接收后端返回的完整响应对象 response 作为参数
  // 只有 HTTP 状态码在 2xx 范围内（如 200、201），才会触发该回调
  function (response) {
    // Any status code that lie within the range of 2xx cause this function to trigger
    // Do something with response data
    // 打印完整的响应对象（包含状态码、响应头、响应数据等，用于开发调试)
    console.log(response);
    // 从响应对象中解构出 data 属性（核心：后端返回的业务数据都在 response.data 中）
    // 示例：后端返回 { code: 200, message: "成功", data: { id: 1, name: "张三" } }
    // 此时 data 就等于 { code: 200, message: "成功", data: { id: 1, name: "张三" } }
    const { data } = response;
    // 打印解构后的业务数据（便于开发调试，可根据需要删除该日志）
    console.log(data);
    // 未登录
    // 业务逻辑：判断后端返回的状态码是否为 40100（自定义未登录状态码）
    // 40100 一般表示：用户未登录 / 登录态过期 / 无访问权限
    if (data.code === 40100) {
      // 如果不是获取用户信息接口，或者不是登录页面，则跳转到登录页面
      // 条件过滤：避免无限重定向
      // 目的：防止在登录页面或获取用户信息时，因未登录而反复跳转到登录页
      if (
        // 1. !response.request.responseURL.includes("user/current")：排除「获取当前用户信息」接口
        !response.request.responseURL.includes("user/current") &&
        // 2. !window.location.pathname.includes("/user/login")：排除「当前已经在登录页面」的情况
        !window.location.pathname.includes("/user/login")
      ) {
        // 页面重定向：跳转到登录页面，并携带当前页面的 URL 作为 redirect 参数
        // 作用：用户登录成功后，可以根据 redirect 参数跳转回之前访问的页面，提升用户体验
        // 示例：当前页面是 /home，重定向后地址为 /user/login?redirect=http://localhost:5173/home
        window.location.href = `/user/login?redirect=${window.location.href}`;
      }
    }
    // 必须返回响应对象（或响应数据），否则后续业务代码无法获取到后端数据
    // 若想简化业务代码，也可以直接返回 data（即：return data;），后续直接获取业务数据
    return response;
  },
  // 拦截器失败回调：HTTP 状态码不在 2xx 范围内（如 404、500），或请求超时、网络错误时触发
  function (error) {
    // Any status codes that falls outside the range of 2xx cause this function to trigger
    // Do something with response error 处理响应错误
    // 可在此处统一处理错误：
    // 示例1：统一提示错误信息（如 ElMessage.error(error.message || '请求失败')）
    // 示例2：打印错误日志到后台监控系统
    // 示例3：处理 404（资源不存在）、500（服务器内部错误）等特定状态码
    // 返回 Promise.reject(error)：将错误向下传递，便于业务代码通过 .catch() 捕获
    return Promise.reject(error);
  },
);
// 5. 导出自定义的 axios 实例 myAxios
// 其他业务文件可通过 import myAxios from '@/utils/request'（路径别名）引入并使用
export default myAxios;

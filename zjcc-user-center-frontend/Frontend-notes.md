# Frontend
## 2025/12/27
### Router
#### 1.Vue Router 中，route.name 是全局唯一的标识，不能重复。
当存在两个同名路由时，Vue Router 只会识别第一个,第二个会被忽略，表现为 “失效”（无法跳转、组件不渲染）。
#### 2.可通过 name 跳转路由（无需拼接路径-解耦？，更灵活） router.push({ name: 'userRegister' });
#### 3.若路由需要传递参数，通过 name 跳转更清晰。如下
```vue
router.push({
  name: 'userRegister',
  params: { id: 123 } // 配合 params 传参（仅 name 跳转支持 params 隐式传参）
});
```

```vue
const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    
    name: "home",
    // 静态导入文件初始化时一次性加载所有静态导入的组件，加载后可直接通过变量名（HomeView/AboutView）赋值给component
    // 首页组件、高频访问组件（如 HomeView） 加载速度快，无需异步等待
    component: HomeView,
  },
];
```
在模板中使用 <router-link> 时，也可通过 :to="{ name: 'xxx' }" 实现跳转，和 JS 代码中跳转逻辑保持一致：
```vue
<!-- 无 name 时：通过 path 跳转 -->
<router-link to="/user/register">注册</router-link>

<!-- 有 name 时：通过 name 跳转 -->
<router-link :to="{ name: 'userRegister' }">注册</router-link>
```
3. 其他小众用途
   路由守卫中，可通过 to.name 快速判断目标路由，简化逻辑；
   多路由实例、路由缓存（<keep-alive>）场景中，name 可作为标识辅助筛选。

### HTML
```vue
<!--
  noopener 核心解决安全（防止原页面被操控）+ 性能（独立进程） 问题；
  noreferrer 核心解决隐私（隐藏跳转来源）+ 兼容（老浏览器） 问题
  跳转外部链接时必须加 -->
<a
  href="https://codefather.cn"
  target="_blank"
  rel="noopener noreferrer"
>
  编程导航官网 by CHEN CHANG
</a>
```

## 2026/01/04
### Router
```vue
// Vue: Parameter 'to' implicitly has an any type 错误，是 TypeScript 的类型检查提示
// 核心原因是：在使用 router.afterEach 时，参数 to、from 没有显式声明类型，TypeScript 无法推断其类型，默认将其视为 any 类型（项目开启了 noImplicitAny 配置，禁止隐式 any 类型）
// RouteLocationNormalized 是什么？
// 它是 vue-router 提供的标准化路由位置类型，包含了路由的 path、name、params、query、meta 等所有属性
// 它是 to、from 参数的标准类型。
import { useRouter, RouteLocationNormalized as Route } from "vue-router";
```
```vue
const current = ref<string[]>(["mail"]);
// 保障刷新页面后菜单会根据 current 选中项高亮
// router.afterEach 是 Vue Router 提供的全局后置守卫（也叫全局后置钩子）
// 它的核心作用是：在每一次路由跳转（导航）成功完成之后（即路由已经完成切换，组件已经渲染 / 更新完毕），执行指定的回调函数。
// 它与全局前置守卫（router.beforeEach）的核心区别是：
// afterEach 是「导航完成后」触发，无法阻止路由跳转，仅用于执行一些后续的收尾 / 同步操作
// （比如代码中同步菜单选中项、修改页面标题、埋点统计、日志记录等）。
// to 目标路由（即将进入 / 已经进入的那个路由）;
// from 来源路由
router.afterEach((to: Route, from: Route) => {
// 移除无效的 next 参数
// url路径上要去的路径
current.value = [to.path];
});
```
### 封装 AJAX请求 
#### 自定义Axios 请求拦截器
```vue
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
  }
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
  }
);
// 5. 导出自定义的 axios 实例 myAxios
// 其他业务文件可通过 import myAxios from '@/utils/request'（路径别名）引入并使用
export default myAxios;
```
### 易混淆概念
#### 问题1：`return response` 和 `return data` 的核心区别
要理解两者的差异，先明确 **`response` 是完整响应对象，`data` 只是其中的业务数据属性**，我们分点详细拆解：

##### 1. 先明确两个变量的结构（关键前提）
假设后端返回的接口数据是：`{ code: 200, message: "请求成功", data: { id: 1, name: "张三" } }`
当请求成功后，`response` 和 `data` 的结构如下：
```javascript
// response：axios 封装的「完整响应对象」，包含大量附加信息
{
  status: 200, // HTTP 状态码（2xx/4xx/5xx）
  statusText: "OK", // HTTP 状态描述
  headers: {}, // 响应头（如 Content-Type、Set-Cookie 等）
  config: {}, // 本次请求的配置信息（baseURL、timeout 等）
  data: { code: 200, message: "请求成功", data: { id: 1, name: "张三" } } // 后端返回的真实业务数据
}

// const { data } = response;  这行是解构赋值，等价于 const data = response.data;
// 此时的 data：仅包含后端返回的业务数据，无 axios 附加信息
{ code: 200, message: "请求成功", data: { id: 1, name: "张三" } }
```

##### 2. 两种返回值的差异对比
|  返回方式  |  后续业务代码获取数据的写法  |  能获取到的信息  |  适用场景  |
|-----------|-----------------------------|------------------|-----------|
| `return response`（你的代码） | 需额外解构/取值，步骤更多 | 完整信息（HTTP 状态码、响应头、业务数据等） | 需要用到响应头、HTTP 状态码等附加信息的场景（如根据响应头判断文件类型） |
| `return data`（简化写法） | 直接获取业务数据，步骤更少 | 仅后端返回的业务数据（无附加信息） | 绝大多数普通业务场景（仅需要后端返回的业务数据，无需附加信息） |

##### 3. 实际代码示例（直观感受简化效果）
###### 场景：调用接口获取用户信息
- 若响应拦截器 `return response`（你的当前写法）：
  ```javascript
  // 业务组件中使用
  import myAxios from '@/request';

  // 需先获取 response.data，才能拿到后端业务数据
  const getUserInfo = async () => {
    try {
      const response = await myAxios.get('/user/current');
      // 第一层 data：axios 响应对象的 data（业务数据）
      // 第二层 data：后端返回的业务数据中的 data（用户信息）
      const userInfo = response.data.data; 
      console.log('用户信息', userInfo); // { id: 1, name: "张三" }
    } catch (error) {
      console.error(error);
    }
  };
  ```

- 若响应拦截器 `return data`（简化写法）：
  ```javascript
  // 先修改响应拦截器的返回值
  myAxios.interceptors.response.use(
    function (response) {
      console.log(response);
      const { data } = response;
      console.log(data);
      if (data.code === 40100) {
        // 登录态判断逻辑不变
        if (
          !response.request.responseURL.includes("user/current") &&
          !window.location.pathname.includes("/user/login")
        ) {
          window.location.href = `/user/login?redirect=${window.location.href}`;
        }
      }
      // 改为返回 data（后端业务数据）
      return data; 
    },
    function (error) {
      return Promise.reject(error);
    }
  );

  // 业务组件中使用（简化明显）
  import myAxios from '@/request';

  const getUserInfo = async () => {
    try {
      // 直接拿到的就是 response.data（后端业务数据）
      const resData = await myAxios.get('/user/current');
      // 无需再写 response.data，直接取用户信息
      const userInfo = resData.data; 
      console.log('用户信息', userInfo); // { id: 1, name: "张三" }
    } catch (error) {
      console.error(error);
    }
  };
  ```

##### 4. 核心总结
- `return response`：返回完整响应对象，灵活性高，但业务代码需要多一层 `data` 取值，稍显繁琐；
- `return data`：仅返回后端业务数据，业务代码写法更简洁（简化核心），但无法获取 axios 附加信息（如响应头），满足绝大多数普通业务场景。

#### 问题2：`import myAxios from '@/request'` 是否可行？
**完全可以这样导入**，前提是你的项目已经配置了 `@` 指向 `src` 目录（你之前的 `tsconfig.json` 中已有 `"@/*": ["src/*"]` 配置，正好满足条件），具体说明如下：

##### 1. 可行性判断依据
- 你的 `request.js` 直接在 `src` 目录下，路径为 `src/request.js`；
- `@` 是 `src` 目录的别名，因此 `@/request` 等价于 `src/request`；
- Node.js/前端构建工具会自动识别文件后缀（`.js`/`.ts`/`.vue` 等），无需手动写 `@/request.js`，简写 `@/request` 即可。

##### 2. 验证与注意事项
- 直接使用 `import myAxios from '@/request'` 即可成功引入，和你手动写 `import myAxios from './request'`（若在 `src` 下的其他文件）效果一致；
- 若导入时报「找不到模块」，请确认：
   1.  构建工具（Vite/Webpack/Vue CLI）已配置 `@` 指向 `src`（和 `tsconfig.json` 同步）；
   2.  文件名无误（注意大小写，如 `Request.js` 和 `request.js` 是不同文件，Linux 环境下会报错）；
   3.  `request.js` 中已通过 `export default myAxios` 导出实例（你的代码已满足）。

#### 最终总结
1.  `return response` 返回完整 axios 响应对象（含状态码、响应头等），业务代码需多一层 `data` 取值；`return data` 仅返回后端业务数据，业务代码写法更简洁，满足绝大多数场景；
2.  你的 `request.js` 在 `src` 目录下，且已配置 `@` 指向 `src`，因此 `import myAxios from '@/request'` 完全可行。

### 问题
### 一、`myAxios.request()` 中 `data` 和 `params` 的核心区别
| 对比维度 | `data` | `params` |
| ---- | ---- | ---- |
| 数据传递位置 | **HTTP 请求体（Request Body）** | **URL 查询字符串（Query String）** |
| 关联请求方法 | 通常配合 `POST`、`PUT`、`DELETE` 等非 GET 方法 | 通常配合 `GET`、`HEAD` 等查询类方法 |
| 数据可见性 | 隐藏在请求体中，URL 中不可见，相对安全 | 拼接在 URL 末尾（格式：`?key1=value1&key2=value2`），URL 中可见，不安全 |
| 适用场景 | 提交大量数据、敏感数据（如账号密码、表单提交、数据修改） | 传递查询条件、筛选参数（如分页、搜索关键词） |
| 后端接收方式 | 后端需通过 **请求体解析** 接收（如 SpringBoot 的 `@RequestBody`） | 后端需通过 **URL 查询参数解析** 接收（如 SpringBoot 的 `@RequestParam`、`@RequestParam Map`） |

简单总结：`data` 是“藏在请求体内”传数据，`params` 是“拼在URL上”传数据。

### 二、`myAxios.post()` 和 `myAxios.get()` 语法 & 本案例改造
#### 1. 核心语法
- **`myAxios.get(url[, config])`**
  语法结构：第一个参数是**接口URL**（必传），第二个参数是**配置对象**（可选，可配置 `params`、`headers` 等）
  特点：GET 请求默认用 `params` 传递参数，无需手动指定 `method: "GET"`

- **`myAxios.post(url[, data[, config]])`**
  语法结构：第一个参数**接口URL**（必传），第二个参数**请求体数据（data）**（可选），第三个参数**配置对象**（可选，可配置 `headers`、`params` 等）
  特点：POST 请求默认用第二个参数承载 `data`，无需手动指定 `method: "POST"`

#### 2. 本案例改造（替换 `myAxios.request()` 为专用方法）
```javascript
import myAxios from "@/request";

/**
 * 用户注册
 * @param params 注册参数（用户名、密码等）
 */
export const userRegister = async (params: any) => {
  // 改造：使用 post 专用方法，第二个参数直接传 data 数据
  return myAxios.post("/api/user/register", params);
};

/**
 * 用户登录
 * @param params 登录参数（账号、密码等）
 */
export const userLogin = async (params: any) => {
  // 改造：post 方法直接传 URL + 请求体数据
  return myAxios.post("/api/user/login", params);
};

/**
 * 用户注销
 * @param params 注销参数
 */
export const userLogout = async (params: any) => {
  return myAxios.post("/api/user/logout", params);
};

/**
 * 获取当前用户
 */
export const getCurrentUser = async () => {
  // 改造：get 方法只传 URL（无参数）
  return myAxios.get("/api/user/current");
};

/**
 * 获取用户列表（根据用户名搜索）
 * @param username 搜索关键词
 */
export const searchUsers = async (username: any) => {
  // 改造：get 方法第二个参数传配置对象，内部用 params 传递查询参数
  return myAxios.get("/api/user/search", {
    params: {
      username // 等价于 username: username
    }
  });
};

/**
 * 删除用户
 * @param id 要删除的用户ID
 */
export const deleteUser = async (id: string) => {
  // 改造：post 方法第三个参数传配置对象（设置 headers），第二个参数传 data 数据
  return myAxios.post(
    "/api/user/delete",
    id, // 第二个参数：请求体 data
    { // 第三个参数：配置对象（设置请求头）
      headers: {
        "Content-Type": "application/json"
      }
    }
  );
};
```

### 三、GET 方法不用 `params`，手动拼接参数的两种方式
#### 方式1：直接拼接在 URL 字符串中（简单直观）
适用于参数少、无需编码的场景，直接通过字符串拼接/模板字符串拼接 URL：
```javascript
/**
 * 搜索用户（手动拼接 URL 参数，不用 params）
 * @param username 搜索关键词
 */
export const searchUsers = async (username: any) => {
  // 模板字符串拼接：URL + ? + 键值对
  // 等价于 "/api/user/search?username=" + username
  const requestUrl = `/api/user/search?username=${username}`;
  return myAxios.get(requestUrl);
};

// 多参数拼接（用 & 连接）
export const getUserList = async (page: number, size: number) => {
  const requestUrl = `/api/user/list?page=${page}&size=${size}`;
  return myAxios.get(requestUrl);
};
```

#### 方式2：使用 `URLSearchParams` 类（推荐，自动编码特殊字符）
适用于参数多、包含特殊字符（如空格、中文、& 等）的场景，自动处理 URL 编码，避免参数传递异常：
```javascript
/**
 * 搜索用户（用 URLSearchParams 拼接，自动编码）
 * @param username 搜索关键词
 */
export const searchUsers = async (username: any) => {
  // 1. 创建 URLSearchParams 实例
  const searchParams = new URLSearchParams();
  // 2. 添加参数（键值对形式）
  searchParams.append("username", username);
  // 3. 拼接为完整 URL（toString() 自动生成编码后的查询字符串）
  const requestUrl = `/api/user/search?${searchParams.toString()}`;
  return myAxios.get(requestUrl);
};

// 多参数示例
export const getUserList = async (page: number, size: number) => {
  const searchParams = new URLSearchParams();
  searchParams.append("page", page.toString());
  searchParams.append("size", size.toString());
  const requestUrl = `/api/user/list?${searchParams.toString()}`;
  return myAxios.get(requestUrl);
};
```

### 四、同是 POST 方法，删除用户接口需单独指定 `headers` 的原因
核心原因：**传递的 `data` 类型是「原始基本类型（字符串 id）」，而非「JSON 对象」，导致 axios 默认的 `Content-Type` 不符合后端 `@RequestBody` 的要求**，具体拆解如下：

#### 1. 先明确两个前提
- 后端用 `@RequestBody` 注解：该注解的作用是 **从 HTTP 请求体中读取 JSON 格式数据，并自动映射到 Java 实体类/参数**，它要求请求头必须是 `Content-Type: application/json`，否则会解析失败（报 `415 Unsupported Media Type` 或 `JSON parse error`）。
- axios 默认的 `Content-Type` 规则：
    - 当 `data` 是 **Plain Object（纯 JSON 对象，如 `{ id: 123 }`）** 时，axios 会自动设置请求头为 `Content-Type: application/json;charset=utf-8`，并自动将对象转为 JSON 字符串。
    - 当 `data` 是 **原始类型（字符串、数字、布尔等，如直接传 `id: "123"`）** 或 `FormData` 时，axios 不会自动设置 `application/json`，而是默认使用 `Content-Type: text/plain`（字符串）或 `multipart/form-data`（FormData）。

#### 2. 本案例的关键问题
删除用户接口中，`data` 直接传递了 **字符串类型的 `id`**（`data: id`，`id` 是 `string` 类型），而非 JSON 对象（如 `data: { id: id }`）：
- 此时 axios 默认的请求头是 `Content-Type: text/plain`，而非 `application/json`。
- 后端 `@RequestBody` 只能解析 `application/json` 格式的请求体，无法识别 `text/plain` 格式的字符串 `id`，会导致解析失败。

#### 3. 解决方案（两种，任选其一）
- 方案1：手动设置 `headers: { "Content-Type": "application/json" }`（本案例采用的方式），强制告诉后端“请求体是 JSON 格式”，即使 `data` 是原始字符串，后端也会按 JSON 格式解析。
- 方案2：将 `data` 改为 JSON 对象（无需手动设置 headers），让 axios 自动设置 `application/json`：
  ```javascript
  export const deleteUser = async (id: string) => {
    return myAxios.post("/api/user/delete", { id }); // 传 JSON 对象 { id: id }
    // 无需手动设置 headers，axios 会自动添加 Content-Type: application/json
  };
  ```
  此时后端只需将接收参数改为对应的实体类（或用 `@RequestBody Map<String, String>`）即可解析。

#### 4. 其他 POST 接口无需手动设置 headers 的原因
用户注册、登录、注销接口中，`data` 传递的是 `params`（通常是 JSON 对象，如 `{ username: "xxx", password: "xxx" }`），符合 axios “Plain Object” 的规则，axios 会自动设置 `Content-Type: application/json`，满足后端 `@RequestBody` 的要求，因此无需手动指定 headers。

### 总结
1.  `data` 传**请求体**（隐藏、安全，配合 POST/PUT），`params` 传**URL 查询参数**（可见、简单，配合 GET）；
2.  `myAxios.get(url, { params: {} })`、`myAxios.post(url, data, { headers: {} })` 是核心语法，本案例已完成对应改造；
3.  GET 不用 `params` 可直接拼接 URL（模板字符串）或用 `URLSearchParams`（自动编码）；
4.  删除接口需单独设 headers，是因为 `data` 传的是**字符串原始类型**（非 JSON 对象），axios 默认请求头不符合后端 `@RequestBody` 的 `application/json` 要求，其他 POST 接口传的是 JSON 对象，axios 自动设置了正确的请求头。
// 存放所有和用户有关的api 统一管理请求
import myAxios from "@/request";

/**
 * 用户注册
 * @param params 注册所需的参数（如用户名、密码、手机号等）
 * 导出该函数 方便其他页面使用
 * 语法：export const 定义对外暴露的常量函数，async 关键字标记该函数为异步函数，异步函数内部可使用await
 * params: any 表示参数params的类型为任意类型（TypeScript类型注解）
 */
export const userRegister = async (params: any) => {
  // const res = await myAxios.post("/api/users/register", params);
  const res = await myAxios.request({
    // url: `${process.env.REACT_APP_URL}/user`,
    // 请求接口地址（会拼接myAxios中的基础路径），用途：指定用户注册的后端接口
    url: "api/user/register",
    // 请求方法，用途：POST方法用于向服务器提交数据（注册信息），语法：字符串类型，大小写不敏感（通常大写）
    method: "POST",
    // 需要传递的参数
    // 请求体数据，用途：将注册参数传递给后端，语法：axios中POST请求默认使用data携带请求体数据
    data: params,
  });
  return res;
};
// 可简化成如下：
// export const userRegister = async (params: any) => {
//   // 改造：使用 post 专用方法，第二个参数直接传 data 数据
//   return myAxios.post("/api/user/register", params);
// };

/**
 * 用户登录
 * @param params
 */
export const userLogin = async (params: any) => {
  // 返回axios请求的结果
  // return 后面可以不需要写 await 效果等价
  // 直接返回 axios 的 Promise 对象（透传）
  return myAxios.request({
    url: "/api/user/login",
    method: "POST",
    data: params,
  });
};
// 同理 可简化
// export const userLogin = async (params: any) => {
//   // 改造：post 方法直接传 URL + 请求体数据
//   return myAxios.post("/api/user/login", params);
// };

/**
 * 用户注销-登出
 * @param params
 */
export const userLogout = async (params: any) => {
  return myAxios.request({
    url: "/api/user/logout",
    method: "POST",
    data: params,
  });
};
// export const userLogout = async (params: any) => {
//   return myAxios.post("/api/user/logout", params);
// };

/**
 * 获取当前用户
 */
export const getCurrentUser = async () => {
  return myAxios.request({
    url: "/api/user/current",
    // GET请求，用于从服务器获取数据（无请求体，参数通常拼在URL上）
    method: "GET",
  });
};
// export const getCurrentUser = async () => {
//   // 改造：get 方法只传 URL（无参数）
//   return myAxios.get("/api/user/current");
// };

/**
 * 获取用户列表
 * @param username 搜索关键词
 */
export const searchUsers = async (username: any) => {
  return myAxios.request({
    url: "/api/user/search",
    method: "GET",
    params: {
      username, // 请求参数（URL查询参数），用途：将用户名关键词拼在URL后传递给后端
    },
  });
};
// export const searchUsers = async (username: any) => {
//   // 改造：get 方法第二个参数传配置对象，内部用 params 传递查询参数
//   return myAxios.get("/api/user/search", {
//     params: {
//       username // 等价于 username: username
//     }
//   });
// };

// 如需分页查询 案例
// /**
//  * 获取用户列表（多参数手动拼接，单句return简化版）
//  * @param page 页码
//  * @param size 每页条数
//  */
// export const getUserList = async (page: number, size: number) => {
//   // 直接在myAxios.get()中使用模板字符串拼接URL，省去requestUrl中间变量
//   return myAxios.get(`/api/user/list?page=${page}&size=${size}`);
// };

/**
 * 删除用户
 * @param id 要删除的用户ID
 */
export const deleteUser = async (id: string) => {
  return myAxios.request({
    url: "/api/user/delete",
    method: "POST",
    data: id, // 携带要删除的用户ID的请求体数据
    // 关键点：要传递 JSON 格式的值
    headers: {
      // 手动设置请求头，指定请求体的数据格式为JSON
      // 用途：告诉后端当前请求体的数据格式是JSON，便于后端解析 后端SpringBoot 用了@requestBody 注解
      // 强制告诉后端 “请求体是 JSON 格式”，即使 data 是原始字符串，后端也会按 JSON 格式解析
      // 语法：键值对形式，Content-Type 是HTTP请求头的标准字段，application/json 表示JSON格式
      "Content-Type": "application/json",
    },
  });
};
// export const deleteUser = async (id: string) => {
//   // 改造：post 方法第三个参数传配置对象（设置 headers），第二个参数传 data 数据
//   return myAxios.post(
//     "/api/user/delete",
//     id, // 第二个参数：请求体 data
//     { // 第三个参数：配置对象（设置请求头）
//       headers: {
//         "Content-Type": "application/json"
//       }
//     }
//   )};

// 或者如下
// export const deleteUser = async (id: string) => {
//   return myAxios.post("/api/user/delete", { id }); // 传 JSON 对象 { id: id }
//   // 无需手动设置 headers，axios 会自动添加 Content-Type: application/json
// };

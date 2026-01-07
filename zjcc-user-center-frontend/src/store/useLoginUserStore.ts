// 存放登录用户相关的pinia 数据
// 1. 导入 Pinia 的核心方法 defineStore（用于定义仓库）
import { defineStore } from "pinia";
// 2. 导入 Vue 的响应式方法 ref（用于创建响应式数据）
import { ref } from "vue";
// 注：这里需要补充导入 getCurrentUser 接口（你代码中用到但未显示导入）
import { getCurrentUser } from "@/api/user";

/**
 * 3. 定义并导出登录用户的 Pinia 仓库
 * - defineStore 是 Pinia 定义仓库的核心函数
 * - 第一个参数 "loginUser"：仓库的唯一 ID（必须唯一，Pinia 用它区分不同仓库）
 * - 第二个参数：回调函数（Setup 语法），返回仓库的状态、方法等
 */
export const useLoginUserStore = defineStore("loginUser", () => {
  /**
   * 4. 创建响应式的登录用户状态
   * - ref 创建响应式数据，Pinia 中推荐用 ref 替代 Vue2 的 data
   * - any 类型是临时兼容，实际开发建议定义接口（如 interface LoginUser { username: string; id?: string }）
   * - 初始值：默认 username 为 "未登录"，表示用户未登录状态
   */
  const loginUser = ref<any>({
    username: "未登录AAA",
  });

  /**
   * 5. 异步方法：从后端获取当前登录用户信息
   * - async/await 处理异步请求，避免回调地狱（Callback Hell）
   * - 作用：调用接口获取用户信息，并更新到仓库的 loginUser 状态中
   */
  async function fetchLoginUser() {
    // 5.1 调用 getCurrentUser 接口（假设是获取当前登录用户信息的接口），等待返回结果
    const res = await getCurrentUser();
    // 5.2 接口返回成功（code=0）且有用户数据时，更新响应式状态
    // - res.data.code === 0：后端约定的成功状态码
    // - res.data.data：后端返回的用户信息数据（如 { username: '张三', id: '123' }）
    try {
      if (res.data.code === 0 && res.data.data) {
        loginUser.value = res.data.data; // ref 数据需通过 .value 修改值
      }
      // else {
      //   //  测试代码
      //   setTimeout(() => {
      //     loginUser.value = { username: "！呵呵！", id: 1 };
      //   }, 2000);
      // }
    } catch (error) {
      // 注：失败处理，比如接口报错时重置为未登录状态
      console.error("获取用户信息失败：", error);
      loginUser.value = { username: "未登录" }; // 失败后重置状态
    }
  }

  // 可以简写如下：箭头函数
  // 无论 async 函数内部是否有 return，它的返回值都会被自动包装成 Promise：
  // const fetchLoginUser = async () => {
  //   const res = await getCurrentUser();
  //   // ...后续逻辑
  // };

  /**
   * 6. 同步方法：手动设置登录用户信息
   * - 作用：比如登录成功后，直接传入新用户信息更新状态  loginUserStore.setLoginUser({ username: "张三", id: "123" })
   * - 参数 newLoginUser：新的用户信息对象（any 类型建议替换为具体接口）
   */
  function setLoginUser(newLoginUser: any) {
    // 修改响应式状态，更新为新的用户信息
    loginUser.value = newLoginUser;
  }

  /**
   * 7. 暴露仓库的状态和方法（外部组件可通过仓库实例访问）
   * - loginUser：响应式状态（用户信息）
   * - fetchLoginUser：异步获取用户信息的方法
   * - setLoginUser：手动设置用户信息的方法
   */
  return { loginUser, fetchLoginUser, setLoginUser };
});

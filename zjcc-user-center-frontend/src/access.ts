import { useLoginUserStore } from "@/store/useLoginUserStore";
import { message } from "ant-design-vue";
import router from "@/router";
// import { RouteLocationNormalized as Route } from "vue-router";

/**
 * 全局权限校验 路由守卫
 */
router.beforeEach(
  async (
    to: any, // to: 目标路由对象（用户想要访问的页面）
    from: any, // from: 来源路由对象（用户当前所在的页面）
    next: any // next: 路由控制函数（用于决定是否允许导航继续）
  ) => {
    // 获取全局登录用户状态管理实例（Pinia store）
    const loginUserStore = useLoginUserStore();
    // 从 store 中取出当前登录用户的信息（包含用户名、角色等）
    const loginUser = loginUserStore.loginUser;
    // 获取用户尝试访问的完整路径（包含查询参数，如 /admin/userManage?tab=1）
    const toUrl = to.fullPath;
    // 判断：如果用户访问的路径以 /admin 开头（说明是管理员页面）
    if (toUrl.startsWith("/admin")) {
      // 二次判断：如果用户未登录（loginUser 不存在）或者用户角色不是管理员（userRole !== 1）
      if (!loginUser || loginUser.userRole !== 1) {
        // 弹出错误提示框，告知用户没有权限访问
        message.error("没有权限");
        // 终止当前导航，重定向到登录页面，并在 URL 参数中携带用户原本想访问的路径
        // redirect 参数的作用：登录成功后可以自动跳回用户原本想访问的页面
        next(`/user/login?redirect=${to.fullPath}`);
        // 提前返回，阻止后续代码执行（避免调用两次 next）
        return;
      }
    }
    // 如果通过权限校验（不是管理员页面，或者是管理员且已登录），则允许导航继续
    next();
  }
);

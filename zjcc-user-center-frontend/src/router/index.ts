import { createRouter, createWebHistory, RouteRecordRaw } from "vue-router";
import AboutView from "../views/AboutView.vue";
import WelcomePage from "@/pages/WelcomePage.vue";
import UserLoginPage from "@/pages/user/UserLoginPage.vue";
import UserManagePage from "@/pages/admin/UserManagePage.vue";

const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    // 1.Vue Router 中，route.name 是全局唯一的标识，不能重复。
    // 当存在两个同名路由时，Vue Router 只会识别第一个,第二个会被忽略，表现为 “失效”（无法跳转、组件不渲染）。
    // 2.可通过 name 跳转路由（无需拼接路径-解耦？，更灵活） router.push({ name: 'userRegister' });
    // 3.若路由需要传递参数，通过 name 跳转更清晰。如下
    // router.push({
    //   name: 'userRegister',
    //   params: { id: 123 } // 配合 params 传参（仅 name 跳转支持 params 隐式传参）
    // });
    name: "home",
    // 静态导入文件初始化时一次性加载所有静态导入的组件，加载后可直接通过变量名（HomeView/AboutView）赋值给component
    // 首页组件、高频访问组件（如 HomeView） 加载速度快，无需异步等待
    // component: HomeView,
    component: WelcomePage,
  },
  {
    path: "/user/login",
    name: "login",
    // route level code-splitting
    // this generates a separate chunk (about.[hash].js) for this route
    // which is lazy-loaded when the route is visited.
    // component: () =>
    //   import(/* webpackChunkName: "about" */ "../views/AboutView.vue"),
    component: UserLoginPage,
  },
  {
    path: "/user/register",
    name: "register",
    // 动态导入(懒加载) 非首页、低频访问组件（如用户注册、用户管理）
    // 优化首屏加载性能，减少初始打包体积
    component: () => import("../views/AboutView.vue"),
  },
  {
    path: "/admin/userManage",
    name: "userManager",
    component: UserManagePage,
  },
];

const router = createRouter({
  history: createWebHistory(process.env.BASE_URL),
  routes,
});

export default router;

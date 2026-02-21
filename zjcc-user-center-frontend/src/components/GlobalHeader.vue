<template>
  <div id="globalHeader">
    <!--  wrap 杜绝缩小浏览器窗口时，自动换行  -->
    <a-row :wrap="false">
      <a-col flex="200px">
        <div class="title-bar">
          <img src="../assets/logo.png" alt="logo" class="logo" />
          <div class="title">User-Center</div>
        </div>
      </a-col>
      <a-col flex="auto">
        <a-menu
          v-model:selectedKeys="current"
          mode="horizontal"
          :items="items"
          @click="doMenuClick"
        />
      </a-col>
      <a-col flex="120px">
        <div class="user-login-status">
          <!-- 如果登录了，则展示登录用户名 如果没有用户名则给一个默认值“无名” -->
          <!-- 如果登录了，则展示登录用户名和登出按钮 -->
           <div v-if="loginUserStore.loginUser.id" class="user-info">
           <span class="username">{{ loginUserStore.loginUser.username || "无名" }}</span>
               <a-button type="link" @click="doLogout" class="logout-btn">登出</a-button>
          </div>
          <!-- 如果没登录，则展示登录按钮 -->
          <div v-else>
            <!-- <a-button type="primary" href="/user/login">登录</a-button> -->
            <a-button type="primary" @click="router.push('/user/login')">登录</a-button>
          </div>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { h, ref } from "vue";
import {
  MailOutlined,
  CrownOutlined,
  SettingOutlined,
} from "@ant-design/icons-vue";
import { MenuProps, message } from "ant-design-vue";
// Vue: Parameter 'to' implicitly has an any type 错误，是 TypeScript 的类型检查提示
// 核心原因是：在使用 router.afterEach 时，参数 to、from 没有显式声明类型，TypeScript 无法推断其类型，默认将其视为 any 类型（项目开启了 noImplicitAny 配置，禁止隐式 any 类型）
// RouteLocationNormalized 是什么？
// 它是 vue-router 提供的标准化路由位置类型，包含了路由的 path、name、params、query、meta 等所有属性
// 它是 to、from 参数的标准类型。
import { useRouter, RouteLocationNormalized as Route } from "vue-router";
import { useLoginUserStore } from "@/store/useLoginUserStore";
import { userLogout } from "@/api/user";

const loginUserStore = useLoginUserStore();

const router = useRouter();
// 点击菜单后的路由跳转事件 route-to
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  });
};
// 登出功能
const doLogout = async () =>{
  const res = await userLogout({});
  if (res.data.code === 0){
    loginUserStore.loginUser = {};// 清除登录状态
    message.success("登出成功")
    router.push("/user/login")
  }
}

const current = ref<string[]>(["home"]);
// 保障刷新页面后菜单会根据 current 选中项高亮
// router.afterEach 是 Vue Router 提供的全局路由后置守卫（也叫全局后置钩子）
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

const items = ref<MenuProps["items"]>([
  {
    key: "/",
    icon: () => h(MailOutlined),
    label: "主页",
    title: "主页",
  },
  {
    key: "/user/login",
    label: "用户登录",
    title: "用户登录",
  },
  {
    key: "/user/register",
    icon: () => h(SettingOutlined),
    label: "用户注册",
    title: "用户注册",
    // children: [
    //   {
    //     type: "group",
    //     label: "Item 1",
    //     children: [
    //       {
    //         label: "Option 1",
    //         key: "setting:1",
    //       },
    //       {
    //         label: "Option 2",
    //         key: "setting:2",
    //       },
    //     ],
    //   },
    //   {
    //     type: "group",
    //     label: "Item 2",
    //     children: [
    //       {
    //         label: "Option 3",
    //         key: "setting:3",
    //       },
    //       {
    //         label: "Option 4",
    //         key: "setting:4",
    //       },
    //     ],
    //   },
    // ],
  },
  {
    key: "/admin/userManage",
    icon: () => h(CrownOutlined),
    label: "用户管理",
    title: "用户管理",
  },
  {
    key: "/others",
    label: h(
      "a",
      { href: " https://www.google.com/", target: "_blank" },
      "Google"
    ),
    title: "Google",
  },
]);
</script>
<style scoped>
.title-bar {
  display: flex;
  align-items: center;
}

.title {
  color: black;
  font-size: 18px;
  margin-left: 16px;
}

.logo {
  height: 48px;
}
</style>

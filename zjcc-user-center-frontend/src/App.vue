<!-- 根页面。启动项目就会加载 -->
<template>
  <div id="app">
    <BasicLayout />
    <!--    <LoginView />-->
  </div>
</template>

<script setup lang="ts">
import BasicLayout from "@/layouts/BasicLayout.vue";

import { useLoginUserStore } from "@/store/useLoginUserStore";

// 进入页面就会调用fetchLoginUser()方法 获取当前登录用户
// App.vue 作为项目的根组件，具备全局执行时机早、作用域覆盖全的特性，是处理这类全局初始化逻辑的最佳位置
// 执行时机最早且唯一
// 项目启动时，App.vue 是第一个被渲染的组件，且只会初始化一次（除非手动刷新页面）；
// 在这里调用 fetchLoginUser()，能保证用户进入任何页面之前，先完成登录状态的拉取，避免页面渲染后才加载用户信息导致的 “未登录”→“已登录” 的状态闪烁。
// 作用域覆盖整个应用
// App.vue 是所有页面 / 组件的父组件，在这里初始化的用户状态（Pinia 中的 loginUser），能被全局所有子组件（比如导航栏、个人中心、各业务页面）立即访问；
// 如果把这个逻辑写在某个子页面（比如 Home.vue），那么用户直接访问其他页面（比如 /profile）时，就会跳过这个逻辑，导致用户信息未加载。
// 无需重复编写逻辑
// 用户信息是全局通用的核心数据（导航栏显示用户名、权限控制、接口请求携带用户 token 等都需要），放在根组件初始化，无需在每个页面都写一遍 fetchLoginUser()，符合 “一次编写、全局复用” 的原则。
// 二、对比：如果不写在 App.vue 会有什么问题？
// 放置位置:	          问题点:
// 某个业务页面	        访问其他页面时不会执行，导致用户信息缺失；重复进入该页面会重复调用接口
// 路由守卫	            虽然也能实现，但路由守卫更适合做 “页面跳转控制”，而非 “数据初始化”，职责不清晰
// 组件的 onMounted	  子组件的 onMounted 执行时机晚于页面渲染，可能出现 “未登录” 的临时状态
const loginUserStore = useLoginUserStore();
loginUserStore.fetchLoginUser();

// import { onMounted } from "vue"; // 导入生命周期钩子
// const loginUserStore = useLoginUserStore();
// // 推荐：把异步逻辑放在 onMounted 中（符合 Vue 生命周期规范）
// onMounted(async () => {
//   try {
//     await loginUserStore.fetchLoginUser(); // 等待用户信息加载完成
//   } catch (error) {
//     console.error("初始化用户信息失败：", error);
//     // 可选：失败后跳转到登录页（如果需要）
//     // router.push("/login");
//   }
// });
</script>

<style></style>

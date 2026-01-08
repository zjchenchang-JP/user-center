<!-- 基础布局，全局通用 导航栏之类 -->
<template>
  <div id="basic-layout">
    <a-layout>
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
        <!-- 动态路由切换入口 -->
        <router-view />
      </a-layout-content>
      <a-layout-footer class="footer">
        <!--
          noopener 核心解决安全（防止原页面被操控）+ 性能（独立进程） 问题；
          noreferrer 核心解决隐私（隐藏跳转来源）+ 兼容（老浏览器） 问题
          跳转外部链接时必须加 -->
        <a
          href="https://codefather.cn"
          target="_blank"
          rel="noopener noreferrer"
        >
          User Center by CHEN CHANG
        </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
import { getCurrentUser, searchUsers } from "@/api/user";

// TODO 测试代码-请求发送 前后端联动
// 异步函数返回 Promise → 用 then 方法监听 Promise 成功状态 → 箭头函数接收并处理成功结果。
// then 核心：专门处理 Promise 成功状态，接收异步操作结果，支持链式调用。
getCurrentUser().then((res) => {
  console.log(res);
});
</script>
<!--
  background-color: #efefef; 背景灰色
  text-align: 文本对齐方式
  linear-gradient 渐变
-->
<style scoped>
#basic-layout .footer {
  background-color: #efefef;
  text-align: center;
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 16px;
}

#basic-layout .content {
  /* padding: 20px; */
  padding-bottom: 80px; /* 高于 footer 高度 */
  margin-bottom: 20px;
  background: linear-gradient(to right, #fefefe, #fff);
}

#basic-layout .header {
  padding-inline: 20px;
  background: white;
  margin-bottom: 0px;
  color: unset;
}

.ant-layout {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.ant-layout-content {
  flex: 1;
  padding: 20px;
  padding-bottom: 64px; /* 留出 footer 空间 */
  margin-bottom: 0;
}

.footer {
  position: fixed;
  bottom: 0;
  left: 0;
  right: 0;
  height: 64px;
}
</style>

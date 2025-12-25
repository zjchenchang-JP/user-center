<!-- 基础布局，全局通用 导航栏之类 -->
<template>
  <div id="basic-layout">
    <a-layout>
      <a-layout-header class="header">
        <GlobalHeader />
      </a-layout-header>
      <a-layout-content class="content">
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
          编程导航官网 by CHEN CHANG
        </a>
      </a-layout-footer>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import GlobalHeader from "@/components/GlobalHeader.vue";
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
  padding: 20px;
  margin-bottom: 20px;
  background: linear-gradient(to right, #fefefe, #fff);
}

#basic-layout .header {
  padding-inline: 20px;
  background: white;
  margin-bottom: 10px;
  color: unset;
}
</style>

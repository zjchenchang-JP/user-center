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
      <a-col flex="80px">
        <div class="user-login-status">
          <a-button type="primary" href="/user/login">登录</a-button>
        </div>
      </a-col>
    </a-row>
  </div>
</template>
<script lang="ts" setup>
import { h, ref } from "vue";
import { MailOutlined, CrownOutlined } from "@ant-design/icons-vue";
import { MenuProps } from "ant-design-vue";
import { useRouter } from "vue-router";

const router = useRouter();
// 点击菜单后的路由跳转事件 route-to
const doMenuClick = ({ key }: { key: string }) => {
  router.push({
    path: key,
  });
};

const current = ref<string[]>(["mail"]);

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
    // icon: () => h(SettingOutlined),
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

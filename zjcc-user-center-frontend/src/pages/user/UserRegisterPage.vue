<!-- 注册页 -->
<template>
  <div id="useLoginPage">
    <h2 class="title">用户注册</h2>
    <a-form
      style="max-width: 480px; margin: 0 auto"
      :model="formState"
      name="basic"
      label-align="left"
      :label-col="{ span: 4 }"
      :wrapper-col="{ span: 20 }"
      autocomplete="off"
      @finish="onSubmit"
      @finishFailed="onFinishFailed"
    >
      <a-form-item
        label="账号"
        name="userAccount"
        :rules="[{ required: true, message: 'Please input your userAccount!' }]"
      >
        <a-input
          v-model:value="formState.userAccount"
          placeholder="请输入用户名 / username"
        />
      </a-form-item>

      <a-form-item
        label="密码"
        name="userPassword"
        :rules="[
          { required: true, message: 'Please input your userPassword!' },
          { min: 8, message: '密码长度不能少于8位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.userPassword"
          placeholder="请输入密码 / password"
        />
      </a-form-item>
      <a-form-item
        label="确认密码"
        name="checkPassword"
        :rules="[
          { required: true, message: 'Please input your checkPassword!' },
          { min: 8, message: '密码长度不能少于8位' },
        ]"
      >
        <a-input-password
          v-model:value="formState.checkPassword"
          placeholder="请输入确认密码 / checkPassword"
        />
      </a-form-item>
      <a-form-item
        label="编号"
        name="planetCode"
        :rules="[
          { required: true, message: 'Please input your planetCode!' }
        ]"
      >
        <a-input-password
          v-model:value="formState.planetCode"
          placeholder="请输入编号 / planetCode"
        />
      </a-form-item>
      <a-form-item :wrapper-col="{ offset: 4, span: 20 }">
        <a-button type="primary" html-type="submit">注册</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { userLogin, userRegister } from "@/api/user";
import { useLoginUserStore } from "@/store/useLoginUserStore";
import { message } from "ant-design-vue";
import { reactive } from "vue";
import { useRouter } from "vue-router";

interface FormState {
  userAccount: string;
  userPassword: string;
  checkPassword: string;
  planetCode: string;
}
// 定义响应式表单数据, 方便接受表单输入的值
const formState = reactive<FormState>({
  userAccount: "",
  userPassword: "",
  checkPassword: "",
  planetCode: "",
});

const loginUserStore = useLoginUserStore();
const router = useRouter();
// 提交表单
const onSubmit = async () => {
  // 简单前端校验
  if (formState.checkPassword != formState.userPassword) {
    message.error("两次输入的密码不一致");
    return;
  }
  const res = await userRegister(formState)
  // 注册成功 跳转到登录界面
  if (res.data.code === 0 && res.data.data) {
    message.success("注册成功");
    router.push({
      path:"/user/login",
      replace: true,
    });
  } else {
    message.error("注册失败, " + res.data.description);
  }
};
const onFinishFailed = (errorInfo: any) => {
  console.log("Failed:", errorInfo);
};
</script>
<style scoped>
.title {
  margin-bottom: 16px;
  text-align: center;
}
</style>

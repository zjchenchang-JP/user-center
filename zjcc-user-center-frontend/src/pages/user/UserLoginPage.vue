<!-- 登录页 -->
<template>
  <div id="useLoginPage">
    <h2 class="title">用户登录</h2>
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
      <a-form-item :wrapper-col="{ offset: 4, span: 20 }">
        <a-button type="primary" html-type="submit">登录</a-button>
      </a-form-item>
    </a-form>
  </div>
</template>
<script lang="ts" setup>
import { userLogin } from "@/api/user";
import { useLoginUserStore } from "@/store/useLoginUserStore";
import { message } from "ant-design-vue";
import { reactive } from "vue";
import { useRouter } from "vue-router";

// 定义表单数据类型  最好和后端定义的接受DTO的字段名保持一致
// 这样可以减少数据转换的麻烦
// // 前端字段名和后端不一致的情况  前端处理
// interface FormState {
//   account: string; // 前端用account
//   password: string; // 前端用password
// }

// // 发送请求时手动映射
// const formData: FormState = { account: "admin", password: "123456" };
// const requestData = {
//   userAccount: formData.account, // 映射到后端的userAccount
//   userPassword: formData.password // 映射到后端的userPassword
// };
// // 发送请求
// axios.post("/login", requestData);

// 或者后端处理
// 用@JsonProperty指定前端传入的字段名
// @JsonProperty("account") // 前端传account，映射到userAccount
// private String userAccount;
// @JsonProperty("password") // 前端传password，映射到userPassword
// private String userPassword;
interface FormState {
  userAccount: string;
  userPassword: string;
}
// 定义响应式表单数据
const formState = reactive<FormState>({
  userAccount: "",
  userPassword: "",
});

const loginUserStore = useLoginUserStore();
const router = useRouter()
// 提交表单
const onSubmit = async (values: any) => {
  const res = await userLogin(values);
  // 登录成功 把登录用户状态保存到全局状态 Pinia 中
  if (res.data.code === 0 && res.data.data) {
    await loginUserStore.fetchLoginUser();
    message.success("登录成功")
    // 登录成功后跳转到首页
    // window.location.href = "/";
    router.push({
      path: "/",
      replace: true,
    });
  } 
  // else {
  //   message.error
  // }
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

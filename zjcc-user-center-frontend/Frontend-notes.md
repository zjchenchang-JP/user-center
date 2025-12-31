# Frontend
## 2025/12/27
### Router
#### 1.Vue Router 中，route.name 是全局唯一的标识，不能重复。
当存在两个同名路由时，Vue Router 只会识别第一个,第二个会被忽略，表现为 “失效”（无法跳转、组件不渲染）。
#### 2.可通过 name 跳转路由（无需拼接路径-解耦？，更灵活） router.push({ name: 'userRegister' });
#### 3.若路由需要传递参数，通过 name 跳转更清晰。如下
```vue
router.push({
  name: 'userRegister',
  params: { id: 123 } // 配合 params 传参（仅 name 跳转支持 params 隐式传参）
});
```

```vue
const routes: Array<RouteRecordRaw> = [
  {
    path: "/",
    
    name: "home",
    // 静态导入文件初始化时一次性加载所有静态导入的组件，加载后可直接通过变量名（HomeView/AboutView）赋值给component
    // 首页组件、高频访问组件（如 HomeView） 加载速度快，无需异步等待
    component: HomeView,
  },
];
```
在模板中使用 <router-link> 时，也可通过 :to="{ name: 'xxx' }" 实现跳转，和 JS 代码中跳转逻辑保持一致：
```vue
<!-- 无 name 时：通过 path 跳转 -->
<router-link to="/user/register">注册</router-link>

<!-- 有 name 时：通过 name 跳转 -->
<router-link :to="{ name: 'userRegister' }">注册</router-link>
```
3. 其他小众用途
   路由守卫中，可通过 to.name 快速判断目标路由，简化逻辑；
   多路由实例、路由缓存（<keep-alive>）场景中，name 可作为标识辅助筛选。

### HTML
```vue
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
```
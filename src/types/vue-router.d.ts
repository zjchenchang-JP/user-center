declare module 'vue-router' {
  // 定义路由记录类型（匹配Vue Router的RouteRecordRaw）
  export interface RouteRecordRaw {
    path: string;
    name?: string;
    component: any;
    children?: Array<RouteRecordRaw>;
    meta?: Record<string, any>;
  }

  // 导出createRouter、createWebHistory及RouteRecordRaw
  export function createRouter(options: {
    history: any;
    routes: Array<RouteRecordRaw>;
  }): any;
  export function createWebHistory(base?: string): any;
}
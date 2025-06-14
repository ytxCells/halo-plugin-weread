import { definePlugin } from "@halo-dev/console-shared";
import HomeView from "./views/HomeView.vue";
import { IconPlug } from "@halo-dev/components";
import { markRaw } from "vue";

export default definePlugin({
  components: {},
  routes: [
    {
      parentName: "Root",
      route: {
        path: "/read",// TodoList 的路由 path
        name: "Read",// 菜单标识名
        component: HomeView,
        meta: {
          title: "Todo List",//菜单页的浏览器 tab 标题
          searchable: true,
          menu: {
            name: "微信读书",// TODO 菜单显示名称
            group: "content",// 所在组名
            icon: markRaw(IconPlug),
            priority: 0,
          },
        },
      },
    },
  ],
  extensionPoints: {},
});

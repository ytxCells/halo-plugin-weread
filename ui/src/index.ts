import { definePlugin } from '@halo-dev/console-shared'
import HomeView from './views/HomeView.vue'
import ConfigView from "@/views/ConfigView.vue";
import { default as WeReadIcon } from '~icons/ri/book-2-line'
import { markRaw } from 'vue'
export default definePlugin({
  routes: [                                 // Console 控制台路由定义
    {
      parentName: "Root",
      route: {
        path: "/weread",
        name: "weread",
        component: HomeView,
        meta: {
          title:"微信读书",
          searchable:true,
          permissions: [""],
          menu: {
            name: "微信读书",
            group: "content",
            icon: markRaw(WeReadIcon),
            priority: 20
          },
        },
      },
    },
    {
      parentName: "Root",
      route:{
        path: "/weread/config",
        name: "config",
        component: ConfigView,
        meta:{
          title: "微信读书配置",
          searchable: true,
          permissions: [""],
        }
      }
    }
  ],
});

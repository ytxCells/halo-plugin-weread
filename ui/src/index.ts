import { definePlugin } from '@halo-dev/console-shared'
import HomeView from './views/HomeView.vue'
import { IconBookRead } from '@halo-dev/components'
import { default as WeReadIcon } from '~icons/ri/book-2-line'
import { markRaw } from 'vue'

export default definePlugin({
  routes: [                                 // Console 控制台路由定义
    {
      parentName: "Root",
      route: {
        path: "/weread",
        name: "微信读书",
        component: HomeView,
        meta: {
          permissions: [""],
          menu: {
            name: "微信读书",
            group: "content",
            icon: markRaw(WeReadIcon),
            priority: 40
          },
        },
      },
    },
  ],
  ucRoutes: [                               // UC 个人中心路由定义
    {
      parentName: "Root",
      route: {
        path: "/weread",
        name: "微信读书",
        component: HomeView,
        meta: {
          permissions: [""],
          menu: {
            name: "微信读书",
            group: "content",
            icon: markRaw(IconBookRead),
            priority: 40
          },
        },
      },
    },
  ]
});

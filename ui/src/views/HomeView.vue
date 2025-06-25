
<template>
  <VPageHeader title="微信读书">
    <template #actions>
      <VSpace v-permission="['plugin:douban:manage']">
        <VButton :route="{ name: 'DoubanCron' }" size="sm">
          任务
        </VButton>
        <VButton :route="{ name: 'DoubanCron' }" size="sm">
          登陆
        </VButton>
        <VButton type="secondary">
          <template #icon>
            <IconAddCircle class="h-full w-full" />
          </template>
          新建
        </VButton>
        <VButton type="secondary" @click="synchronizationWeRead">
          同步数据
        </VButton>
        <VButton type="danger"> 清空 </VButton>
      </VSpace>
    </template>
  </VPageHeader>
  <section id="plugin-starter">
    <div class="wrapper">
      <span class="title"> 你已经成功运行起了插件！ </span>
      <span class="message">你可以点击下方文档继续下一步</span>
      <div class="docs">
        <a
                href="https://docs.halo.run/developer-guide/plugin/publish"
                class="docs__box"
                target="_blank"
        >
          <h2 class="docs__box-title"><RiShareCircleLine />发布一个插件</h2>
          <span class="docs__box-message">
            了解如何与我们的社区分享您的扩展。
          </span>
          <span class="docs__box-arrow">
            <RiArrowRightSLine />
          </span>
        </a>
        <a
                href="https://docs.halo.run/category/%E5%9F%BA%E7%A1%80"
                class="docs__box"
                target="_blank"
        >
          <h2 class="docs__box-title"><RiComputerLine />基础概览</h2>
          <span class="docs__box-message">
            了解插件的项目结构、生命周期、资源配置等。
          </span>
          <span class="docs__box-arrow">
            <RiArrowRightSLine />
          </span>
        </a>
        <a
                href="https://docs.halo.run/developer-guide/plugin/examples/todolist"
                class="docs__box group"
                target="_blank"
        >
          <h2 class="docs__box-title"><RiBookReadLine />示例插件</h2>
          <span class="docs__box-message">帮助你从 0 到 1 完成一个插件。</span>
          <span class="docs__box-arrow">
            <RiArrowRightSLine />
          </span>
        </a>
        <a
                href="https://docs.halo.run/category/api-%E5%8F%82%E8%80%83"
                class="docs__box"
                target="_blank"
        >
          <h2 class="docs__box-title"><RiCodeBoxLine />API 参考</h2>
          <span class="docs__box-message">插件中的 API 列表。</span>
          <span class="docs__box-arrow">
            <RiArrowRightSLine />
          </span>
        </a>
      </div>
    </div>
  </section>
</template>

<script setup lang="ts">
import confetti from "canvas-confetti";
import { onMounted } from "vue";
import { axiosInstance } from "@halo-dev/api-client";
import {
  VButton,
  VPageHeader,
  VSpace,
  IconAddCircle,
  Toast,
  Dialog
} from "@halo-dev/components";
onMounted(() => {
  confetti({
    particleCount: 100,
    spread: 70,
    origin: { y: 0.6, x: 0.58 },
  });
});

const synchronizationWeRead = () => {
  Dialog.warning({
    title: "同步微信读书数据",
    description: "确定要同步微信数据吗，此操作可能会持续较长时间。 ",
    confirmType: "danger",
    confirmText: "确定",
    cancelText: "取消",
    onConfirm: async () => {
      try {
        await axiosInstance.post("/apis/api.plugin.halo.run/v1alpha1/plugins/plugin-weread/weRead/synchronizationWeRead")
                .then((res: any) => {
                  Toast.success("已请求同步微信读书数据");
                });
      } catch (e) {
        console.error("", e);
      }
    },
  });
};
</script>


<style lang="scss" scoped>
#plugin-starter {
  height: 100vh;
  background-color: #f8fafc;
}

.wrapper {
  display: flex;
  flex-direction: column;
  justify-content: center;
  align-items: center;
  height: 100vh;
  gap: 1.5rem;

  .title {
    font-weight: 700;
    font-size: 1.25rem;
    line-height: 1.75rem;
  }

  .message {
    font-size: 0.875rem;
    line-height: 1.25rem;
    color: #4b5563;
  }

  .docs {
    display: grid;
    grid-template-columns: repeat(1, minmax(0, 1fr));
    gap: 1rem;
    max-width: 48rem;

    .docs__box {
      background-color: #fff;
      border-radius: 0.375rem;
      padding: 0.75rem;
      transition-property: all;
      transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
      transition-duration: 300ms;
      cursor: pointer;
      filter: drop-shadow(0 1px 2px rgb(0 0 0 / 0.1))
      drop-shadow(0 1px 1px rgb(0 0 0 / 0.06));

      &:hover {
        box-shadow:
                0 0 0 0px #fff,
                0 0 0 1px rgb(59 130 246 / 0.5),
                0 0 #0000;
      }

      .docs__box-title {
        display: flex;
        flex-direction: row;
        font-size: 1.125rem;
        line-height: 1.75rem;
        font-weight: 700;
        margin-bottom: 2rem;
        gap: 0.5rem;
        align-items: center;
      }

      .docs__box-message {
        font-size: 0.875rem;
        line-height: 1.25rem;
        color: #4b5563;
      }

      .docs__box-arrow {
        pointer-events: none;
        position: absolute;
        top: 1rem;
        right: 1rem;
        transition-property: all;
        transition-timing-function: cubic-bezier(0.4, 0, 0.2, 1);
        transition-duration: 150ms;
        color: #d1d5db;
      }

      &:hover {
        .docs__box-arrow {
          color: #9ca3af;
          transform: translate(00.375rem, 0) rotate(0) skewX(0) skewY(0)
          scaleX(1) scaleY(1);
        }
      }
    }
  }

  @media (min-width: 640px) {
    .docs {
      grid-template-columns: repeat(2, minmax(0, 1fr));
    }
  }
}
</style>

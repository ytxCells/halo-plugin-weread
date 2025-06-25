
<template>
  <VPageHeader title="微信读书">
    <template #actions>
      <VSpace>
        <VButton :route="{name:'config'}"  size="sm">
          配置
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

</style>

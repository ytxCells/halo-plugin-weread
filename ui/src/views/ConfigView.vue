<script setup lang="ts">
import { ref } from "vue"
import { Toast, VButton, VCard, VPageHeader } from "@halo-dev/components";
import WeReadCookieConfig from "@/component/WeReadCookieConfig.vue";
</script>

<template>
  <VPageHeader title="配置">
    <template #actions>
      <VSpace>
        <VButton :route="{ name: 'weread' }" size="sm">
          返回
        </VButton>
      </VSpace>
    </template>
  </VPageHeader>
  <WeReadCookieConfig/>
</template>

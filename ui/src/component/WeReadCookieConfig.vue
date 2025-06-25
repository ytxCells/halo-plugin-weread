
<script setup lang="ts">
import { onMounted, ref } from "vue"
import { Toast, VButton, VCard } from "@halo-dev/components";
import { axiosInstance } from "@halo-dev/api-client";
import type { AxiosError } from 'axios';

// Cookie 管理相关逻辑
const cookieValue = ref('');
const isSaving = ref(false);
const configName = "weread-config"; // 固定的配置名称
const configVersion = ref(0); // 存储配置版本号

// 获取Cookie
const fetchInitialCookie = async () => {
  try {
    // 修正获取接口地址
    const response = await axiosInstance.get(
            `/apis/weread.pplay.fun/v1alpha1/config/${configName}`
    );

    // 提取数据
    if (response.data) {
      // 保存版本号
      configVersion.value = response.data.metadata?.version || 0;
      if (response.data?.spec?.cookie) {
        cookieValue.value = response.data.spec.cookie;
      }
    }
  } catch (err) {
    const error = err as AxiosError;
    console.error("获取Cookie失败:", error);
    Toast.error("获取配置失败，请重试");
  }
};

onMounted(() => {
  fetchInitialCookie();
});

// 保存Cookie - 统一使用PUT请求
const saveCookie = async () => {
  if (!cookieValue.value) {
    Toast.error('Cookie为空，无法保存');
    return;
  }

  isSaving.value = true;
  try {
    const configData = {
      apiVersion: "weread.pplay.fun/v1alpha1",
      kind: "wereadconfig",
      metadata: {
        name: configName,
        version: configVersion.value // 保留版本号
      },
      spec: {
        cookie: cookieValue.value
      }
    };

    // 统一使用PUT请求（后端会处理创建或更新）
    const response = await axiosInstance.put(
            `/apis/weread.pplay.fun/v1alpha1/config/${configName}`,
            configData
    );

    // 更新本地版本号
    configVersion.value = response.data.metadata?.version || 0;

    Toast.success('Cookie已保存');
  } catch (err) {
    const error = err as AxiosError;
    console.error("保存Cookie失败:", error);

    if (error.response?.status === 409) {
      Toast.error('保存失败：配置已被修改，请刷新后重试');
      // 自动刷新配置
      await fetchInitialCookie();
    } else {
      Toast.error('保存Cookie失败');
    }
  } finally {
    isSaving.value = false;
  }
};

const copyCookie = async () => {
  if (!cookieValue.value) {
    Toast.error('Cookie为空，无法复制');
    return;
  }

  try {
    await navigator.clipboard.writeText(cookieValue.value);
    Toast.success('Cookie已成功复制到剪贴板');
  } catch (err) {
    Toast.error('复制Cookie失败，请重试');
  }
};

const refreshCookie = () => {
  const newCookie = `mocked_cookie_value_${Date.now()}`;
  cookieValue.value = newCookie;
  Toast.info('Cookie已刷新');
};
</script>

<template>
  <div class="m-0 md:m-4">
    <VCard class="mt-4">
      <div class="p-5 border-b border-gray-200">
        <h2 class="text-lg font-semibold text-gray-800">微信读书Cookie管理</h2>
        <p class="text-sm text-gray-500 mt-1">管理您的微信读书账户认证信息</p>
      </div>

      <div class="p-5">
        <div class="mb-6">
          <div class="flex justify-between items-center mb-2">
            <label for="cookie-input" class="block text-sm font-medium text-gray-700">
              Cookie值
            </label>
            <span class="text-xs text-gray-500">长度: {{ cookieValue.length }} 字符</span>
          </div>

          <div class="relative">
            <textarea
                    id="cookie-input"
                    v-model="cookieValue"
                    rows="4"
                    class="w-full p-3 border border-gray-400 rounded-md focus:border-blue-600 focus:ring-2 focus:ring-blue-500 transition"
                    placeholder="请输入微信读书的Cookie..."
            ></textarea>

            <button
                    v-if="cookieValue"
                    @click="cookieValue = ''"
                    class="absolute top-3 right-3 text-gray-500 hover:text-gray-700"
            >
              <svg xmlns="http://www.w3.org/2000/svg" class="h-4 w-4" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </button>
          </div>

          <p v-if="!cookieValue" class="mt-1 text-sm text-red-500">Cookie不能为空</p>
        </div>

        <div class="flex flex-wrap gap-2">
          <VButton
                  type="secondary"
                  @click="saveCookie"
                  class="px-4 py-2 text-sm"
                  :loading="isSaving"
          >
            保存
          </VButton>

          <VButton
                  type="default"
                  @click="copyCookie"
                  class="px-4 py-2 text-sm"
          >
            复制
          </VButton>

          <VButton
                  type="primary"
                  @click="refreshCookie"
                  class="px-4 py-2 text-sm"
          >
            刷新
          </VButton>
        </div>
      </div>
    </VCard>
  </div>
</template>

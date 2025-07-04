import {rsbuildConfig} from '@halo-dev/ui-plugin-bundler-kit';
import Icons from "unplugin-icons/rspack";
import {pluginSass} from "@rsbuild/plugin-sass";

export default rsbuildConfig({
  rsbuild: {
    resolve: {
      alias: {
        "@": "./src",
      },
    },
    plugins: [
      pluginSass()
    ],
    tools: {
      rspack: {
        plugins: [
          Icons({
                    compiler: "vue3",
                    autoInstall: true // 自动安装图标集
                  }
          )],
      },
    },
  }
})

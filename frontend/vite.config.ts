/**
 * @file ViteConfig
 * @project SlothNote
 * @module 前端构建 / Vite
 * @description 配置 Vue 构建、别名、Element Plus 按需导入和生产压缩。
 * @logic 1. 自动导入 Element Plus 组件/API；2. 仅解析模板实际使用的图标；3. 为手动组件导入补齐按需样式。
 * @dependencies Vite, @vitejs/plugin-vue, unplugin-auto-import, unplugin-vue-components, unplugin-element-plus
 * @index_tags Vite, ElementPlus按需导入, 图标按需加载, 构建优化
 * @author holic512
 */
import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';
import AutoImport from 'unplugin-auto-import/vite';
import Components from 'unplugin-vue-components/vite';
import {ElementPlusResolver} from 'unplugin-vue-components/resolvers';
import ElementPlus from 'unplugin-element-plus/vite';
import * as ElementPlusIcons from '@element-plus/icons-vue';

const elementPlusIconNames = new Set(Object.keys(ElementPlusIcons));
const elementPlusIconResolver = (name: string) => {
  if (!elementPlusIconNames.has(name)) return undefined;
  return {name, from: '@element-plus/icons-vue'};
};

export default defineConfig({
  optimizeDeps: {
    entries: ['index.html', 'src/**/*.{vue,ts,tsx}'],
    holdUntilCrawlEnd: true,
  },
  plugins: [
    vue(),
    AutoImport({
      resolvers: [ElementPlusResolver()],
      dts: 'src/auto-imports.d.ts',
    }),
    Components({
      resolvers: [ElementPlusResolver(), elementPlusIconResolver],
      dts: 'src/components.d.ts',
    }),
    // 为现有手动 Element Plus import 自动补齐对应样式。
    ElementPlus(),
  ],
  resolve: {
    alias: {
      '@': resolve(__dirname, 'src'), // 确保这个路径是正确的
    },
  },

  // 定义构建选项，构建输出将放在其中，如果目录存在，它将在构建之前被删除
  // 指定输出目录为 'dist',并使用 Terser 进行代码压缩
  build: {
    outDir: 'dist',
    minify: 'terser'
  },

});

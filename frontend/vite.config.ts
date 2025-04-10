import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import { resolve } from 'path';

export default defineConfig({
  plugins: [vue()],
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

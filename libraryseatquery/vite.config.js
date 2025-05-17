import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

// https://vite.dev/config/
export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  server: {
    proxy: {
      // 代理API請求到台北市圖書館
      '/api/taipei': {
        target: 'https://seat.tpml.edu.tw',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api\/taipei/, '/sm/service')
      },
      // 代理API請求到後端服務
      '/api/library': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})

import { fileURLToPath, URL } from 'node:url'
import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

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
    host: '0.0.0.0',      // ← bind to all interfaces
    port: 5173,           // ← pick your preferred port
    proxy: {
      '/api/taipei': {
        target: 'https://seat.tpml.edu.tw',
        changeOrigin: true,
        rewrite: path => path.replace(/^\/api\/taipei/, '/sm/service')
      },
      '/api/library': {
        target: 'http://localhost:8080',
        changeOrigin: true
      }
    }
  }
})

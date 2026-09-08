import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import Components from 'unplugin-vue-components/vite'
import { AntDesignVueResolver } from 'unplugin-vue-components/resolvers'
import { resolve } from 'node:path'

const serverPort = 8889

export default defineConfig({
  plugins: [
    vue(),
    Components({
      dirs: [],
      resolvers: [AntDesignVueResolver({ importStyle: false })]
    })
  ],
  resolve: {
    alias: {
      '@': resolve(import.meta.dirname, 'src')
    }
  },
  server: {
    host: '127.0.0.1',
    port: 8800,
    open: true,
    compress: false,
    proxy: {
      '/api': {
        target: `http://localhost:${serverPort}`,
        changeOrigin: true
      },
      '/files': {
        target: `http://localhost:${serverPort}`,
        changeOrigin: true
      }
    }
  },
  css: {
    preprocessorOptions: {
      scss: {
        silenceDeprecations: ['legacy-js-api'],
        api: 'modern-compiler'
      }
    }
  },
  define: {
    'import.meta.env.VITE_APP_BASE_API': JSON.stringify('/api')
  }
})

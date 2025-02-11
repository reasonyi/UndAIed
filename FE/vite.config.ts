import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    port: 5173,
    watch: {
      usePolling: true
    },
    cors: true,
    strictPort: true,
    // hmr: {
    //   clientPort: 5173
    // },
    hmr:false,
    // 명시적으로 허용할 호스트 추가
    allowedHosts: ['i12b212.p.ssafy.io', 'localhost']
  }
})
// import { defineConfig } from 'vite'
// import react from '@vitejs/plugin-react'

// // https://vitejs.dev/config/
// export default defineConfig({
//   plugins: [react()],
//   server: {
//     host: true,
//     port: 5173,
//     watch: {
//       usePolling: true
//     },
//     cors: true,
//     strictPort: true,
//     // hmr: {
//     //   clientPort: 5173
//     // },
//     hmr:false,
//     // 명시적으로 허용할 호스트 추가
//     allowedHosts: ['i12b212.p.ssafy.io', 'localhost']
//   }
// })


import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

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
    hmr: {
      protocol: 'wss',
      host: 'i12b212.p.ssafy.io',
      clientPort: 443
    },
    proxy: {
      '/api': {
        target: 'https://i12b212.p.ssafy.io',
        changeOrigin: true,
        secure: false
      },
      '/socket.io': {
        target: 'https://i12b212.p.ssafy.io',
        changeOrigin: true,
        ws: true,
        secure: false
      }
    },
    allowedHosts: ['i12b212.p.ssafy.io', 'localhost']
  }
})
import { defineConfig } from 'vite';
import react from '@vitejs/plugin-react';

// 프록시 없이 게이트웨이로 바로 호출하는 구성을 유지한다.
export default defineConfig({
  plugins: [react()],
  server: {
    host: true,
    allowedHosts: true,
    port: 5173,
  },
});

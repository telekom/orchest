import react from "@vitejs/plugin-react-swc";
import { defineConfig } from "vitest/config";
import { visualizer } from 'rollup-plugin-visualizer';

export default defineConfig(() => ({
  server: {
    host: "::",
    port: 8080,
  },
  plugins: [
    react(),
    visualizer({
      open: false, // Don't auto-open browser
      gzipSize: true,
      brotliSize: true,
      filename: './dist/stats.html',
    }),
  ],
  resolve: {
    alias: {
      "@": "/src",
    },
  },
  optimizeDeps: {
    exclude: ["@flowskin-bpmn/flowskin-bpmn"],
  },
  build: {
    target: "esnext",
  },
  test: {
    environment: "jsdom",
    include: ["src/**/*.{test,spec}.{ts,tsx}"],
  },
}));

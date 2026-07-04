import { defineConfig } from "vite";
import react from "@vitejs/plugin-react";
import path from "path";

// React SPA build config. The dev server proxies /api to the Spring backend
// so the browser never talks to target domains directly (see CLAUDE.md).
export default defineConfig({
  plugins: [react()],
  resolve: {
    alias: {
      "@": path.resolve(__dirname, "./src"),
    },
  },
  server: {
    port: 5173,
    proxy: {
      "/api": {
        target: "http://localhost:8080",
        changeOrigin: true,
      },
    },
  },
});

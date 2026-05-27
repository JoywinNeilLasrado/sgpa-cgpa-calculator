import { defineConfig } from 'vite';
import { resolve } from 'path';

export default defineConfig({
  build: {
    outDir: resolve(__dirname, 'src/main/resources/META-INF/resources'),
    emptyOutDir: false, // Keep existing HTML files in resources folder
    rollupOptions: {
      input: {
        'js/core': resolve(__dirname, 'frontend/src/js/core.js'),
        'js/api-v2': resolve(__dirname, 'frontend/src/js/api-v2.js'),
        'js/admin': resolve(__dirname, 'frontend/src/js/admin.js'),
        'js/faculty': resolve(__dirname, 'frontend/src/js/faculty.js'),
        'js/analytics': resolve(__dirname, 'frontend/src/js/analytics.js'),
        'js/transcript': resolve(__dirname, 'frontend/src/js/transcript.js'),
        'css/theme': resolve(__dirname, 'frontend/src/scss/theme.scss')
      },
      output: {
        entryFileNames: '[name].js',
        chunkFileNames: 'js/chunks/[name].js',
        assetFileNames: (assetInfo) => {
          if (assetInfo.name && assetInfo.name.endsWith('.css')) {
            return 'css/theme.css'; // Keep original filename for transparent compatibility
          }
          return 'assets/[name].[ext]';
        }
      }
    }
  }
});

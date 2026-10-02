import { defineConfig } from 'vite';
import { vanillaExtractPlugin } from '@vanilla-extract/vite-plugin';
export default defineConfig({plugins:[vanillaExtractPlugin()],server:{port:5175,strictPort:true}});

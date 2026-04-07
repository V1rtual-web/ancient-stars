<template>
  <div class="loading-spinner" :class="{ 'fullscreen': fullscreen }">
    <el-icon class="is-loading" :size="size">
      <Loading />
    </el-icon>
    <p v-if="text" class="loading-text">{{ text }}</p>
  </div>
</template>

<script setup>
import { Loading } from '@element-plus/icons-vue'

defineProps({
  fullscreen: {
    type: Boolean,
    default: false
  },
  size: {
    type: Number,
    default: 40
  },
  text: {
    type: String,
    default: '加载中...'
  }
})
</script>

<style scoped>
.loading-spinner {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px;
  color: #409EFF;
}

.loading-spinner.fullscreen {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(255, 255, 255, 0.9);
  z-index: 9999;
}

.loading-text {
  margin-top: 15px;
  font-size: 14px;
  color: #606266;
}
</style>

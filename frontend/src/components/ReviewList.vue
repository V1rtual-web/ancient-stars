<template>
  <div class="review-list-component">
    <!-- 待复习列表 -->
    <div v-if="reviewReminders.length > 0" class="review-grid">
      <el-card
        v-for="reminder in reviewReminders"
        :key="reminder.id"
        class="review-card"
        shadow="hover"
      >
        <div class="card-content">
          <!-- 词汇信息 -->
          <div class="vocabulary-info">
            <h3 class="word">{{ reminder.vocabulary.word }}</h3>
            <p v-if="reminder.vocabulary.phonetic" class="phonetic">
              {{ reminder.vocabulary.phonetic }}
            </p>
            <p class="translation">{{ reminder.vocabulary.translation }}</p>
          </div>

          <!-- 学习时间信息 -->
          <div class="study-info">
            <el-tag :type="getDaysTagType(reminder.daysSinceLastStudy)" size="large">
              <el-icon><Clock /></el-icon>
              学习于 {{ reminder.daysSinceLastStudy }} 天前
            </el-tag>
          </div>

          <!-- 操作按钮 -->
          <div class="card-actions">
            <el-button
              type="primary"
              size="large"
              @click="handleStartReview(reminder)"
              :loading="reviewingId === reminder.id"
            >
              <el-icon><Reading /></el-icon>
              开始复习
            </el-button>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-else
      description="暂无待复习词汇"
      :image-size="150"
    >
      <template #description>
        <p class="empty-text">太棒了！目前没有需要复习的词汇</p>
        <p class="empty-subtext">继续保持学习，系统会在合适的时间提醒你复习</p>
      </template>
    </el-empty>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { Clock, Reading } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  reviewReminders: {
    type: Array,
    required: true,
    default: () => []
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['startReview'])

// 状态
const reviewingId = ref(null)

// 根据天数获取标签类型
const getDaysTagType = (days) => {
  if (days <= 7) {
    return 'success'
  } else if (days <= 14) {
    return 'warning'
  } else {
    return 'danger'
  }
}

// 处理开始复习
const handleStartReview = (reminder) => {
  reviewingId.value = reminder.id
  emit('startReview', reminder)
  
  // 延迟重置状态
  setTimeout(() => {
    reviewingId.value = null
  }, 500)
}
</script>

<style scoped>
.review-list-component {
  width: 100%;
}

/* 复习网格 */
.review-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 20px;
}

/* 复习卡片 */
.review-card {
  transition: all 0.3s ease;
  border: 2px solid transparent;
}

.review-card:hover {
  transform: translateY(-4px);
  border-color: #409EFF;
}

.review-card :deep(.el-card__body) {
  padding: 0;
}

.card-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
  padding: 24px;
}

/* 词汇信息 */
.vocabulary-info {
  flex: 1;
  padding-bottom: 16px;
  border-bottom: 2px solid #EBEEF5;
}

.word {
  margin: 0 0 8px 0;
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  word-break: break-word;
}

.phonetic {
  margin: 0 0 12px 0;
  font-size: 16px;
  color: #909399;
  font-style: italic;
}

.translation {
  margin: 0;
  font-size: 18px;
  color: #606266;
  line-height: 1.6;
}

/* 学习时间信息 */
.study-info {
  display: flex;
  justify-content: center;
  padding: 12px 0;
}

.study-info .el-tag {
  padding: 10px 16px;
  font-size: 14px;
  font-weight: 500;
}

.study-info .el-icon {
  margin-right: 6px;
}

/* 操作按钮 */
.card-actions {
  padding-top: 16px;
  border-top: 2px solid #EBEEF5;
}

.card-actions .el-button {
  width: 100%;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

/* 空状态 */
.empty-text {
  font-size: 18px;
  color: #606266;
  margin: 10px 0;
}

.empty-subtext {
  font-size: 14px;
  color: #909399;
  margin: 5px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .review-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .card-content {
    padding: 20px;
    gap: 16px;
  }

  .word {
    font-size: 24px;
  }

  .phonetic {
    font-size: 14px;
  }

  .translation {
    font-size: 16px;
  }

  .card-actions .el-button {
    height: 44px;
    font-size: 15px;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .review-grid {
    grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  }
}
</style>

<template>
  <div class="study-card-container">
    <!-- 进度指示器 -->
    <div class="progress-indicator">
      <span class="current">{{ currentIndex + 1 }}</span>
      <span class="separator">/</span>
      <span class="total">{{ vocabularies.length }}</span>
    </div>

    <!-- 卡片容器 -->
    <div 
      class="card-wrapper"
      @touchstart="handleTouchStart"
      @touchmove="handleTouchMove"
      @touchend="handleTouchEnd"
    >
      <transition :name="transitionName" mode="out-in">
        <div 
          v-if="currentVocabulary" 
          :key="currentVocabulary.id"
          class="study-card"
        >
          <!-- 复习标记 -->
          <div v-if="currentVocabulary.needsReview" class="review-badge">
            📅 今日复习 · 已学 {{ currentVocabulary.reviewCount }} 次
          </div>

          <!-- 单词 -->
          <div class="word-section">
            <h1 class="word">{{ currentVocabulary.word }}</h1>
            <p v-if="currentVocabulary.phonetic" class="phonetic">
              {{ currentVocabulary.phonetic }}
            </p>
          </div>

          <!-- 释义 -->
          <div class="translation-section">
            <p class="translation">{{ currentVocabulary.translation }}</p>
          </div>

          <!-- 例句 -->
          <div v-if="currentVocabulary.example" class="example-section">
            <div class="example-label">例句</div>
            <p class="example-text">{{ currentVocabulary.example }}</p>
          </div>

          <!-- 操作按钮 -->
          <div class="actions">
            <template v-if="currentVocabulary.needsReview">
              <el-button
                type="success"
                size="large"
                :loading="isMarking"
                @click="handleRemembered"
                class="learned-button"
              >
                <el-icon v-if="!isMarking"><Check /></el-icon>
                还记得 ✓
              </el-button>
              <el-button
                type="danger"
                size="large"
                :loading="isMarking"
                @click="handleForgotten"
                class="learned-button"
                plain
              >
                忘了 ✗
              </el-button>
            </template>
            <template v-else>
              <el-button
                type="primary"
                size="large"
                :loading="isMarking"
                @click="handleMarkAsLearned"
                class="learned-button"
              >
                <el-icon v-if="!isMarking"><Check /></el-icon>
                已掌握
              </el-button>
            </template>
          </div>
        </div>
      </transition>
    </div>

    <!-- 导航按钮 -->
    <div class="navigation">
      <el-button
        :disabled="currentIndex === 0"
        @click="handlePrevious"
        circle
        size="large"
      >
        <el-icon><ArrowLeft /></el-icon>
      </el-button>

      <el-button
        :disabled="currentIndex === vocabularies.length - 1"
        @click="handleNext"
        circle
        size="large"
      >
        <el-icon><ArrowRight /></el-icon>
      </el-button>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-if="vocabularies.length === 0"
      description="暂无词汇"
      :image-size="120"
    />
  </div>
</template>

<script setup>
import { ref, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Check, ArrowLeft, ArrowRight } from '@element-plus/icons-vue'
import { selfStudyAPI } from '../api/selfStudy'

// Props
const props = defineProps({
  vocabularies: {
    type: Array,
    required: true,
    default: () => []
  },
  initialIndex: {
    type: Number,
    default: 0
  }
})

// Emits
const emit = defineEmits(['learned', 'indexChange', 'groupFinished', 'forgotten'])

// 状态
const currentIndex = ref(props.initialIndex)
const isMarking = ref(false)
const transitionName = ref('slide-left')

// 触摸事件相关
const touchStartX = ref(0)
const touchEndX = ref(0)

// 当前词汇
const currentVocabulary = computed(() => {
  return props.vocabularies[currentIndex.value] || null
})

// 处理"已掌握"按钮点击（新词）
const handleMarkAsLearned = async () => {
  if (!currentVocabulary.value || isMarking.value) return
  try {
    isMarking.value = true
    await selfStudyAPI.createStudyRecord(currentVocabulary.value.id)
    emit('learned', currentVocabulary.value.id)
    if (currentIndex.value === props.vocabularies.length - 1) {
      setTimeout(() => emit('groupFinished'), 300)
    } else {
      setTimeout(() => handleNext(), 500)
    }
  } catch (error) {
    ElMessage.error('标记失败，请稍后重试')
  } finally {
    isMarking.value = false
  }
}

// 处理"还记得"（复习词汇）
const handleRemembered = async () => {
  if (!currentVocabulary.value || isMarking.value) return
  try {
    isMarking.value = true
    await selfStudyAPI.createStudyRecord(currentVocabulary.value.id)
    ElMessage.success('很好！记忆巩固 +1 💪')
    emit('learned', currentVocabulary.value.id)
    if (currentIndex.value === props.vocabularies.length - 1) {
      setTimeout(() => emit('groupFinished'), 300)
    } else {
      setTimeout(() => handleNext(), 500)
    }
  } catch (error) {
    ElMessage.error('操作失败，请稍后重试')
  } finally {
    isMarking.value = false
  }
}

// 处理"忘了"（复习词汇）
const handleForgotten = async () => {
  if (!currentVocabulary.value || isMarking.value) return
  // 忘了不调用 API，只是跳过，让用户多看几遍
  ElMessage.warning('没关系，多看几遍就记住了 📖')
  // 把当前词移到末尾，让用户稍后再看
  emit('forgotten', currentVocabulary.value.id)
  if (currentIndex.value < props.vocabularies.length - 1) {
    handleNext()
  }
}

// 切换到上一个词汇
const handlePrevious = () => {
  if (currentIndex.value > 0) {
    transitionName.value = 'slide-right'
    currentIndex.value--
    emit('indexChange', currentIndex.value)
  }
}

// 切换到下一个词汇
const handleNext = () => {
  if (currentIndex.value < props.vocabularies.length - 1) {
    transitionName.value = 'slide-left'
    currentIndex.value++
    emit('indexChange', currentIndex.value)
  } else {
    // 已是最后一张，通知父组件
    emit('groupFinished')
  }
}

// 触摸开始
const handleTouchStart = (e) => {
  touchStartX.value = e.touches[0].clientX
}

// 触摸移动
const handleTouchMove = (e) => {
  touchEndX.value = e.touches[0].clientX
}

// 触摸结束
const handleTouchEnd = () => {
  const diff = touchStartX.value - touchEndX.value
  const threshold = 50 // 滑动阈值

  if (Math.abs(diff) > threshold) {
    if (diff > 0) {
      // 向左滑动，显示下一个
      handleNext()
    } else {
      // 向右滑动，显示上一个
      handlePrevious()
    }
  }

  // 重置
  touchStartX.value = 0
  touchEndX.value = 0
}
</script>

<style scoped>
.study-card-container {
  max-width: 600px;
  margin: 0 auto;
  padding: 20px;
}

/* 进度指示器 */
.progress-indicator {
  text-align: center;
  margin-bottom: 20px;
  font-size: 18px;
  color: #606266;
}

.progress-indicator .current {
  font-weight: bold;
  color: #409EFF;
  font-size: 24px;
}

.progress-indicator .separator {
  margin: 0 8px;
  color: #909399;
}

.progress-indicator .total {
  color: #909399;
}

/* 卡片包装器 */
.card-wrapper {
  position: relative;
  min-height: 500px;
  margin-bottom: 30px;
}

/* 学习卡片 */
.study-card {
  background: #fff;
  border-radius: 16px;
  padding: 40px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  gap: 30px;
  min-height: 500px;
}

/* 单词部分 */
.word-section {
  text-align: center;
  padding-bottom: 20px;
  border-bottom: 3px solid #409EFF;
}

.word {
  margin: 0 0 12px 0;
  font-size: 48px;
  font-weight: bold;
  color: #303133;
  word-break: break-word;
}

.phonetic {
  margin: 0;
  font-size: 20px;
  color: #909399;
  font-style: italic;
}

/* 释义部分 */
.translation-section {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
}

.translation {
  margin: 0;
  font-size: 24px;
  color: #606266;
  line-height: 1.8;
  text-align: center;
}

/* 例句部分 */
.example-section {
  padding: 20px;
  background: linear-gradient(135deg, #f5f7fa 0%, #e8eef5 100%);
  border-radius: 12px;
  border-left: 4px solid #409EFF;
}

.example-label {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #909399;
  font-weight: 600;
  text-transform: uppercase;
  letter-spacing: 1px;
}

.example-text {
  margin: 0;
  font-size: 18px;
  color: #606266;
  line-height: 1.8;
  font-style: italic;
}

/* 复习标记 */
.review-badge {
  text-align: center;
  padding: 6px 16px;
  background: linear-gradient(135deg, #fff3cd, #ffeaa7);
  border-radius: 20px;
  font-size: 13px;
  color: #856404;
  font-weight: 600;
  border: 1px solid #ffc107;
  align-self: center;
}

/* 操作按钮 */
.actions {
  padding-top: 20px;
  border-top: 2px solid #EBEEF5;
  display: flex;
  gap: 12px;
}

.learned-button {
  flex: 1;
  height: 56px;
  font-size: 18px;
  font-weight: 600;
}

/* 导航按钮 */
.navigation {
  display: flex;
  justify-content: center;
  gap: 40px;
}

.navigation .el-button {
  width: 56px;
  height: 56px;
  font-size: 24px;
}

/* 过渡动画 - 向左滑动 */
.slide-left-enter-active,
.slide-left-leave-active {
  transition: all 0.3s ease;
}

.slide-left-enter-from {
  transform: translateX(100%);
  opacity: 0;
}

.slide-left-leave-to {
  transform: translateX(-100%);
  opacity: 0;
}

/* 过渡动画 - 向右滑动 */
.slide-right-enter-active,
.slide-right-leave-active {
  transition: all 0.3s ease;
}

.slide-right-enter-from {
  transform: translateX(-100%);
  opacity: 0;
}

.slide-right-leave-to {
  transform: translateX(100%);
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .study-card-container {
    padding: 10px;
  }

  .study-card {
    padding: 30px 20px;
    min-height: 450px;
  }

  .word {
    font-size: 36px;
  }

  .phonetic {
    font-size: 18px;
  }

  .translation {
    font-size: 20px;
  }

  .example-text {
    font-size: 16px;
  }

  .learned-button {
    height: 48px;
    font-size: 16px;
  }

  .navigation .el-button {
    width: 48px;
    height: 48px;
    font-size: 20px;
  }
}

@media (max-width: 480px) {
  .word {
    font-size: 28px;
  }

  .phonetic {
    font-size: 16px;
  }

  .translation {
    font-size: 18px;
  }

  .example-section {
    padding: 15px;
  }

  .example-text {
    font-size: 14px;
  }
}
</style>

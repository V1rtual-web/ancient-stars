<template>
  <div class="review-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <el-button
          :icon="ArrowLeft"
          @click="handleBack"
          circle
        />
        <h2>{{ isReviewing ? '复习中' : '待复习词汇' }}</h2>
      </div>
      <div class="header-right">
        <el-tag v-if="!isReviewing && reviewReminders.length > 0" type="primary" size="large">
          待复习: {{ reviewReminders.length }} 个
        </el-tag>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 内容区域 -->
    <div v-else>
      <!-- 待复习列表视图 -->
      <div v-if="!isReviewing" class="list-view">
        <ReviewList
          :review-reminders="reviewReminders"
          :loading="loading"
          @start-review="handleStartReview"
        />
      </div>

      <!-- 复习卡片视图 -->
      <div v-else class="review-view">
        <!-- 复习进度 -->
        <div class="review-progress">
          <el-progress
            :percentage="reviewProgressPercentage"
            :stroke-width="8"
            :color="progressColor"
          >
            <template #default="{ percentage }">
              <span class="progress-text">{{ percentage }}%</span>
            </template>
          </el-progress>
          <p class="progress-info">
            已复习 {{ currentReviewIndex }} / {{ reviewVocabularies.length }}
          </p>
        </div>

        <!-- 学习卡片 -->
        <StudyCard
          v-if="reviewVocabularies.length > 0"
          :vocabularies="reviewVocabularies"
          :initial-index="currentCardIndex"
          @learned="handleReviewComplete"
          @index-change="handleCardIndexChange"
        />

        <!-- 操作按钮 -->
        <div class="review-actions">
          <el-button
            size="large"
            @click="handleCancelReview"
          >
            <el-icon><Close /></el-icon>
            退出复习
          </el-button>
          <el-button
            type="primary"
            size="large"
            :loading="completing"
            @click="handleCompleteCurrentReview"
          >
            <el-icon><Check /></el-icon>
            完成复习
          </el-button>
        </div>
      </div>
    </div>

    <!-- 完成复习对话框 -->
    <el-dialog
      v-model="showCompleteDialog"
      title="复习完成"
      width="500px"
      :close-on-click-modal="false"
    >
      <div class="complete-content">
        <el-result
          icon="success"
          title="太棒了！"
          :sub-title="`你已完成 ${completedCount} 个单词的复习`"
        >
          <template #icon>
            <el-icon :size="80" color="#67C23A"><SuccessFilled /></el-icon>
          </template>
        </el-result>
        <div class="complete-stats">
          <p>继续保持学习，记忆会更加牢固！</p>
        </div>
      </div>
      <template #footer>
        <el-button type="primary" @click="handleFinishReview">
          完成
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { ArrowLeft, Loading, Check, Close, SuccessFilled } from '@element-plus/icons-vue'
import { useSelfStudyStore } from '../../store/modules/selfStudy'
import ReviewList from '../../components/ReviewList.vue'
import StudyCard from '../../components/StudyCard.vue'

const router = useRouter()
const selfStudyStore = useSelfStudyStore()

const loading = ref(false)
const reviewReminders = ref([])
const isReviewing = ref(false)
const reviewVocabularies = ref([])
const currentCardIndex = ref(0)
const currentReviewIndex = ref(0)
const currentReminderId = ref(null)
const completing = ref(false)
const completedCount = ref(0)
const showCompleteDialog = ref(false)

const reviewProgressPercentage = computed(() => {
  if (reviewVocabularies.value.length === 0) return 0
  return Math.round((currentReviewIndex.value / reviewVocabularies.value.length) * 100)
})

const progressColor = computed(() => {
  const percentage = reviewProgressPercentage.value
  if (percentage < 30) return '#F56C6C'
  if (percentage < 70) return '#E6A23C'
  return '#67C23A'
})

const loadReviewReminders = async () => {
  loading.value = true
  const result = await selfStudyStore.loadReviewReminders()
  loading.value = false
  
  if (result.success) {
    reviewReminders.value = result.data || []
  } else {
    ElMessage.error(result.message || '加载待复习列表失败')
  }
}

const handleStartReview = (reminder) => {
  currentReminderId.value = reminder.id
  reviewVocabularies.value = [reminder.vocabulary]
  currentCardIndex.value = 0
  currentReviewIndex.value = 0
  isReviewing.value = true
}

const handleCardIndexChange = (index) => {
  currentCardIndex.value = index
}

const handleCompleteCurrentReview = async () => {
  if (!currentReminderId.value) {
    ElMessage.warning('请先选择要复习的词汇')
    return
  }

  try {
    completing.value = true
    const result = await selfStudyStore.completeReview(currentReminderId.value)
    
    if (result.success) {
      completedCount.value++
      currentReviewIndex.value++
      reviewReminders.value = reviewReminders.value.filter(
        r => r.id !== currentReminderId.value
      )
      ElMessage.success('复习完成！')
      
      if (reviewReminders.value.length > 0) {
        isReviewing.value = false
        currentReminderId.value = null
        reviewVocabularies.value = []
      } else {
        showCompleteDialog.value = true
      }
    } else {
      ElMessage.error(result.message || '完成复习失败')
    }
  } catch (error) {
    console.error('完成复习失败:', error)
    ElMessage.error('完成复习失败，请稍后重试')
  } finally {
    completing.value = false
  }
}

const handleReviewComplete = async (vocabularyId) => {
  await handleCompleteCurrentReview()
}

const handleCancelReview = async () => {
  try {
    await ElMessageBox.confirm(
      '确定要退出复习吗？当前进度不会保存。',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    isReviewing.value = false
    currentReminderId.value = null
    reviewVocabularies.value = []
    currentCardIndex.value = 0
    currentReviewIndex.value = 0
  } catch {
    // 用户取消
  }
}

const handleFinishReview = () => {
  showCompleteDialog.value = false
  
  if (reviewReminders.value.length > 0) {
    isReviewing.value = false
  } else {
    router.push('/student/self-study')
  }
}

const handleBack = () => {
  if (isReviewing.value) {
    handleCancelReview()
  } else {
    router.back()
  }
}

onMounted(() => {
  loadReviewReminders()
})
</script>

<style scoped>
.review-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #EBEEF5;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 15px;
}

.header-left h2 {
  margin: 0;
  font-size: 28px;
  color: #303133;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 10px;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 400px;
  color: #909399;
}

.loading-container p {
  margin-top: 15px;
  font-size: 16px;
}

.list-view {
  margin-top: 20px;
}

.review-view {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

.review-progress {
  background: #fff;
  padding: 25px;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.progress-text {
  font-size: 16px;
  font-weight: bold;
  color: #606266;
}

.progress-info {
  margin: 15px 0 0 0;
  text-align: center;
  font-size: 16px;
  color: #606266;
  font-weight: 500;
}

.review-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  padding: 20px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.review-actions .el-button {
  min-width: 150px;
  height: 48px;
  font-size: 16px;
  font-weight: 600;
}

.complete-content {
  text-align: center;
}

.complete-stats {
  margin-top: 20px;
  padding: 20px;
  background: #F5F7FA;
  border-radius: 8px;
}

.complete-stats p {
  margin: 0;
  font-size: 16px;
  color: #606266;
  line-height: 1.8;
}

@media (max-width: 768px) {
  .review-page {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .header-left h2 {
    font-size: 24px;
  }

  .header-right {
    width: 100%;
    justify-content: flex-start;
  }

  .review-progress {
    padding: 20px;
  }

  .review-actions {
    flex-direction: column;
    gap: 15px;
  }

  .review-actions .el-button {
    width: 100%;
  }
}
</style>

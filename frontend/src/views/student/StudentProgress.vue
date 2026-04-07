<template>
  <div class="student-progress-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2>学习进度</h2>
        <p class="subtitle">记录你的每一步成长</p>
      </div>
      <div class="header-right">
        <el-button
          type="primary"
          :icon="Refresh"
          @click="handleRefresh"
          :loading="loading"
        >
          刷新数据
        </el-button>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading && !progressData" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 进度统计内容 -->
    <div v-else-if="progressData">
      <ProgressStats
        :progress-data="progressData"
        @period-change="handlePeriodChange"
      />

      <!-- 鼓励信息 -->
      <el-card class="encouragement-card">
        <div class="encouragement-content">
          <el-icon :size="50" color="#409EFF"><Trophy /></el-icon>
          <div class="encouragement-text">
            <h3>{{ getEncouragementTitle() }}</h3>
            <p>{{ getEncouragementMessage() }}</p>
          </div>
        </div>
      </el-card>

      <!-- 学习建议 -->
      <el-card class="tips-card">
        <template #header>
          <div class="card-header">
            <el-icon><Memo /></el-icon>
            <span>学习小贴士</span>
          </div>
        </template>
        <div class="tips-content">
          <ul class="tips-list">
            <li>
              <el-icon color="#409EFF"><Check /></el-icon>
              <span>每天坚持学习10-20个新单词，效果最佳</span>
            </li>
            <li>
              <el-icon color="#67C23A"><Check /></el-icon>
              <span>按时复习是记忆的关键，不要错过复习提醒</span>
            </li>
            <li>
              <el-icon color="#E6A23C"><Check /></el-icon>
              <span>利用碎片时间学习，积少成多</span>
            </li>
            <li>
              <el-icon color="#F56C6C"><Check /></el-icon>
              <span>结合例句理解单词，记忆更牢固</span>
            </li>
          </ul>
        </div>
      </el-card>
    </div>

    <!-- 错误状态 -->
    <el-empty
      v-else
      description="加载失败"
      :image-size="120"
    >
      <el-button type="primary" @click="loadProgress">
        重新加载
      </el-button>
    </el-empty>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Loading, Trophy, Memo, Check } from '@element-plus/icons-vue'
import { useSelfStudyStore } from '../../store/modules/selfStudy'
import ProgressStats from '../../components/ProgressStats.vue'

// Store
const selfStudyStore = useSelfStudyStore()

// 状态
const loading = ref(false)
const progressData = ref(null)
const currentPeriod = ref(7) // 默认显示最近7天

// 加载学习进度
const loadProgress = async () => {
  loading.value = true
  
  const params = {
    // 根据当前时间段计算日期范围
    startDate: getStartDate(currentPeriod.value),
    endDate: new Date().toISOString().split('T')[0]
  }
  
  const result = await selfStudyStore.loadProgress(params)
  loading.value = false
  
  if (result.success) {
    progressData.value = result.data
  } else {
    ElMessage.error(result.message || '加载学习进度失败')
  }
}

// 获取开始日期
const getStartDate = (days) => {
  const date = new Date()
  date.setDate(date.getDate() - days)
  return date.toISOString().split('T')[0]
}

// 处理刷新
const handleRefresh = () => {
  loadProgress()
}

// 处理时间段变化
const handlePeriodChange = (period) => {
  currentPeriod.value = period
  loadProgress()
}

// 获取鼓励标题
const getEncouragementTitle = () => {
  const todayWords = progressData.value?.todayNewWords || 0
  const totalWords = progressData.value?.totalLearnedWords || 0
  
  if (totalWords === 0) {
    return '开始你的学习之旅吧！'
  } else if (totalWords < 50) {
    return '良好的开端！'
  } else if (totalWords < 200) {
    return '进步显著！'
  } else if (totalWords < 500) {
    return '学习达人！'
  } else {
    return '词汇大师！'
  }
}

// 获取鼓励信息
const getEncouragementMessage = () => {
  const todayWords = progressData.value?.todayNewWords || 0
  const totalWords = progressData.value?.totalLearnedWords || 0
  const weekWords = progressData.value?.weekNewWords || 0
  
  if (todayWords === 0) {
    return '今天还没有学习新单词，开始学习吧！每天进步一点点，成就更好的自己。'
  } else if (todayWords < 10) {
    return `今天已学习 ${todayWords} 个单词，继续加油！本周已累计学习 ${weekWords} 个单词。`
  } else if (todayWords < 20) {
    return `今天已学习 ${todayWords} 个单词，表现不错！你已经掌握了 ${totalWords} 个单词，继续保持！`
  } else {
    return `今天已学习 ${todayWords} 个单词，太棒了！你的努力正在积累成果，累计已掌握 ${totalWords} 个单词！`
  }
}

// 初始化
onMounted(() => {
  loadProgress()
})
</script>

<style scoped>
.student-progress-page {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* 页面标题 */
.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding-bottom: 20px;
  border-bottom: 2px solid #EBEEF5;
}

.header-left h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #303133;
}

.header-left .subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
}

.header-right {
  display: flex;
  gap: 10px;
}

/* 加载状态 */
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

/* 鼓励卡片 */
.encouragement-card {
  margin-top: 25px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border: none;
}

.encouragement-card :deep(.el-card__body) {
  padding: 30px;
}

.encouragement-content {
  display: flex;
  align-items: center;
  gap: 25px;
  color: #fff;
}

.encouragement-text h3 {
  margin: 0 0 10px 0;
  font-size: 24px;
  font-weight: 600;
}

.encouragement-text p {
  margin: 0;
  font-size: 16px;
  line-height: 1.6;
  opacity: 0.95;
}

/* 学习建议卡片 */
.tips-card {
  margin-top: 25px;
}

.tips-card :deep(.el-card__header) {
  padding: 20px 25px;
  border-bottom: 2px solid #EBEEF5;
}

.tips-card .card-header {
  display: flex;
  align-items: center;
  gap: 10px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.tips-content {
  padding: 10px 0;
}

.tips-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.tips-list li {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 15px 0;
  font-size: 15px;
  color: #606266;
  border-bottom: 1px solid #EBEEF5;
}

.tips-list li:last-child {
  border-bottom: none;
}

.tips-list li .el-icon {
  font-size: 18px;
  flex-shrink: 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .student-progress-page {
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
  }

  .header-right .el-button {
    width: 100%;
  }

  .encouragement-card :deep(.el-card__body) {
    padding: 25px 20px;
  }

  .encouragement-content {
    flex-direction: column;
    text-align: center;
    gap: 20px;
  }

  .encouragement-text h3 {
    font-size: 20px;
  }

  .encouragement-text p {
    font-size: 14px;
  }

  .tips-list li {
    font-size: 14px;
    padding: 12px 0;
  }
}

@media (max-width: 480px) {
  .header-left h2 {
    font-size: 20px;
  }

  .encouragement-card :deep(.el-card__body) {
    padding: 20px 15px;
  }

  .encouragement-text h3 {
    font-size: 18px;
  }

  .encouragement-text p {
    font-size: 13px;
  }
}
</style>

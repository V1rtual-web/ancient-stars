<template>
  <div class="student-tasks">
    <div class="page-header">
      <h1>我的学习任务</h1>
      <p class="subtitle">查看和管理您的学习任务</p>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>

    <!-- 空状态 -->
    <el-empty
      v-else-if="tasks.length === 0"
      description="暂无学习任务"
      :image-size="200"
    />

    <!-- 任务列表 -->
    <div v-else class="tasks-container">
      <el-card
        v-for="task in tasks"
        :key="task.id"
        class="task-card"
        shadow="hover"
      >
        <div class="task-header">
          <div class="task-title-section">
            <h2 class="task-title">{{ task.taskTitle }}</h2>
            <el-tag :type="getStatusType(task.status)" size="large">
              {{ getStatusLabel(task.status) }}
            </el-tag>
          </div>
          <div class="task-meta">
            <span class="meta-item">
              <el-icon><Document /></el-icon>
              {{ task.wordListName }}
            </span>
            <span class="meta-item">
              <el-icon><Reading /></el-icon>
              {{ task.totalWords }} 个词汇
            </span>
          </div>
        </div>

        <el-divider />

        <div class="task-body">
          <p v-if="task.description" class="task-description">
            {{ task.description }}
          </p>

          <!-- 进度条 -->
          <div class="progress-section">
            <div class="progress-header">
              <span class="progress-label">学习进度</span>
              <span class="progress-percentage">{{ task.progress }}%</span>
            </div>
            <el-progress
              :percentage="task.progress"
              :color="getProgressColor(task.progress)"
              :stroke-width="12"
            />
            <div class="progress-detail">
              已掌握 {{ getMasteredCount(task) }} / {{ task.totalWords }} 个词汇
            </div>
          </div>

          <!-- 截止日期和倒计时 -->
          <div class="deadline-section">
            <div class="deadline-item">
              <el-icon><Calendar /></el-icon>
              <span class="deadline-label">截止日期：</span>
              <span class="deadline-value">{{ formatDeadline(task.deadline) }}</span>
            </div>
            <div class="countdown-item">
              <el-icon><Clock /></el-icon>
              <span class="countdown-label">剩余时间：</span>
              <span
                class="countdown-value"
                :class="getCountdownClass(task.deadline)"
              >
                {{ getCountdown(task.deadline) }}
              </span>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 操作按钮 -->
        <div class="task-actions">
          <el-button
            v-if="task.status === 'NOT_STARTED'"
            type="primary"
            size="large"
            @click="handleStartLearning(task)"
          >
            <el-icon><VideoPlay /></el-icon>
            开始学习
          </el-button>
          <el-button
            v-else-if="task.status === 'IN_PROGRESS'"
            type="warning"
            size="large"
            @click="handleContinueLearning(task)"
          >
            <el-icon><VideoPlay /></el-icon>
            继续学习
          </el-button>
          <el-button
            v-else-if="task.status === 'COMPLETED'"
            type="success"
            size="large"
            disabled
          >
            <el-icon><CircleCheck /></el-icon>
            已完成
          </el-button>
          <el-button
            size="large"
            @click="handleViewDetail(task)"
          >
            <el-icon><View /></el-icon>
            查看详情
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 任务详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      title="任务详情"
      width="600px"
    >
      <div v-if="currentTask" class="task-detail">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="任务标题">
            {{ currentTask.taskTitle }}
          </el-descriptions-item>
          <el-descriptions-item label="词汇表">
            {{ currentTask.wordListName }}
          </el-descriptions-item>
          <el-descriptions-item label="词汇数量">
            {{ currentTask.totalWords }} 个
          </el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusType(currentTask.status)">
              {{ getStatusLabel(currentTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="学习进度">
            {{ currentTask.progress }}%
          </el-descriptions-item>
          <el-descriptions-item label="已掌握词汇">
            {{ getMasteredCount(currentTask) }} / {{ currentTask.totalWords }}
          </el-descriptions-item>
          <el-descriptions-item label="分配日期">
            {{ formatDeadline(currentTask.assignedAt) }}
          </el-descriptions-item>
          <el-descriptions-item label="截止日期">
            {{ formatDeadline(currentTask.deadline) }}
          </el-descriptions-item>
          <el-descriptions-item label="剩余时间">
            <span :class="getCountdownClass(currentTask.deadline)">
              {{ getCountdown(currentTask.deadline) }}
            </span>
          </el-descriptions-item>
          <el-descriptions-item v-if="currentTask.completedAt" label="完成日期">
            {{ formatDeadline(currentTask.completedAt) }}
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <template #footer>
        <el-button @click="showDetailDialog = false">关闭</el-button>
        <el-button
          v-if="currentTask && currentTask.status !== 'COMPLETED'"
          type="primary"
          @click="handleStartFromDetail"
        >
          开始学习
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import {
  Document,
  Reading,
  Calendar,
  Clock,
  VideoPlay,
  CircleCheck,
  View
} from '@element-plus/icons-vue'
import { taskAPI } from '@/api/task'
import { useUserStore } from '@/store/modules/user'

const router = useRouter()
const userStore = useUserStore()

// 数据
const loading = ref(false)
const tasks = ref([])
const showDetailDialog = ref(false)
const currentTask = ref(null)
let countdownTimer = null

// 加载任务列表
const loadTasks = async () => {
  loading.value = true
  try {
    const studentId = userStore.userInfo?.id
    if (!studentId) {
      ElMessage.error('无法获取学生信息')
      return
    }

    const response = await taskAPI.getStudentTasks(studentId)
    tasks.value = response.data || []
  } catch (error) {
    ElMessage.error('加载任务列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 获取任务状态类型
const getStatusType = (status) => {
  const types = {
    NOT_STARTED: 'info',
    IN_PROGRESS: 'warning',
    COMPLETED: 'success',
    EXPIRED: 'danger'
  }
  return types[status] || 'info'
}

// 获取任务状态标签
const getStatusLabel = (status) => {
  const labels = {
    NOT_STARTED: '未开始',
    IN_PROGRESS: '进行中',
    COMPLETED: '已完成',
    EXPIRED: '已过期'
  }
  return labels[status] || status
}

// 获取进度条颜色
const getProgressColor = (progress) => {
  if (progress === 100) return '#67C23A'
  if (progress >= 60) return '#409EFF'
  if (progress >= 30) return '#E6A23C'
  return '#F56C6C'
}

// 计算已掌握词汇数
const getMasteredCount = (task) => {
  if (!task || !task.totalWords || !task.progress) return 0
  return Math.round((task.progress / 100) * task.totalWords)
}

// 格式化截止日期
const formatDeadline = (deadline) => {
  if (!deadline) return '无截止日期'
  const date = new Date(deadline)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 计算倒计时
const getCountdown = (deadline) => {
  if (!deadline) return '无截止日期'
  
  const now = new Date()
  const end = new Date(deadline)
  const diff = end - now

  if (diff <= 0) return '已过期'

  const days = Math.floor(diff / (1000 * 60 * 60 * 24))
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60))
  const minutes = Math.floor((diff % (1000 * 60 * 60)) / (1000 * 60))

  if (days > 0) {
    return `${days}天${hours}小时`
  } else if (hours > 0) {
    return `${hours}小时${minutes}分钟`
  } else {
    return `${minutes}分钟`
  }
}

// 获取倒计时样式类
const getCountdownClass = (deadline) => {
  if (!deadline) return ''
  
  const now = new Date()
  const end = new Date(deadline)
  const diff = end - now

  if (diff <= 0) return 'countdown-expired'
  
  const days = diff / (1000 * 60 * 60 * 24)
  if (days < 1) return 'countdown-urgent'
  if (days < 3) return 'countdown-warning'
  
  return 'countdown-normal'
}

// 开始学习
const handleStartLearning = (task) => {
  router.push(`/student/learn/${task.taskId}`)
}

// 继续学习
const handleContinueLearning = (task) => {
  router.push(`/student/learn/${task.taskId}`)
}

// 查看详情
const handleViewDetail = (task) => {
  currentTask.value = task
  showDetailDialog.value = true
}

// 从详情对话框开始学习
const handleStartFromDetail = () => {
  if (currentTask.value) {
    showDetailDialog.value = false
    router.push(`/student/learn/${currentTask.value.taskId}`)
  }
}

// 启动倒计时定时器（每分钟更新一次）
const startCountdownTimer = () => {
  countdownTimer = setInterval(() => {
    // 强制更新组件以刷新倒计时显示
    tasks.value = [...tasks.value]
  }, 60000) // 每60秒更新一次
}

// 初始化
onMounted(() => {
  loadTasks()
  startCountdownTimer()
})

// 清理定时器
onUnmounted(() => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
  }
})
</script>

<style scoped>
.student-tasks {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 32px;
}

.page-header h1 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
}

.subtitle {
  font-size: 14px;
  color: #909399;
  margin: 0;
}

.loading-container {
  padding: 40px;
}

.tasks-container {
  display: grid;
  gap: 24px;
}

.task-card {
  transition: all 0.3s ease;
}

.task-card:hover {
  transform: translateY(-4px);
}

.task-header {
  margin-bottom: 16px;
}

.task-title-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 12px;
}

.task-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.task-meta {
  display: flex;
  gap: 24px;
  color: #606266;
  font-size: 14px;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 6px;
}

.task-body {
  margin: 20px 0;
}

.task-description {
  color: #606266;
  font-size: 14px;
  line-height: 1.6;
  margin: 0 0 20px 0;
}

.progress-section {
  margin-bottom: 24px;
}

.progress-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.progress-label {
  font-size: 14px;
  color: #606266;
  font-weight: 500;
}

.progress-percentage {
  font-size: 18px;
  font-weight: 600;
  color: #409EFF;
}

.progress-detail {
  margin-top: 8px;
  font-size: 13px;
  color: #909399;
}

.deadline-section {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 16px;
  padding: 16px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.deadline-item,
.countdown-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
}

.deadline-label,
.countdown-label {
  color: #606266;
}

.deadline-value {
  color: #303133;
  font-weight: 500;
}

.countdown-value {
  font-weight: 600;
}

.countdown-normal {
  color: #67C23A;
}

.countdown-warning {
  color: #E6A23C;
}

.countdown-urgent {
  color: #F56C6C;
}

.countdown-expired {
  color: #909399;
}

.task-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-start;
}

.task-detail {
  padding: 16px 0;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .student-tasks {
    padding: 16px;
  }

  .page-header h1 {
    font-size: 24px;
  }

  .task-title {
    font-size: 18px;
  }

  .task-title-section {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .task-meta {
    flex-direction: column;
    gap: 8px;
  }

  .deadline-section {
    grid-template-columns: 1fr;
  }

  .task-actions {
    flex-direction: column;
  }

  .task-actions .el-button {
    width: 100%;
  }
}
</style>

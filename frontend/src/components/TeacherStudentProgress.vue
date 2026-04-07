<template>
  <div class="teacher-student-progress-component">
    <!-- 筛选工具栏 -->
    <div class="filter-toolbar">
      <div class="filter-item">
        <span class="label">班级：</span>
        <el-select
          v-model="filters.classId"
          placeholder="选择班级"
          clearable
          @change="handleFilterChange"
          style="width: 200px"
        >
          <el-option
            v-for="cls in classes"
            :key="cls.id"
            :label="cls.name"
            :value="cls.id"
          />
        </el-select>
      </div>

      <div class="filter-item">
        <span class="label">时间范围：</span>
        <el-select
          v-model="filters.timeRange"
          @change="handleFilterChange"
          style="width: 150px"
        >
          <el-option label="最近7天" value="7" />
          <el-option label="最近30天" value="30" />
          <el-option label="自定义" value="custom" />
        </el-select>
      </div>

      <div v-if="filters.timeRange === 'custom'" class="filter-item">
        <el-date-picker
          v-model="customDateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          @change="handleDateRangeChange"
          style="width: 280px"
        />
      </div>

      <div class="filter-item">
        <el-input
          v-model="filters.keyword"
          placeholder="搜索学生姓名..."
          clearable
          @clear="handleFilterChange"
          @keyup.enter="handleFilterChange"
          style="width: 200px"
        >
          <template #prefix>
            <el-icon><Search /></el-icon>
          </template>
        </el-input>
      </div>

      <el-button type="primary" :icon="Search" @click="handleSearch">
        查询
      </el-button>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 学生列表 -->
    <div v-else-if="students.length > 0" class="students-list">
      <el-card
        v-for="student in students"
        :key="student.studentId"
        class="student-card"
        shadow="hover"
      >
        <div class="student-header">
          <div class="student-info">
            <el-avatar :size="50" :src="student.avatar">
              {{ student.studentName?.charAt(0) || 'S' }}
            </el-avatar>
            <div class="student-details">
              <h3 class="student-name">{{ student.studentName }}</h3>
              <p class="student-meta">学号: {{ student.studentId }}</p>
            </div>
          </div>
          <div class="student-actions">
            <el-button
              type="primary"
              size="small"
              @click="handleViewDetails(student)"
            >
              查看详情
            </el-button>
          </div>
        </div>

        <div class="student-stats">
          <div class="stat-item">
            <div class="stat-label">累计学习</div>
            <div class="stat-value primary">{{ student.totalLearnedWords || 0 }}</div>
            <div class="stat-unit">个单词</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">日均学习</div>
            <div class="stat-value">{{ student.averageDailyWords || 0 }}</div>
            <div class="stat-unit">个单词</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">学习天数</div>
            <div class="stat-value">{{ student.studyDays || 0 }}</div>
            <div class="stat-unit">天</div>
          </div>
          <div class="stat-item">
            <div class="stat-label">活跃度</div>
            <el-progress
              :percentage="getActivityPercentage(student)"
              :color="getActivityColor(student)"
              :stroke-width="8"
            />
          </div>
        </div>
      </el-card>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-else
      description="暂无学生数据"
      :image-size="120"
    >
      <el-button type="primary" @click="handleFilterChange">
        刷新数据
      </el-button>
    </el-empty>

    <!-- 学生详情对话框 -->
    <el-dialog
      v-model="showDetailDialog"
      :title="`${currentStudent?.studentName} 的学习详情`"
      width="800px"
      :close-on-click-modal="false"
    >
      <div v-if="studentDetail" class="detail-content">
        <!-- 统计概览 -->
        <div class="detail-stats">
          <el-card class="detail-stat-card">
            <div class="stat-label">累计学习</div>
            <div class="stat-value primary">{{ studentDetail.totalLearnedWords || 0 }}</div>
            <div class="stat-unit">个单词</div>
          </el-card>
          <el-card class="detail-stat-card">
            <div class="stat-label">日均学习</div>
            <div class="stat-value">{{ studentDetail.averageDailyWords || 0 }}</div>
            <div class="stat-unit">个单词</div>
          </el-card>
          <el-card class="detail-stat-card">
            <div class="stat-label">学习天数</div>
            <div class="stat-value">{{ studentDetail.studyDays || 0 }}</div>
            <div class="stat-unit">天</div>
          </el-card>
        </div>

        <!-- 学习趋势图 -->
        <el-card class="detail-chart-card">
          <template #header>
            <span>学习趋势</span>
          </template>
          <div class="chart-container">
            <v-chart
              v-if="detailChartOption"
              :option="detailChartOption"
              :autoresize="true"
              class="chart"
            />
            <el-empty v-else description="暂无数据" :image-size="80" />
          </div>
        </el-card>
      </div>
      <div v-else class="detail-loading">
        <el-icon class="is-loading" :size="30"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Search, Loading } from '@element-plus/icons-vue'
import { teacherAPI } from '../api/teacher'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'

use([
  CanvasRenderer,
  LineChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// Props
const props = defineProps({
  initialClassId: {
    type: Number,
    default: null
  }
})

// Emits
const emit = defineEmits(['studentSelected'])

// 状态
const loading = ref(false)
const classes = ref([])
const students = ref([])
const filters = ref({
  classId: props.initialClassId,
  timeRange: '7',
  keyword: ''
})
const customDateRange = ref(null)
const showDetailDialog = ref(false)
const currentStudent = ref(null)
const studentDetail = ref(null)

// 计算活跃度百分比
const getActivityPercentage = (student) => {
  const avgDaily = student.averageDailyWords || 0
  // 假设每天学习20个单词为100%活跃度
  return Math.min(Math.round((avgDaily / 20) * 100), 100)
}

// 获取活跃度颜色
const getActivityColor = (student) => {
  const percentage = getActivityPercentage(student)
  if (percentage >= 80) return '#67C23A'
  if (percentage >= 50) return '#409EFF'
  if (percentage >= 30) return '#E6A23C'
  return '#F56C6C'
}

// 加载班级列表
const loadClasses = async () => {
  try {
    const response = await teacherAPI.getClasses()
    if (response.data) {
      classes.value = response.data
    }
  } catch (error) {
    console.error('加载班级列表失败:', error)
  }
}

// 加载学生列表
const loadStudents = async () => {
  if (!filters.value.classId) {
    students.value = []
    return
  }

  loading.value = true
  
  try {
    const params = {
      timeRange: filters.value.timeRange === 'custom' ? undefined : filters.value.timeRange,
      keyword: filters.value.keyword || undefined
    }

    if (filters.value.timeRange === 'custom' && customDateRange.value) {
      params.startDate = customDateRange.value[0]
      params.endDate = customDateRange.value[1]
    }

    const response = await teacherAPI.getClassStudents(filters.value.classId)
    if (response.data) {
      students.value = response.data
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
    ElMessage.error('加载学生列表失败')
  } finally {
    loading.value = false
  }
}

// 处理筛选变化
const handleFilterChange = () => {
  loadStudents()
}

// 处理日期范围变化
const handleDateRangeChange = () => {
  loadStudents()
}

// 处理搜索
const handleSearch = () => {
  loadStudents()
}

// 处理查看详情
const handleViewDetails = async (student) => {
  currentStudent.value = student
  showDetailDialog.value = true
  studentDetail.value = null

  try {
    const params = {
      timeRange: filters.value.timeRange === 'custom' ? undefined : filters.value.timeRange
    }

    if (filters.value.timeRange === 'custom' && customDateRange.value) {
      params.startDate = customDateRange.value[0]
      params.endDate = customDateRange.value[1]
    }

    const response = await teacherAPI.getStudentProgress(student.studentId, params)
    if (response.data) {
      studentDetail.value = response.data
    }
  } catch (error) {
    console.error('加载学生详情失败:', error)
    ElMessage.error('加载学生详情失败')
  }
}

// 详情图表配置
const detailChartOption = computed(() => {
  if (!studentDetail.value?.dailyProgress || studentDetail.value.dailyProgress.length === 0) {
    return null
  }

  const data = studentDetail.value.dailyProgress
  const dates = data.map(item => item.date)
  const newWords = data.map(item => item.newWords || 0)
  const reviewWords = data.map(item => item.reviewWords || 0)

  return {
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['新学单词', '复习单词']
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates,
      axisLabel: {
        formatter: (value) => {
          const date = new Date(value)
          return `${date.getMonth() + 1}/${date.getDate()}`
        }
      }
    },
    yAxis: {
      type: 'value',
      name: '单词数'
    },
    series: [
      {
        name: '新学单词',
        type: 'line',
        data: newWords,
        smooth: true,
        itemStyle: { color: '#409EFF' }
      },
      {
        name: '复习单词',
        type: 'line',
        data: reviewWords,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
})

// 初始化
onMounted(() => {
  loadClasses()
  if (filters.value.classId) {
    loadStudents()
  }
})
</script>

<style scoped>
.teacher-student-progress-component {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.filter-toolbar {
  display: flex;
  align-items: center;
  gap: 15px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  flex-wrap: wrap;
}

.filter-item {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 300px;
  color: #909399;
}

.loading-container p {
  margin-top: 15px;
}

.students-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.student-card {
  transition: all 0.3s ease;
}

.student-card:hover {
  transform: translateY(-4px);
}

.student-card :deep(.el-card__body) {
  padding: 20px;
}

.student-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 2px solid #EBEEF5;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 15px;
}

.student-details {
  flex: 1;
}

.student-name {
  margin: 0 0 5px 0;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.student-meta {
  margin: 0;
  font-size: 13px;
  color: #909399;
}

.student-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
}

.stat-item {
  text-align: center;
}

.stat-label {
  font-size: 12px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.stat-value.primary {
  color: #409EFF;
}

.stat-unit {
  font-size: 11px;
  color: #909399;
}

.detail-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.detail-stats {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 15px;
}

.detail-stat-card {
  text-align: center;
  padding: 20px;
}

.detail-chart-card :deep(.el-card__header) {
  padding: 15px 20px;
  font-weight: 600;
}

.chart-container {
  min-height: 300px;
  padding: 10px;
}

.chart {
  width: 100%;
  height: 300px;
}

.detail-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #909399;
}

.detail-loading p {
  margin-top: 10px;
}

@media (max-width: 768px) {
  .filter-toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-item {
    flex-direction: column;
    align-items: stretch;
  }

  .students-list {
    grid-template-columns: 1fr;
  }

  .student-stats {
    grid-template-columns: repeat(2, 1fr);
  }

  .detail-stats {
    grid-template-columns: 1fr;
  }
}
</style>

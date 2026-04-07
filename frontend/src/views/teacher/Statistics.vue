<template>
  <div class="teacher-statistics">
    <el-card class="header-card">
      <h2>统计分析</h2>
      
      <!-- 时间范围选择器 -->
      <div class="date-range-selector">
        <el-date-picker
          v-model="dateRange"
          type="daterange"
          range-separator="至"
          start-placeholder="开始日期"
          end-placeholder="结束日期"
          :shortcuts="dateShortcuts"
          @change="handleDateRangeChange"
          style="width: 400px"
        />
        <el-button type="primary" @click="refreshData" :loading="loading">
          刷新数据
        </el-button>
      </div>
    </el-card>

    <!-- 班级整体统计 -->
    <el-card class="stats-card">
      <template #header>
        <div class="card-header">
          <span>班级整体统计</span>
        </div>
      </template>
      
      <el-row :gutter="20" v-loading="loading">
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-icon total-students">
              <el-icon><User /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ classStats.totalStudents }}</div>
              <div class="stat-label">总学生数</div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-icon active-students">
              <el-icon><UserFilled /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ classStats.activeStudents }}</div>
              <div class="stat-label">活跃学生数</div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-icon avg-duration">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ classStats.avgLearningDuration }}</div>
              <div class="stat-label">平均学习时长(小时)</div>
            </div>
          </div>
        </el-col>
        
        <el-col :span="6">
          <div class="stat-item">
            <div class="stat-icon avg-score">
              <el-icon><TrophyBase /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ classStats.avgScore }}</div>
              <div class="stat-label">平均测试成绩</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>成绩分布</span>
            </div>
          </template>
          <div v-loading="loading" style="height: 350px">
            <v-chart :option="scoreDistributionOption" autoresize />
          </div>
        </el-card>
      </el-col>
      
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>词汇掌握情况</span>
            </div>
          </template>
          <div v-loading="loading" style="height: 350px">
            <v-chart :option="vocabularyMasteryOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20">
      <el-col :span="24">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>学习时长趋势</span>
            </div>
          </template>
          <div v-loading="loading" style="height: 350px">
            <v-chart :option="learningTrendOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 学生详情统计 -->
    <el-card class="student-detail-card">
      <template #header>
        <div class="card-header">
          <span>学生详情统计</span>
          <el-select
            v-model="selectedStudentId"
            placeholder="选择学生"
            filterable
            @change="handleStudentChange"
            style="width: 250px"
          >
            <el-option
              v-for="student in studentList"
              :key="student.id"
              :label="`${student.realName} (${student.username})`"
              :value="student.id"
            />
          </el-select>
        </div>
      </template>

      <div v-if="selectedStudentId" v-loading="studentLoading">
        <el-row :gutter="20" class="student-stats-row">
          <el-col :span="6">
            <div class="student-stat-item">
              <div class="label">学习时长</div>
              <div class="value">{{ studentStats.learningDuration }} 小时</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="student-stat-item">
              <div class="label">掌握词汇数</div>
              <div class="value">{{ studentStats.masteredVocabulary }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="student-stat-item">
              <div class="label">平均成绩</div>
              <div class="value">{{ studentStats.avgScore }}</div>
            </div>
          </el-col>
          <el-col :span="6">
            <div class="student-stat-item">
              <div class="label">任务完成率</div>
              <div class="value">{{ studentStats.taskCompletionRate }}%</div>
            </div>
          </el-col>
        </el-row>

        <div class="student-chart">
          <h3>测试成绩趋势</h3>
          <div style="height: 300px">
            <v-chart :option="studentScoreTrendOption" autoresize />
          </div>
        </div>
      </div>
      
      <el-empty v-else description="请选择学生查看详情" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { User, UserFilled, Clock, TrophyBase } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { BarChart, LineChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'
import { statisticsAPI } from '@/api/statistics'
import { studentAPI } from '@/api/student'

// 注册ECharts组件
use([
  CanvasRenderer,
  BarChart,
  LineChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// 状态管理
const loading = ref(false)
const studentLoading = ref(false)
const dateRange = ref([])
const selectedStudentId = ref(null)
const studentList = ref([])

// 班级统计数据
const classStats = reactive({
  totalStudents: 0,
  activeStudents: 0,
  avgLearningDuration: 0,
  avgScore: 0
})

// 学生统计数据
const studentStats = reactive({
  learningDuration: 0,
  masteredVocabulary: 0,
  avgScore: 0,
  taskCompletionRate: 0
})

// 日期快捷选项
const dateShortcuts = [
  {
    text: '最近7天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 7)
      return [start, end]
    }
  },
  {
    text: '最近30天',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
      return [start, end]
    }
  },
  {
    text: '最近3个月',
    value: () => {
      const end = new Date()
      const start = new Date()
      start.setTime(start.getTime() - 3600 * 1000 * 24 * 90)
      return [start, end]
    }
  }
]

// 成绩分布图表配置
const scoreDistributionOption = ref({
  tooltip: {
    trigger: 'axis',
    axisPointer: {
      type: 'shadow'
    }
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    data: ['0-60', '60-70', '70-80', '80-90', '90-100'],
    axisLabel: {
      interval: 0
    }
  },
  yAxis: {
    type: 'value',
    name: '学生数'
  },
  series: [
    {
      name: '学生数',
      type: 'bar',
      data: [0, 0, 0, 0, 0],
      itemStyle: {
        color: '#409EFF'
      },
      barWidth: '50%'
    }
  ]
})

// 词汇掌握情况图表配置
const vocabularyMasteryOption = ref({
  tooltip: {
    trigger: 'item',
    formatter: '{a} <br/>{b}: {c} ({d}%)'
  },
  legend: {
    orient: 'vertical',
    left: 'left'
  },
  series: [
    {
      name: '掌握程度',
      type: 'pie',
      radius: '50%',
      data: [
        { value: 0, name: '未学习' },
        { value: 0, name: '学习中' },
        { value: 0, name: '熟悉' },
        { value: 0, name: '已掌握' }
      ],
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }
  ]
})

// 学习时长趋势图表配置
const learningTrendOption = ref({
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: []
  },
  yAxis: {
    type: 'value',
    name: '学习时长(小时)'
  },
  series: [
    {
      name: '学习时长',
      type: 'line',
      data: [],
      smooth: true,
      itemStyle: {
        color: '#67C23A'
      },
      areaStyle: {
        color: 'rgba(103, 194, 58, 0.3)'
      }
    }
  ]
})

// 学生成绩趋势图表配置
const studentScoreTrendOption = ref({
  tooltip: {
    trigger: 'axis'
  },
  grid: {
    left: '3%',
    right: '4%',
    bottom: '3%',
    containLabel: true
  },
  xAxis: {
    type: 'category',
    boundaryGap: false,
    data: []
  },
  yAxis: {
    type: 'value',
    name: '成绩',
    min: 0,
    max: 100
  },
  series: [
    {
      name: '测试成绩',
      type: 'line',
      data: [],
      smooth: true,
      itemStyle: {
        color: '#E6A23C'
      }
    }
  ]
})

// 加载班级统计数据
const loadClassStatistics = async () => {
  try {
    loading.value = true
    // 假设classId为1，实际应从用户信息中获取
    const classId = 1
    
    // 格式化日期参数
    let startDate = null
    let endDate = null
    if (dateRange.value && dateRange.value.length === 2) {
      startDate = formatDate(dateRange.value[0])
      endDate = formatDate(dateRange.value[1])
    }
    
    const response = await statisticsAPI.getClassStatistics(classId, startDate, endDate)
    
    if (response.data) {
      const data = response.data
      classStats.totalStudents = data.totalStudents || 0
      classStats.activeStudents = data.activeStudents || 0
      classStats.avgLearningDuration = data.avgLearningDuration || 0
      classStats.avgScore = data.avgScore || 0

      // 更新成绩分布图
      if (data.scoreDistribution) {
        scoreDistributionOption.value.series[0].data = data.scoreDistribution
      }

      // 更新词汇掌握情况图
      if (data.vocabularyMastery) {
        vocabularyMasteryOption.value.series[0].data = [
          { value: data.vocabularyMastery.unknown || 0, name: '未学习' },
          { value: data.vocabularyMastery.learning || 0, name: '学习中' },
          { value: data.vocabularyMastery.familiar || 0, name: '熟悉' },
          { value: data.vocabularyMastery.mastered || 0, name: '已掌握' }
        ]
      }

      // 更新学习时长趋势图
      if (data.learningTrend) {
        learningTrendOption.value.xAxis.data = data.learningTrend.dates || []
        learningTrendOption.value.series[0].data = data.learningTrend.durations || []
      }
    }
  } catch (error) {
    console.error('加载班级统计失败:', error)
    ElMessage.error('加载班级统计数据失败')
  } finally {
    loading.value = false
  }
}

// 加载学生列表
const loadStudentList = async () => {
  try {
    const response = await studentAPI.getStudents()
    if (response.data) {
      studentList.value = response.data
    }
  } catch (error) {
    console.error('加载学生列表失败:', error)
  }
}

// 加载学生统计数据
const loadStudentStatistics = async (studentId) => {
  try {
    studentLoading.value = true
    
    // 格式化日期参数
    let startDate = null
    let endDate = null
    if (dateRange.value && dateRange.value.length === 2) {
      startDate = formatDate(dateRange.value[0])
      endDate = formatDate(dateRange.value[1])
    }
    
    const response = await statisticsAPI.getStudentStatistics(studentId, startDate, endDate)
    
    if (response.data) {
      const data = response.data
      studentStats.learningDuration = data.learningDuration || 0
      studentStats.masteredVocabulary = data.masteredVocabulary || 0
      studentStats.avgScore = data.avgScore || 0
      studentStats.taskCompletionRate = data.taskCompletionRate || 0

      // 更新学生成绩趋势图
      if (data.scoreTrend) {
        studentScoreTrendOption.value.xAxis.data = data.scoreTrend.dates || []
        studentScoreTrendOption.value.series[0].data = data.scoreTrend.scores || []
      }
    }
  } catch (error) {
    console.error('加载学生统计失败:', error)
    ElMessage.error('加载学生统计数据失败')
  } finally {
    studentLoading.value = false
  }
}

// 处理日期范围变化
const handleDateRangeChange = () => {
  refreshData()
}

// 刷新数据
const refreshData = () => {
  loadClassStatistics()
  if (selectedStudentId.value) {
    loadStudentStatistics(selectedStudentId.value)
  }
}

// 处理学生选择变化
const handleStudentChange = (studentId) => {
  if (studentId) {
    loadStudentStatistics(studentId)
  }
}

// 格式化日期为 YYYY-MM-DD
const formatDate = (date) => {
  if (!date) return null
  const d = new Date(date)
  const year = d.getFullYear()
  const month = String(d.getMonth() + 1).padStart(2, '0')
  const day = String(d.getDate()).padStart(2, '0')
  return `${year}-${month}-${day}`
}

// 初始化
onMounted(() => {
  // 设置默认日期范围为最近30天
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
  dateRange.value = [start, end]
  
  loadClassStatistics()
  loadStudentList()
})
</script>

<style scoped>
.teacher-statistics {
  padding: 20px;
}

.header-card {
  margin-bottom: 20px;
}

.header-card h2 {
  margin: 0 0 20px 0;
  font-size: 24px;
  color: #303133;
}

.date-range-selector {
  display: flex;
  gap: 15px;
  align-items: center;
  flex-wrap: wrap;
}

.stats-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  font-size: 16px;
  flex-wrap: wrap;
  gap: 10px;
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
}

.stat-item:hover {
  transform: translateY(-2px);
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  margin-right: 15px;
}

.stat-icon.total-students {
  background: #ecf5ff;
  color: #409eff;
}

.stat-icon.active-students {
  background: #f0f9ff;
  color: #67c23a;
}

.stat-icon.avg-duration {
  background: #fef0f0;
  color: #f56c6c;
}

.stat-icon.avg-score {
  background: #fdf6ec;
  color: #e6a23c;
}

.stat-content {
  flex: 1;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
}

.chart-card {
  margin-bottom: 20px;
}

.student-detail-card {
  margin-top: 20px;
}

.student-stats-row {
  margin-bottom: 30px;
}

.student-stat-item {
  text-align: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.student-stat-item .label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.student-stat-item .value {
  font-size: 24px;
  font-weight: bold;
  color: #303133;
}

.student-chart {
  margin-top: 20px;
}

.student-chart h3 {
  margin: 0 0 15px 0;
  font-size: 16px;
  color: #303133;
}

/* 移动端样式 */
@media (max-width: 767px) {
  .teacher-statistics {
    padding: 12px;
  }

  .header-card h2 {
    font-size: 20px;
  }

  .date-range-selector {
    flex-direction: column;
    align-items: stretch;
  }

  .date-range-selector .el-date-picker {
    width: 100% !important;
  }

  .date-range-selector .el-button {
    width: 100%;
  }

  :deep(.el-row) {
    margin: 0 !important;
  }

  :deep(.el-col) {
    padding: 0 !important;
    margin-bottom: 12px;
  }

  .stat-item {
    padding: 15px;
  }

  .stat-icon {
    width: 50px;
    height: 50px;
    font-size: 24px;
    margin-right: 12px;
  }

  .stat-value {
    font-size: 24px;
  }

  .stat-label {
    font-size: 12px;
  }

  .chart-card :deep(.el-card__body) {
    padding: 12px;
  }

  .chart-card > div {
    height: 250px !important;
  }

  .student-stat-item {
    padding: 15px;
    margin-bottom: 12px;
  }

  .student-stat-item .value {
    font-size: 20px;
  }

  .card-header .el-select {
    width: 100% !important;
  }
}

/* 平板端样式 */
@media (min-width: 768px) and (max-width: 1023px) {
  .teacher-statistics {
    padding: 16px;
  }

  .header-card h2 {
    font-size: 22px;
  }

  .date-range-selector .el-date-picker {
    width: 350px !important;
  }

  :deep(.el-col-6) {
    width: 50% !important;
  }

  :deep(.el-col-12) {
    width: 100% !important;
  }

  .stat-icon {
    width: 55px;
    height: 55px;
    font-size: 26px;
  }

  .stat-value {
    font-size: 26px;
  }

  .chart-card > div {
    height: 300px !important;
  }
}
</style>

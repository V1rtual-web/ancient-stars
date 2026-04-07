<template>
  <div class="student-statistics">
    <el-card class="header-card">
      <h2>我的学习统计</h2>
      
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
          style="width: 100%; max-width: 400px"
        />
        <el-button type="primary" @click="refreshData" :loading="loading">
          刷新数据
        </el-button>
      </div>
    </el-card>

    <!-- 个人统计概览 -->
    <el-card class="stats-card">
      <template #header>
        <div class="card-header">
          <span>学习概览</span>
        </div>
      </template>
      
      <el-row :gutter="20" v-loading="loading">
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-icon learning-duration">
              <el-icon><Clock /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ personalStats.learningDuration }}</div>
              <div class="stat-label">学习时长(小时)</div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-icon mastered-vocab">
              <el-icon><Document /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ personalStats.masteredVocabulary }}</div>
              <div class="stat-label">掌握词汇数</div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-icon avg-score">
              <el-icon><TrophyBase /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ personalStats.avgScore }}</div>
              <div class="stat-label">平均测试成绩</div>
            </div>
          </div>
        </el-col>
        
        <el-col :xs="12" :sm="12" :md="6">
          <div class="stat-item">
            <div class="stat-icon completion-rate">
              <el-icon><CircleCheck /></el-icon>
            </div>
            <div class="stat-content">
              <div class="stat-value">{{ personalStats.taskCompletionRate }}%</div>
              <div class="stat-label">任务完成率</div>
            </div>
          </div>
        </el-col>
      </el-row>
    </el-card>

    <!-- 图表区域 -->
    <el-row :gutter="20">
      <el-col :xs="24" :sm="24" :md="12">
        <el-card class="chart-card">
          <template #header>
            <div class="card-header">
              <span>测试成绩趋势</span>
            </div>
          </template>
          <div v-loading="loading" style="height: 350px">
            <v-chart :option="scoreTrendOption" autoresize />
          </div>
        </el-card>
      </el-col>
      
      <el-col :xs="24" :sm="24" :md="12">
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
              <span>每日学习时长</span>
            </div>
          </template>
          <div v-loading="loading" style="height: 350px">
            <v-chart :option="learningDurationOption" autoresize />
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 学习报告 -->
    <el-card class="report-card">
      <template #header>
        <div class="card-header">
          <span>学习报告</span>
          <el-button type="primary" @click="generateReport" :loading="reportLoading">
            生成报告
          </el-button>
        </div>
      </template>

      <div v-if="report" class="report-content">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="报告时间">
            {{ report.reportTime }}
          </el-descriptions-item>
          <el-descriptions-item label="统计周期">
            {{ report.period }}
          </el-descriptions-item>
          <el-descriptions-item label="总学习时长">
            {{ report.totalLearningDuration }} 小时
          </el-descriptions-item>
          <el-descriptions-item label="总掌握词汇">
            {{ report.totalMasteredVocabulary }} 个
          </el-descriptions-item>
          <el-descriptions-item label="完成测试次数">
            {{ report.totalTests }} 次
          </el-descriptions-item>
          <el-descriptions-item label="平均测试成绩">
            {{ report.avgTestScore }} 分
          </el-descriptions-item>
          <el-descriptions-item label="最高成绩">
            {{ report.highestScore }} 分
          </el-descriptions-item>
          <el-descriptions-item label="最低成绩">
            {{ report.lowestScore }} 分
          </el-descriptions-item>
          <el-descriptions-item label="完成任务数">
            {{ report.completedTasks }} / {{ report.totalTasks }}
          </el-descriptions-item>
          <el-descriptions-item label="学习建议" :span="2">
            <el-tag v-for="(suggestion, index) in report.suggestions" :key="index" style="margin-right: 8px; margin-bottom: 8px">
              {{ suggestion }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      
      <el-empty v-else description="点击生成报告按钮查看学习报告" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Clock, Document, TrophyBase, CircleCheck } from '@element-plus/icons-vue'
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
import { useUserStore } from '@/store/modules/user'

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
const userStore = useUserStore()
const loading = ref(false)
const reportLoading = ref(false)
const dateRange = ref([])
const report = ref(null)

// 个人统计数据
const personalStats = reactive({
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

// 测试成绩趋势图表配置
const scoreTrendOption = ref({
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
        color: '#409EFF'
      },
      areaStyle: {
        color: 'rgba(64, 158, 255, 0.3)'
      }
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
      radius: '60%',
      data: [
        { value: 0, name: '未学习', itemStyle: { color: '#909399' } },
        { value: 0, name: '学习中', itemStyle: { color: '#E6A23C' } },
        { value: 0, name: '熟悉', itemStyle: { color: '#409EFF' } },
        { value: 0, name: '已掌握', itemStyle: { color: '#67C23A' } }
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

// 每日学习时长图表配置
const learningDurationOption = ref({
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
    data: []
  },
  yAxis: {
    type: 'value',
    name: '学习时长(小时)'
  },
  series: [
    {
      name: '学习时长',
      type: 'bar',
      data: [],
      itemStyle: {
        color: '#67C23A'
      },
      barWidth: '50%'
    }
  ]
})

// 加载个人统计数据
const loadPersonalStatistics = async () => {
  loading.value = true
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      ElMessage.error('用户信息不存在')
      return
    }

    // 格式化日期参数
    let startDate = null
    let endDate = null
    if (dateRange.value && dateRange.value.length === 2) {
      startDate = formatDate(dateRange.value[0])
      endDate = formatDate(dateRange.value[1])
    }

    const response = await statisticsAPI.getStudentStatistics(userId, startDate, endDate)
    
    if (response.data) {
      const data = response.data
      personalStats.learningDuration = data.learningDuration || 0
      personalStats.masteredVocabulary = data.masteredVocabulary || 0
      personalStats.avgScore = data.avgScore || 0
      personalStats.taskCompletionRate = data.taskCompletionRate || 0

      // 更新测试成绩趋势图
      if (data.scoreTrend) {
        scoreTrendOption.value.xAxis.data = data.scoreTrend.dates || []
        scoreTrendOption.value.series[0].data = data.scoreTrend.scores || []
      }

      // 更新词汇掌握情况图
      if (data.vocabularyMastery) {
        vocabularyMasteryOption.value.series[0].data = [
          { value: data.vocabularyMastery.unknown || 0, name: '未学习', itemStyle: { color: '#909399' } },
          { value: data.vocabularyMastery.learning || 0, name: '学习中', itemStyle: { color: '#E6A23C' } },
          { value: data.vocabularyMastery.familiar || 0, name: '熟悉', itemStyle: { color: '#409EFF' } },
          { value: data.vocabularyMastery.mastered || 0, name: '已掌握', itemStyle: { color: '#67C23A' } }
        ]
      }

      // 更新每日学习时长图
      if (data.dailyLearningDuration) {
        learningDurationOption.value.xAxis.data = data.dailyLearningDuration.dates || []
        learningDurationOption.value.series[0].data = data.dailyLearningDuration.durations || []
      }
    }
  } catch (error) {
    ElMessage.error('加载统计数据失败')
  } finally {
    loading.value = false
  }
}

// 生成学习报告
const generateReport = async () => {
  reportLoading.value = true
  try {
    const userId = userStore.userInfo?.id
    if (!userId) {
      ElMessage.error('用户信息不存在')
      return
    }

    // 格式化日期参数
    let startDate = null
    let endDate = null
    if (dateRange.value && dateRange.value.length === 2) {
      startDate = formatDate(dateRange.value[0])
      endDate = formatDate(dateRange.value[1])
    }

    const response = await statisticsAPI.generateReport(userId, startDate, endDate)
    
    if (response.data) {
      report.value = response.data
      ElMessage.success('报告生成成功')
    }
  } catch (error) {
    ElMessage.error('生成报告失败')
  } finally {
    reportLoading.value = false
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

// 处理日期范围变化
const handleDateRangeChange = () => {
  refreshData()
}

// 刷新数据
const refreshData = () => {
  loadPersonalStatistics()
  // 如果已经生成过报告，重新生成
  if (report.value) {
    generateReport()
  }
}

// 初始化
onMounted(() => {
  // 设置默认日期范围为最近30天
  const end = new Date()
  const start = new Date()
  start.setTime(start.getTime() - 3600 * 1000 * 24 * 30)
  dateRange.value = [start, end]
  
  loadPersonalStatistics()
})
</script>

<style scoped>
.student-statistics {
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
}

.stat-item {
  display: flex;
  align-items: center;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
  height: 100%;
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
  flex-shrink: 0;
}

.stat-icon.learning-duration {
  background: #fef0f0;
  color: #f56c6c;
}

.stat-icon.mastered-vocab {
  background: #f0f9ff;
  color: #67c23a;
}

.stat-icon.avg-score {
  background: #fdf6ec;
  color: #e6a23c;
}

.stat-icon.completion-rate {
  background: #ecf5ff;
  color: #409eff;
}

.stat-content {
  flex: 1;
  min-width: 0;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 5px;
  word-break: break-all;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chart-card {
  margin-bottom: 20px;
}

.report-card {
  margin-top: 20px;
}

.report-content {
  padding: 10px 0;
}

/* 响应式设计 */
@media (max-width: 767px) {
  .student-statistics {
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
    max-width: 100% !important;
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
    margin-right: 10px;
  }

  .stat-value {
    font-size: 22px;
  }

  .stat-label {
    font-size: 12px;
  }

  .chart-card {
    margin-bottom: 15px;
  }

  .chart-card :deep(.el-card__body) {
    padding: 12px;
  }

  .chart-card > div {
    height: 250px !important;
  }

  :deep(.el-descriptions) {
    font-size: 12px;
  }

  :deep(.el-descriptions__label) {
    width: 100px !important;
  }
}

/* 平板端样式 */
@media (min-width: 768px) and (max-width: 1023px) {
  .student-statistics {
    padding: 16px;
  }

  .header-card h2 {
    font-size: 22px;
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

@media (max-width: 576px) {
  .stat-value {
    font-size: 20px;
  }

  .stat-icon {
    width: 45px;
    height: 45px;
    font-size: 20px;
  }
}
</style>

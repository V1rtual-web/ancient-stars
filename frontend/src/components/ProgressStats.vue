<template>
  <div class="progress-stats-component">
    <!-- 统计卡片 -->
    <div class="stats-cards">
      <el-card class="stat-card today">
        <div class="stat-icon">
          <el-icon :size="40"><Calendar /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">今日新学</div>
          <div class="stat-value">{{ progressData.todayNewWords || 0 }}</div>
          <div class="stat-unit">个单词</div>
        </div>
      </el-card>

      <el-card class="stat-card review">
        <div class="stat-icon">
          <el-icon :size="40"><Reading /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">今日复习</div>
          <div class="stat-value">{{ progressData.todayReviewWords || 0 }}</div>
          <div class="stat-unit">个单词</div>
        </div>
      </el-card>

      <el-card class="stat-card week">
        <div class="stat-icon">
          <el-icon :size="40"><TrendCharts /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">本周新学</div>
          <div class="stat-value">{{ progressData.weekNewWords || 0 }}</div>
          <div class="stat-unit">个单词</div>
        </div>
      </el-card>

      <el-card class="stat-card total">
        <div class="stat-icon">
          <el-icon :size="40"><Trophy /></el-icon>
        </div>
        <div class="stat-content">
          <div class="stat-label">累计已学</div>
          <div class="stat-value primary">{{ progressData.totalLearnedWords || 0 }}</div>
          <div class="stat-unit">个单词</div>
        </div>
      </el-card>
    </div>

    <!-- 学习趋势图 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="title">学习趋势</span>
          <el-radio-group v-model="trendPeriod" size="small" @change="handlePeriodChange">
            <el-radio-button label="7">最近7天</el-radio-button>
            <el-radio-button label="30">最近30天</el-radio-button>
          </el-radio-group>
        </div>
      </template>
      <div class="chart-container">
        <v-chart
          v-if="trendChartOption"
          :option="trendChartOption"
          :autoresize="true"
          class="chart"
        />
        <el-empty v-else description="暂无数据" :image-size="100" />
      </div>
    </el-card>

    <!-- 学习日历热力图 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="title">学习日历</span>
          <span class="subtitle">每日学习情况一目了然</span>
        </div>
      </template>
      <div class="chart-container calendar">
        <v-chart
          v-if="calendarChartOption"
          :option="calendarChartOption"
          :autoresize="true"
          class="chart calendar-chart"
        />
        <el-empty v-else description="暂无数据" :image-size="100" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, watch } from 'vue'
import { Calendar, Reading, TrendCharts, Trophy } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, HeatmapChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CalendarComponent,
  VisualMapComponent
} from 'echarts/components'

// 注册 ECharts 组件
use([
  CanvasRenderer,
  LineChart,
  HeatmapChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent,
  CalendarComponent,
  VisualMapComponent
])

// Props
const props = defineProps({
  progressData: {
    type: Object,
    required: true,
    default: () => ({
      todayNewWords: 0,
      todayReviewWords: 0,
      weekNewWords: 0,
      totalLearnedWords: 0,
      dailyProgress: []
    })
  }
})

// Emits
const emit = defineEmits(['periodChange'])

// 状态
const trendPeriod = ref('7')

// 处理时间段变化
const handlePeriodChange = (value) => {
  emit('periodChange', parseInt(value))
}

// 学习趋势图配置
const trendChartOption = computed(() => {
  if (!props.progressData.dailyProgress || props.progressData.dailyProgress.length === 0) {
    return null
  }

  const data = props.progressData.dailyProgress
  const dates = data.map(item => item.date)
  const newWords = data.map(item => item.newWords || 0)
  const reviewWords = data.map(item => item.reviewWords || 0)

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross',
        label: {
          backgroundColor: '#6a7985'
        }
      }
    },
    legend: {
      data: ['新学单词', '复习单词'],
      top: 10
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: 50,
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
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
      name: '单词数',
      minInterval: 1
    },
    series: [
      {
        name: '新学单词',
        type: 'line',
        smooth: true,
        data: newWords,
        itemStyle: {
          color: '#409EFF'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(64, 158, 255, 0.3)' },
              { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
            ]
          }
        }
      },
      {
        name: '复习单词',
        type: 'line',
        smooth: true,
        data: reviewWords,
        itemStyle: {
          color: '#67C23A'
        },
        areaStyle: {
          color: {
            type: 'linear',
            x: 0,
            y: 0,
            x2: 0,
            y2: 1,
            colorStops: [
              { offset: 0, color: 'rgba(103, 194, 58, 0.3)' },
              { offset: 1, color: 'rgba(103, 194, 58, 0.05)' }
            ]
          }
        }
      }
    ]
  }
})

// 学习日历热力图配置
const calendarChartOption = computed(() => {
  if (!props.progressData.dailyProgress || props.progressData.dailyProgress.length === 0) {
    return null
  }

  const data = props.progressData.dailyProgress
  const heatmapData = data.map(item => [
    item.date,
    (item.newWords || 0) + (item.reviewWords || 0)
  ])

  // 获取日期范围
  const dates = data.map(item => new Date(item.date))
  const minDate = new Date(Math.min(...dates))
  const maxDate = new Date(Math.max(...dates))

  // 计算最大值用于视觉映射
  const maxValue = Math.max(...heatmapData.map(item => item[1]))

  return {
    tooltip: {
      position: 'top',
      formatter: (params) => {
        const date = params.data[0]
        const value = params.data[1]
        return `${date}<br/>学习: ${value} 个单词`
      }
    },
    visualMap: {
      min: 0,
      max: maxValue || 20,
      calculable: true,
      orient: 'horizontal',
      left: 'center',
      bottom: 20,
      inRange: {
        color: ['#ebedf0', '#c6e48b', '#7bc96f', '#239a3b', '#196127']
      },
      text: ['多', '少']
    },
    calendar: {
      top: 60,
      left: 50,
      right: 30,
      cellSize: ['auto', 20],
      range: [minDate.toISOString().split('T')[0], maxDate.toISOString().split('T')[0]],
      itemStyle: {
        borderWidth: 3,
        borderColor: '#fff'
      },
      yearLabel: { show: false },
      dayLabel: {
        nameMap: ['日', '一', '二', '三', '四', '五', '六']
      },
      monthLabel: {
        nameMap: 'cn'
      }
    },
    series: [
      {
        type: 'heatmap',
        coordinateSystem: 'calendar',
        data: heatmapData
      }
    ]
  }
})
</script>

<style scoped>
.progress-stats-component {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

/* 统计卡片 */
.stats-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.stat-card {
  transition: all 0.3s ease;
  cursor: default;
}

.stat-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.stat-card :deep(.el-card__body) {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 25px;
}

.stat-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 70px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
}

.stat-card.today .stat-icon {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.stat-card.review .stat-icon {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.stat-card.week .stat-icon {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.stat-card.total .stat-icon {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.stat-content {
  flex: 1;
  text-align: left;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
  font-weight: 500;
}

.stat-value {
  font-size: 32px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
  line-height: 1;
}

.stat-value.primary {
  color: #409EFF;
}

.stat-unit {
  font-size: 12px;
  color: #909399;
}

/* 图表卡片 */
.chart-card {
  width: 100%;
}

.chart-card :deep(.el-card__header) {
  padding: 20px 25px;
  border-bottom: 2px solid #EBEEF5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-header .subtitle {
  font-size: 14px;
  color: #909399;
  margin-left: 10px;
}

.chart-container {
  padding: 20px;
  min-height: 350px;
}

.chart-container.calendar {
  min-height: 250px;
}

.chart {
  width: 100%;
  height: 350px;
}

.calendar-chart {
  height: 250px;
}

/* 响应式设计 */
@media (max-width: 1200px) {
  .stats-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .stats-cards {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .stat-card :deep(.el-card__body) {
    padding: 20px;
    gap: 15px;
  }

  .stat-icon {
    width: 60px;
    height: 60px;
  }

  .stat-icon .el-icon {
    font-size: 32px;
  }

  .stat-value {
    font-size: 28px;
  }

  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .chart-container {
    padding: 15px;
    min-height: 300px;
  }

  .chart {
    height: 300px;
  }

  .calendar-chart {
    height: 220px;
  }
}

@media (max-width: 480px) {
  .stat-card :deep(.el-card__body) {
    flex-direction: column;
    text-align: center;
    padding: 20px 15px;
  }

  .stat-content {
    text-align: center;
  }

  .stat-value {
    font-size: 24px;
  }

  .chart-container {
    padding: 10px;
    min-height: 250px;
  }

  .chart {
    height: 250px;
  }

  .calendar-chart {
    height: 200px;
  }
}
</style>

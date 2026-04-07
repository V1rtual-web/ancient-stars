<template>
  <div class="class-progress-stats-component">
    <!-- 班级整体统计卡片 -->
    <div class="overview-cards">
      <el-card class="overview-card">
        <div class="card-content">
          <div class="card-icon students">
            <el-icon :size="40"><User /></el-icon>
          </div>
          <div class="card-info">
            <div class="card-label">总学生数</div>
            <div class="card-value">{{ classData.totalStudents || 0 }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card">
        <div class="card-content">
          <div class="card-icon active">
            <el-icon :size="40"><Checked /></el-icon>
          </div>
          <div class="card-info">
            <div class="card-label">活跃学生</div>
            <div class="card-value">{{ classData.activeStudents || 0 }}</div>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card">
        <div class="card-content">
          <div class="card-icon average">
            <el-icon :size="40"><TrendCharts /></el-icon>
          </div>
          <div class="card-info">
            <div class="card-label">日均学习</div>
            <div class="card-value">{{ classData.averageDailyWords || 0 }}</div>
            <div class="card-unit">个单词</div>
          </div>
        </div>
      </el-card>

      <el-card class="overview-card">
        <div class="card-content">
          <div class="card-icon total">
            <el-icon :size="40"><Trophy /></el-icon>
          </div>
          <div class="card-info">
            <div class="card-label">班级总学习</div>
            <div class="card-value primary">{{ classData.totalWords || 0 }}</div>
            <div class="card-unit">个单词</div>
          </div>
        </div>
      </el-card>
    </div>

    <!-- 学习最积极的学生排名 -->
    <el-card class="ranking-card">
      <template #header>
        <div class="card-header">
          <span class="title">
            <el-icon><Medal /></el-icon>
            学习排行榜
          </span>
          <span class="subtitle">学习最积极的学生</span>
        </div>
      </template>
      <div v-if="classData.topStudents && classData.topStudents.length > 0" class="ranking-list">
        <div
          v-for="(student, index) in classData.topStudents"
          :key="student.studentId"
          class="ranking-item"
          :class="{ 'top-three': index < 3 }"
        >
          <div class="rank-badge" :class="`rank-${index + 1}`">
            <span v-if="index < 3" class="medal-icon">
              {{ ['🥇', '🥈', '🥉'][index] }}
            </span>
            <span v-else class="rank-number">{{ index + 1 }}</span>
          </div>
          <div class="student-info">
            <el-avatar :size="40" :src="student.avatar">
              {{ student.studentName?.charAt(0) || 'S' }}
            </el-avatar>
            <div class="student-details">
              <div class="student-name">{{ student.studentName }}</div>
              <div class="student-meta">学号: {{ student.studentId }}</div>
            </div>
          </div>
          <div class="student-score">
            <div class="score-value">{{ student.totalWords || 0 }}</div>
            <div class="score-label">个单词</div>
          </div>
        </div>
      </div>
      <el-empty v-else description="暂无数据" :image-size="100" />
    </el-card>

    <!-- 班级学习趋势图 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="title">班级学习趋势</span>
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

    <!-- 学生分布图 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span class="title">学生学习分布</span>
          <span class="subtitle">按学习量分组</span>
        </div>
      </template>
      <div class="chart-container">
        <v-chart
          v-if="distributionChartOption"
          :option="distributionChartOption"
          :autoresize="true"
          class="chart"
        />
        <el-empty v-else description="暂无数据" :image-size="100" />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { User, Checked, TrendCharts, Trophy, Medal } from '@element-plus/icons-vue'
import VChart from 'vue-echarts'
import { use } from 'echarts/core'
import { CanvasRenderer } from 'echarts/renderers'
import { LineChart, BarChart, PieChart } from 'echarts/charts'
import {
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
} from 'echarts/components'

use([
  CanvasRenderer,
  LineChart,
  BarChart,
  PieChart,
  TitleComponent,
  TooltipComponent,
  LegendComponent,
  GridComponent
])

// Props
const props = defineProps({
  classData: {
    type: Object,
    required: true,
    default: () => ({
      totalStudents: 0,
      activeStudents: 0,
      averageDailyWords: 0,
      totalWords: 0,
      topStudents: [],
      dailyTrend: [],
      distribution: []
    })
  }
})

// 班级学习趋势图配置
const trendChartOption = computed(() => {
  if (!props.classData.dailyTrend || props.classData.dailyTrend.length === 0) {
    return null
  }

  const data = props.classData.dailyTrend
  const dates = data.map(item => item.date)
  const totalWords = data.map(item => item.totalWords || 0)
  const activeStudents = data.map(item => item.activeStudents || 0)

  return {
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'cross'
      }
    },
    legend: {
      data: ['班级总学习量', '活跃学生数']
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
    yAxis: [
      {
        type: 'value',
        name: '学习量',
        position: 'left'
      },
      {
        type: 'value',
        name: '学生数',
        position: 'right'
      }
    ],
    series: [
      {
        name: '班级总学习量',
        type: 'line',
        data: totalWords,
        smooth: true,
        itemStyle: { color: '#409EFF' },
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
        name: '活跃学生数',
        type: 'line',
        yAxisIndex: 1,
        data: activeStudents,
        smooth: true,
        itemStyle: { color: '#67C23A' }
      }
    ]
  }
})

// 学生分布图配置
const distributionChartOption = computed(() => {
  if (!props.classData.distribution || props.classData.distribution.length === 0) {
    return null
  }

  const data = props.classData.distribution

  return {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} 人 ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [
      {
        type: 'pie',
        radius: ['40%', '70%'],
        avoidLabelOverlap: false,
        itemStyle: {
          borderRadius: 10,
          borderColor: '#fff',
          borderWidth: 2
        },
        label: {
          show: true,
          formatter: '{b}: {c} 人'
        },
        emphasis: {
          label: {
            show: true,
            fontSize: 16,
            fontWeight: 'bold'
          }
        },
        data: data.map(item => ({
          name: item.range,
          value: item.count
        }))
      }
    ]
  }
})
</script>

<style scoped>
.class-progress-stats-component {
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.overview-cards {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
}

.overview-card {
  transition: all 0.3s ease;
}

.overview-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.15);
}

.overview-card :deep(.el-card__body) {
  padding: 25px;
}

.card-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.card-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 70px;
  border-radius: 12px;
  color: #fff;
}

.card-icon.students {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
}

.card-icon.active {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
}

.card-icon.average {
  background: linear-gradient(135deg, #4facfe 0%, #00f2fe 100%);
}

.card-icon.total {
  background: linear-gradient(135deg, #43e97b 0%, #38f9d7 100%);
}

.card-info {
  flex: 1;
}

.card-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.card-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin-bottom: 4px;
}

.card-value.primary {
  color: #409EFF;
}

.card-unit {
  font-size: 12px;
  color: #909399;
}

.ranking-card :deep(.el-card__header) {
  padding: 20px 25px;
  border-bottom: 2px solid #EBEEF5;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header .title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: 600;
  color: #303133;
}

.card-header .subtitle {
  font-size: 14px;
  color: #909399;
}

.ranking-list {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.ranking-item {
  display: flex;
  align-items: center;
  gap: 20px;
  padding: 15px;
  background: #F5F7FA;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.ranking-item:hover {
  background: #E8EEF5;
  transform: translateX(5px);
}

.ranking-item.top-three {
  background: linear-gradient(135deg, #FFF9E6 0%, #FFF3D6 100%);
  border: 2px solid #FFD700;
}

.rank-badge {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 50px;
  height: 50px;
  border-radius: 50%;
  font-size: 24px;
  font-weight: bold;
}

.rank-badge.rank-1,
.rank-badge.rank-2,
.rank-badge.rank-3 {
  background: transparent;
}

.rank-badge .rank-number {
  font-size: 20px;
  color: #606266;
}

.student-info {
  display: flex;
  align-items: center;
  gap: 15px;
  flex: 1;
}

.student-details {
  flex: 1;
}

.student-name {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 4px;
}

.student-meta {
  font-size: 13px;
  color: #909399;
}

.student-score {
  text-align: right;
}

.score-value {
  font-size: 24px;
  font-weight: bold;
  color: #409EFF;
  margin-bottom: 4px;
}

.score-label {
  font-size: 12px;
  color: #909399;
}

.chart-card :deep(.el-card__header) {
  padding: 20px 25px;
  border-bottom: 2px solid #EBEEF5;
}

.chart-container {
  padding: 20px;
  min-height: 350px;
}

.chart {
  width: 100%;
  height: 350px;
}

@media (max-width: 1200px) {
  .overview-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .overview-cards {
    grid-template-columns: 1fr;
  }

  .card-content {
    gap: 15px;
  }

  .card-icon {
    width: 60px;
    height: 60px;
  }

  .card-value {
    font-size: 24px;
  }

  .ranking-item {
    flex-direction: column;
    text-align: center;
  }

  .student-info {
    flex-direction: column;
  }

  .chart-container {
    padding: 15px;
    min-height: 300px;
  }

  .chart {
    height: 300px;
  }
}
</style>

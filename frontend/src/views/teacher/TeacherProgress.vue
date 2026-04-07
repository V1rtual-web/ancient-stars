<template>
  <div class="teacher-progress-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <div class="header-left">
        <h2>学生自主学习进度</h2>
        <p class="subtitle">查看学生自主背单词的学习情况</p>
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

    <!-- 标签页 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange">
      <!-- 班级统计 -->
      <el-tab-pane label="班级统计" name="class">
        <div class="tab-content">
          <!-- 班级选择 -->
          <div class="class-selector">
            <span class="label">选择班级：</span>
            <el-select
              v-model="selectedClassId"
              placeholder="请选择班级"
              @change="handleClassChange"
              style="width: 250px"
            >
              <el-option
                v-for="cls in classes"
                :key="cls.id"
                :label="cls.name"
                :value="cls.id"
              />
            </el-select>
          </div>

          <!-- 加载状态 -->
          <div v-if="classLoading" class="loading-container">
            <el-icon class="is-loading" :size="40"><Loading /></el-icon>
            <p>加载中...</p>
          </div>

          <!-- 班级统计内容 -->
          <ClassProgressStats
            v-else-if="classProgressData"
            :class-data="classProgressData"
          />

          <!-- 空状态 -->
          <el-empty
            v-else
            description="请选择班级查看统计数据"
            :image-size="120"
          />
        </div>
      </el-tab-pane>

      <!-- 学生进度 -->
      <el-tab-pane label="学生进度" name="students">
        <div class="tab-content">
          <TeacherStudentProgress :initial-class-id="selectedClassId" />
        </div>
      </el-tab-pane>
    </el-tabs>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Refresh, Loading } from '@element-plus/icons-vue'
import { teacherAPI } from '../../api/teacher'
import ClassProgressStats from '../../components/ClassProgressStats.vue'
import TeacherStudentProgress from '../../components/TeacherStudentProgress.vue'

// 状态
const loading = ref(false)
const classLoading = ref(false)
const activeTab = ref('class')
const classes = ref([])
const selectedClassId = ref(null)
const classProgressData = ref(null)

// 加载班级列表
const loadClasses = async () => {
  try {
    const response = await teacherAPI.getClasses()
    if (response.data && response.data.length > 0) {
      classes.value = response.data
      // 默认选择第一个班级
      if (!selectedClassId.value) {
        selectedClassId.value = classes.value[0].id
        loadClassProgress()
      }
    }
  } catch (error) {
    console.error('加载班级列表失败:', error)
    ElMessage.error('加载班级列表失败')
  }
}

// 加载班级统计数据
const loadClassProgress = async () => {
  if (!selectedClassId.value) {
    return
  }

  classLoading.value = true
  
  try {
    const params = {
      classId: selectedClassId.value,
      timeRange: 7 // 默认最近7天
    }

    const response = await teacherAPI.getClassProgress(params)
    if (response.data) {
      classProgressData.value = response.data
    }
  } catch (error) {
    console.error('加载班级统计失败:', error)
    ElMessage.error('加载班级统计失败')
  } finally {
    classLoading.value = false
  }
}

// 处理班级变化
const handleClassChange = () => {
  loadClassProgress()
}

// 处理标签页变化
const handleTabChange = (tabName) => {
  if (tabName === 'class' && selectedClassId.value) {
    loadClassProgress()
  }
}

// 处理刷新
const handleRefresh = () => {
  if (activeTab.value === 'class') {
    loadClassProgress()
  } else {
    // 学生进度标签页的刷新由子组件处理
    ElMessage.success('请使用筛选工具栏中的查询按钮刷新数据')
  }
}

// 初始化
onMounted(() => {
  loadClasses()
})
</script>

<style scoped>
.teacher-progress-page {
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

/* 标签页内容 */
.tab-content {
  padding: 20px 0;
}

/* 班级选择器 */
.class-selector {
  display: flex;
  align-items: center;
  gap: 15px;
  margin-bottom: 25px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.class-selector .label {
  font-weight: 500;
  color: #606266;
  white-space: nowrap;
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

/* 响应式设计 */
@media (max-width: 768px) {
  .teacher-progress-page {
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

  .class-selector {
    flex-direction: column;
    align-items: stretch;
  }

  .class-selector .el-select {
    width: 100% !important;
  }
}

@media (max-width: 480px) {
  .header-left h2 {
    font-size: 20px;
  }
}
</style>

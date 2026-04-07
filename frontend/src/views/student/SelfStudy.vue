<template>
  <div class="self-study-page">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>自主学习</h2>
      <div class="header-actions">
        <el-button
          type="primary"
          :icon="TrendCharts"
          @click="showProgressDialog = true"
        >
          学习进度
        </el-button>
        <el-button
          @click="toggleViewMode"
          :icon="viewMode === 'card' ? Grid : Reading"
        >
          {{ viewMode === 'card' ? '列表模式' : '卡片模式' }}
        </el-button>
      </div>
    </div>

    <!-- 筛选和搜索工具栏 -->
    <div class="toolbar">
      <div class="filter-section">
        <span class="label">词汇表：</span>
        <el-select
          v-model="selfStudyStore.filters.wordListId"
          placeholder="全部词汇表"
          clearable
          @change="handleFilterChange"
          style="width: 200px"
        >
          <el-option
            v-for="wordList in wordLists"
            :key="wordList.id"
            :label="wordList.name"
            :value="wordList.id"
          />
        </el-select>
      </div>

      <div class="search-section">
        <el-input
          v-model="selfStudyStore.filters.keyword"
          placeholder="搜索单词..."
          clearable
          @clear="handleSearch"
          @keyup.enter="handleSearch"
          style="width: 300px"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="selfStudyStore.loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 内容区域 -->
    <div v-else-if="selfStudyStore.vocabularies.length > 0">
      <!-- 卡片模式 -->
      <div v-if="viewMode === 'card'" class="card-mode">
        <StudyCard
          :vocabularies="selfStudyStore.vocabularies"
          :initial-index="selfStudyStore.currentIndex"
          @learned="handleLearned"
          @index-change="handleIndexChange"
          @group-finished="handleGroupFinished"
        />
      </div>

      <!-- 列表模式 -->
      <div v-else class="list-mode">
        <VocabularyList
          :vocabularies="selfStudyStore.vocabularies"
          :total="selfStudyStore.total"
          :loading="selfStudyStore.loading"
          @learned="handleLearned"
          @page-change="handlePageChange"
          @size-change="handleSizeChange"
        />
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty
      v-else
      description="暂无词汇"
      :image-size="120"
    >
      <template #description>
        <p>{{ emptyMessage }}</p>
      </template>
      <el-button type="primary" @click="handleResetFilters">
        重置筛选
      </el-button>
    </el-empty>

    <!-- 学习进度对话框 -->
    <el-dialog
      v-model="showProgressDialog"
      title="学习进度"
      width="600px"
      :close-on-click-modal="false"
    >
      <div v-if="progressLoading" class="progress-loading">
        <el-icon class="is-loading" :size="30"><Loading /></el-icon>
        <p>加载中...</p>
      </div>
      <div v-else-if="selfStudyStore.progress" class="progress-content">
        <!-- 统计卡片 -->
        <div class="stats-cards">
          <el-card class="stat-card">
            <div class="stat-label">今日新学</div>
            <div class="stat-value">{{ selfStudyStore.progress.todayNewWords || 0 }}</div>
            <div class="stat-unit">个单词</div>
          </el-card>
          <el-card class="stat-card">
            <div class="stat-label">今日复习</div>
            <div class="stat-value">{{ selfStudyStore.progress.todayReviewWords || 0 }}</div>
            <div class="stat-unit">个单词</div>
          </el-card>
          <el-card class="stat-card">
            <div class="stat-label">本周新学</div>
            <div class="stat-value">{{ selfStudyStore.progress.weekNewWords || 0 }}</div>
            <div class="stat-unit">个单词</div>
          </el-card>
          <el-card class="stat-card">
            <div class="stat-label">累计已学</div>
            <div class="stat-value primary">{{ selfStudyStore.progress.totalLearnedWords || 0 }}</div>
            <div class="stat-unit">个单词</div>
          </el-card>
        </div>

        <!-- 鼓励信息 -->
        <div class="encouragement">
          <el-alert
            :title="getEncouragementMessage()"
            type="success"
            :closable="false"
            show-icon
          />
        </div>
      </div>
      <template #footer>
        <el-button @click="showProgressDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Loading, TrendCharts, Grid, Reading } from '@element-plus/icons-vue'
import { useSelfStudyStore } from '../../store/modules/selfStudy'
import { vocabularyAPI } from '../../api/vocabulary'
import StudyCard from '../../components/StudyCard.vue'
import VocabularyList from '../../components/VocabularyList.vue'

// Store
const selfStudyStore = useSelfStudyStore()

// 数据状态
const wordLists = ref([])
const viewMode = ref('card') // 'card' 或 'list'
const showProgressDialog = ref(false)
const progressLoading = ref(false)

// 空状态消息
const emptyMessage = computed(() => {
  if (selfStudyStore.filters.keyword) {
    return `未找到包含"${selfStudyStore.filters.keyword}"的词汇`
  }
  if (selfStudyStore.filters.wordListId) {
    return '该词汇表暂无词汇'
  }
  return '暂无词汇，请联系老师添加'
})

// 加载词汇表列表
const loadWordLists = async () => {
  try {
    const response = await vocabularyAPI.getWordLists()
    if (response.data) {
      wordLists.value = response.data
    }
  } catch (error) {
    console.error('加载词汇表失败:', error)
  }
}

// 加载词汇列表
const loadVocabularies = async () => {
  const result = await selfStudyStore.loadVocabularies()
  if (!result.success) {
    ElMessage.error(result.message || '加载词汇失败')
  }
}

// 处理筛选变化
const handleFilterChange = () => {
  selfStudyStore.pagination.page = 1
  loadVocabularies()
}

// 处理搜索
const handleSearch = () => {
  selfStudyStore.pagination.page = 1
  loadVocabularies()
}

// 处理重置筛选
const handleResetFilters = () => {
  selfStudyStore.resetFilters()
  loadVocabularies()
}

// 处理已掌握
const handleLearned = async (vocabularyId) => {
  const result = await selfStudyStore.markAsLearned(vocabularyId)
  if (result.success) {
    ElMessage.success('标记成功！继续加油 💪')
  } else {
    ElMessage.error(result.message || '标记失败')
  }
}

// 处理索引变化
const handleIndexChange = (index) => {
  selfStudyStore.setCurrentIndex(index)
}

// 处理一组词汇完成
const handleGroupFinished = async () => {  const currentPage = selfStudyStore.pagination.page
  const totalPages = Math.ceil(selfStudyStore.total / selfStudyStore.pagination.size)
  const learnedCount = selfStudyStore.pagination.page * selfStudyStore.pagination.size

  const hasMore = currentPage < totalPages

  const encouragements = [
    '🎉 太棒了！你已完成这组单词！',
    '🌟 厉害！又掌握了一批新词汇！',
    '💪 坚持就是胜利！这组单词拿下了！',
    '🚀 学习进度飞速！继续保持！',
  ]
  const msg = encouragements[Math.floor(Math.random() * encouragements.length)]

  if (hasMore) {
    try {
      await ElMessageBox.confirm(
        `${msg}\n\n已完成第 ${currentPage} 组（约 ${learnedCount} 个单词），是否继续学习下一组？`,
        '完成一组！',
        {
          confirmButtonText: '继续下一组 →',
          cancelButtonText: '休息一下',
          type: 'success',
          dangerouslyUseHTMLString: false,
        }
      )
      // 加载下一页
      selfStudyStore.pagination.page++
      await loadVocabularies()
    } catch {
      // 用户选择休息，不做任何操作
    }
  } else {
    // 已是最后一组
    ElMessageBox.alert(
      `${msg}\n\n🏆 恭喜你！已完成全部 ${selfStudyStore.total} 个单词的学习！`,
      '全部完成！',
      {
        confirmButtonText: '太棒了！',
        type: 'success',
      }
    )
  }
}

// 处理页码变化
const handlePageChange = (page) => {
  selfStudyStore.pagination.page = page
  loadVocabularies()
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  selfStudyStore.pagination.size = size
  selfStudyStore.pagination.page = 1
  loadVocabularies()
}

// 切换视图模式
const toggleViewMode = () => {
  viewMode.value = viewMode.value === 'card' ? 'list' : 'card'
}

// 加载学习进度
const loadProgress = async () => {
  progressLoading.value = true
  const result = await selfStudyStore.loadProgress()
  progressLoading.value = false
  
  if (!result.success) {
    ElMessage.error(result.message || '加载进度失败')
  }
}

// 获取鼓励信息
const getEncouragementMessage = () => {
  const todayWords = selfStudyStore.progress?.todayNewWords || 0
  const totalWords = selfStudyStore.progress?.totalLearnedWords || 0
  
  if (todayWords === 0) {
    return '今天还没有学习新单词，开始学习吧！'
  } else if (todayWords < 10) {
    return `今天已学习 ${todayWords} 个单词，继续加油！`
  } else if (todayWords < 20) {
    return `今天已学习 ${todayWords} 个单词，表现不错！`
  } else {
    return `今天已学习 ${todayWords} 个单词，太棒了！累计已掌握 ${totalWords} 个单词！`
  }
}

// 监听进度对话框打开
const handleProgressDialogOpen = () => {
  if (showProgressDialog.value) {
    loadProgress()
  }
}

// 初始化
onMounted(() => {
  loadWordLists()
  loadVocabularies()
})

// 监听对话框状态
watch(showProgressDialog, (newVal) => {
  if (newVal) {
    loadProgress()
  }
})
</script>

<style scoped>
.self-study-page {
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
}

.page-header h2 {
  margin: 0;
  font-size: 28px;
  color: #303133;
}

.header-actions {
  display: flex;
  gap: 10px;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 25px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  gap: 20px;
}

.filter-section,
.search-section {
  display: flex;
  align-items: center;
  gap: 10px;
}

.label {
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

/* 卡片模式 */
.card-mode {
  margin-top: 20px;
}

/* 列表模式 */
.list-mode {
  margin-top: 20px;
}

/* 学习进度对话框 */
.progress-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  color: #909399;
}

.progress-loading p {
  margin-top: 10px;
}

.progress-content {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 15px;
}

.stat-card {
  text-align: center;
  padding: 20px;
}

.stat-card :deep(.el-card__body) {
  padding: 20px;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #606266;
  margin-bottom: 5px;
}

.stat-value.primary {
  color: #409EFF;
}

.stat-unit {
  font-size: 12px;
  color: #909399;
}

.encouragement {
  margin-top: 10px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .self-study-page {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 15px;
  }

  .page-header h2 {
    font-size: 24px;
  }

  .header-actions {
    width: 100%;
    justify-content: flex-start;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
    gap: 15px;
  }

  .filter-section,
  .search-section {
    flex-direction: column;
    align-items: stretch;
  }

  .filter-section .el-select,
  .search-section .el-input {
    width: 100% !important;
  }

  .stats-cards {
    grid-template-columns: 1fr;
  }

  .stat-value {
    font-size: 28px;
  }
}

@media (max-width: 480px) {
  .header-actions {
    flex-direction: column;
    width: 100%;
  }

  .header-actions .el-button {
    width: 100%;
  }
}
</style>

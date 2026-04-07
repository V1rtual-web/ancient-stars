<template>
  <div class="vocabulary-list">
    <!-- 页面标题 -->
    <div class="page-header">
      <h2>自主学习</h2>
      <p class="subtitle">选择词汇开始学习</p>
    </div>

    <!-- 筛选和搜索工具栏 -->
    <div class="toolbar">
      <div class="filter-section">
        <span class="label">词汇表：</span>
        <el-select
          v-model="filters.wordListId"
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
          v-model="filters.keyword"
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
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 词汇列表 -->
    <div v-else-if="vocabularies.length > 0" class="vocabulary-grid">
      <el-card
        v-for="vocab in vocabularies"
        :key="vocab.id"
        class="vocabulary-card"
        shadow="hover"
        :class="{ 'learned': vocab.isLearned }"
      >
        <div class="card-header">
          <h3 class="word">{{ vocab.word }}</h3>
          <el-tag v-if="vocab.isLearned" type="success" size="small">
            已学习
          </el-tag>
        </div>

        <p v-if="vocab.phonetic" class="phonetic">{{ vocab.phonetic }}</p>
        
        <div class="translation">
          <p>{{ vocab.translation }}</p>
        </div>

        <div v-if="vocab.example" class="example">
          <p class="example-label">例句：</p>
          <p class="example-text">{{ vocab.example }}</p>
        </div>

        <div class="card-actions">
          <el-button
            v-if="!vocab.isLearned"
            type="primary"
            size="small"
            @click="markAsLearned(vocab)"
            :loading="markingId === vocab.id"
          >
            标记已掌握
          </el-button>
          <el-button
            v-else
            type="success"
            size="small"
            plain
            disabled
          >
            <el-icon><Check /></el-icon>
            已掌握
          </el-button>
        </div>
      </el-card>
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
    </el-empty>

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 30, 50]"
        :total="total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { selfStudyAPI } from '../../api/selfStudy'
import { vocabularyAPI } from '../../api/vocabulary'
import { ElMessage } from 'element-plus'
import { Search, Loading, Check } from '@element-plus/icons-vue'

// 数据状态
const loading = ref(false)
const vocabularies = ref([])
const wordLists = ref([])
const total = ref(0)
const markingId = ref(null)

// 筛选条件
const filters = ref({
  wordListId: null,
  keyword: ''
})

// 分页参数
const pagination = ref({
  page: 1,
  size: 20
})

// 空状态消息
const emptyMessage = computed(() => {
  if (filters.value.keyword) {
    return `未找到包含"${filters.value.keyword}"的词汇`
  }
  if (filters.value.wordListId) {
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
  try {
    loading.value = true
    
    const params = {
      page: pagination.value.page,
      size: pagination.value.size
    }
    
    if (filters.value.wordListId) {
      params.wordListId = filters.value.wordListId
    }
    
    if (filters.value.keyword) {
      params.keyword = filters.value.keyword
    }
    
    const response = await selfStudyAPI.getVocabularies(params)
    
    if (response.data) {
      vocabularies.value = response.data.items || []
      total.value = response.data.total || 0
    }
  } catch (error) {
    console.error('加载词汇失败:', error)
    ElMessage.error('加载词汇失败，请稍后重试')
  } finally {
    loading.value = false
  }
}

// 标记已掌握
const markAsLearned = async (vocab) => {
  try {
    markingId.value = vocab.id
    
    await selfStudyAPI.createStudyRecord(vocab.id)
    
    // 更新本地状态
    vocab.isLearned = true
    
    ElMessage.success('标记成功！继续加油 💪')
  } catch (error) {
    console.error('标记失败:', error)
    ElMessage.error('标记失败，请稍后重试')
  } finally {
    markingId.value = null
  }
}

// 处理筛选变化
const handleFilterChange = () => {
  pagination.value.page = 1
  loadVocabularies()
}

// 处理搜索
const handleSearch = () => {
  pagination.value.page = 1
  loadVocabularies()
}

// 处理页码变化
const handlePageChange = () => {
  loadVocabularies()
  // 滚动到顶部
  window.scrollTo({ top: 0, behavior: 'smooth' })
}

// 处理每页数量变化
const handleSizeChange = () => {
  pagination.value.page = 1
  loadVocabularies()
}

// 初始化
onMounted(() => {
  loadWordLists()
  loadVocabularies()
})
</script>

<style scoped>
.vocabulary-list {
  max-width: 1400px;
  margin: 0 auto;
  padding: 20px;
}

/* 页面标题 */
.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  margin: 0 0 8px 0;
  font-size: 28px;
  color: #303133;
}

.subtitle {
  margin: 0;
  font-size: 14px;
  color: #909399;
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

/* 词汇网格 */
.vocabulary-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

/* 词汇卡片 */
.vocabulary-card {
  transition: all 0.3s ease;
}

.vocabulary-card:hover {
  transform: translateY(-4px);
}

.vocabulary-card.learned {
  background-color: #f0f9ff;
}

.vocabulary-card :deep(.el-card__body) {
  padding: 20px;
  display: flex;
  flex-direction: column;
  gap: 12px;
  min-height: 200px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  gap: 10px;
  padding-bottom: 10px;
  border-bottom: 2px solid #EBEEF5;
}

.word {
  margin: 0;
  font-size: 24px;
  font-weight: bold;
  color: #303133;
  word-break: break-word;
}

.phonetic {
  margin: 0;
  font-size: 16px;
  color: #909399;
  font-style: italic;
}

.translation {
  flex: 1;
}

.translation p {
  margin: 0;
  font-size: 16px;
  color: #606266;
  line-height: 1.6;
}

.example {
  padding: 12px;
  background: #F5F7FA;
  border-radius: 6px;
  border-left: 3px solid #409EFF;
}

.example-label {
  margin: 0 0 6px 0;
  font-size: 12px;
  color: #909399;
  font-weight: 500;
}

.example-text {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  font-style: italic;
}

.card-actions {
  padding-top: 10px;
  border-top: 1px solid #EBEEF5;
}

.card-actions .el-button {
  width: 100%;
}

/* 分页 */
.pagination-container {
  display: flex;
  justify-content: center;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .vocabulary-list {
    padding: 10px;
  }

  .page-header h2 {
    font-size: 24px;
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

  .vocabulary-grid {
    grid-template-columns: 1fr;
    gap: 15px;
  }

  .word {
    font-size: 20px;
  }

  .pagination-container {
    padding: 15px 10px;
  }

  .pagination-container :deep(.el-pagination) {
    flex-wrap: wrap;
    justify-content: center;
  }
}

@media (min-width: 769px) and (max-width: 1024px) {
  .vocabulary-grid {
    grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  }
}
</style>

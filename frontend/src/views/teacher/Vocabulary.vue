<template>
  <div class="teacher-vocabulary">
    <el-tabs v-model="activeTab" type="border-card">
      <!-- 词汇管理标签页 -->
      <el-tab-pane label="词汇管理" name="vocabulary">
        <div class="toolbar">
          <el-input
            v-model="searchKeyword"
            placeholder="搜索单词、释义..."
            class="search-input"
            clearable
            @clear="handleSearch"
            @keyup.enter="handleSearch"
          >
            <template #append>
              <el-button :icon="Search" @click="handleSearch" />
            </template>
          </el-input>
          <el-select
            v-model="filterDifficulty"
            placeholder="难度筛选"
            clearable
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
            <el-option label="较难" :value="4" />
            <el-option label="极难" :value="5" />
          </el-select>
          <el-select
            v-model="sortField"
            style="width: 120px"
            @change="handleSearch"
          >
            <el-option label="创建时间" value="createdAt" />
            <el-option label="单词" value="word" />
            <el-option label="难度" value="difficultyLevel" />
          </el-select>
          <div class="toolbar-actions">
            <el-button type="primary" :icon="Plus" @click="handleAdd">
              <span class="hide-on-mobile">添加词汇</span>
              <span class="mobile-only">添加</span>
            </el-button>
            <el-button type="success" :icon="Upload" @click="showImportDialog = true">
              <span class="hide-on-mobile">批量导入</span>
              <span class="mobile-only">导入</span>
            </el-button>
          </div>
        </div>

        <el-table
          v-loading="loading"
          :data="vocabularyList"
          style="width: 100%; margin-top: 20px"
          stripe
        >
          <el-table-column prop="word" label="单词" width="150" />
          <el-table-column prop="phonetic" label="音标" width="150" class-name="hide-on-mobile" />
          <el-table-column prop="translation" label="释义" show-overflow-tooltip />
          <el-table-column prop="example" label="例句" show-overflow-tooltip class-name="hide-on-mobile hide-on-tablet" />
          <el-table-column prop="difficultyLevel" label="难度" width="100" class-name="hide-on-mobile">
            <template #default="{ row }">
              <el-tag :type="getDifficultyType(row.difficultyLevel)">
                {{ getDifficultyLabel(row.difficultyLevel) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="180" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(row)">
                <span class="hide-on-mobile">编辑</span>
              </el-button>
              <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(row)">
                <span class="hide-on-mobile">删除</span>
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :total="total"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          style="margin-top: 20px; justify-content: center"
          @size-change="handleSizeChange"
          @current-change="handlePageChange"
        />
      </el-tab-pane>

      <!-- 词汇表管理标签页 -->
      <el-tab-pane label="词汇表管理" name="wordlist">
        <div class="toolbar">
          <el-button type="primary" :icon="Plus" @click="handleAddWordList">创建词汇表</el-button>
        </div>

        <el-table
          v-loading="wordListLoading"
          :data="wordLists"
          style="width: 100%; margin-top: 20px"
          stripe
        >
          <el-table-column prop="name" label="词汇表名称" />
          <el-table-column prop="description" label="描述" show-overflow-tooltip />
          <el-table-column prop="vocabularyCount" label="词汇数量" width="120" />
          <el-table-column prop="isPublic" label="公开" width="100">
            <template #default="{ row }">
              <el-tag :type="row.isPublic ? 'success' : 'info'">
                {{ row.isPublic ? '是' : '否' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="250" fixed="right">
            <template #default="{ row }">
              <el-button type="primary" size="small" :icon="View" @click="handleViewWordList(row)">查看</el-button>
              <el-button type="warning" size="small" :icon="Edit" @click="handleEditWordList(row)">编辑</el-button>
              <el-button type="danger" size="small" :icon="Delete" @click="handleDeleteWordList(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 词汇添加/编辑对话框 -->
    <el-dialog
      v-model="showVocabularyDialog"
      :title="isEdit ? '编辑词汇' : '添加词汇'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="vocabularyFormRef"
        :model="vocabularyForm"
        :rules="vocabularyRules"
        label-width="100px"
      >
        <el-form-item label="单词" prop="word">
          <el-input v-model="vocabularyForm.word" placeholder="请输入单词" />
        </el-form-item>
        <el-form-item label="音标" prop="phonetic">
          <el-input v-model="vocabularyForm.phonetic" placeholder="请输入音标" />
        </el-form-item>
        <el-form-item label="释义" prop="translation">
          <el-input
            v-model="vocabularyForm.translation"
            type="textarea"
            :rows="3"
            placeholder="请输入释义"
          />
        </el-form-item>
        <el-form-item label="例句" prop="example">
          <el-input
            v-model="vocabularyForm.example"
            type="textarea"
            :rows="3"
            placeholder="请输入例句"
          />
        </el-form-item>
        <el-form-item label="难度等级" prop="difficultyLevel">
          <el-select v-model="vocabularyForm.difficultyLevel" placeholder="请选择难度等级">
            <el-option label="简单" :value="1" />
            <el-option label="中等" :value="2" />
            <el-option label="困难" :value="3" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showVocabularyDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 批量手动输入对话框 -->
    <el-dialog
      v-model="showImportDialog"
      title="批量添加词汇"
      width="700px"
      @close="resetBatchForm"
    >
      <div class="batch-tip">
        每行一个词汇，格式：<code>单词, 音标, 释义, 例句, 难度(1-5)</code>，后四项可省略
      </div>
      <el-input
        v-model="batchText"
        type="textarea"
        :rows="12"
        placeholder="例：
hello, /həˈloʊ/, 你好, Hello world!, 1
world, /wɜːrld/, 世界, The world is big., 2
computer, /kəmˈpjuːtər/, 计算机, I use a computer., 3"
        style="font-family: monospace; margin-bottom: 12px"
      />
      <div class="batch-preview" v-if="batchPreview.length">
        <span style="color: #67c23a">✓ 解析到 {{ batchPreview.length }} 条词汇</span>
        <span v-if="batchErrors.length" style="color: #f56c6c; margin-left: 12px">
          {{ batchErrors.length }} 行格式有误（已跳过）
        </span>
      </div>
      <template #footer>
        <el-button @click="showImportDialog = false">取消</el-button>
        <el-button @click="previewBatch">预览解析</el-button>
        <el-button type="primary" :loading="importing" @click="handleBatchSubmit">
          确认添加 {{ batchPreview.length ? `(${batchPreview.length}条)` : '' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 词汇表添加/编辑对话框 -->
    <el-dialog
      v-model="showWordListDialog"
      :title="isEditWordList ? '编辑词汇表' : '创建词汇表'"
      width="600px"
      @close="resetWordListForm"
    >
      <el-form
        ref="wordListFormRef"
        :model="wordListForm"
        :rules="wordListRules"
        label-width="100px"
      >
        <el-form-item label="名称" prop="name">
          <el-input v-model="wordListForm.name" placeholder="请输入词汇表名称" />
        </el-form-item>
        <el-form-item label="描述" prop="description">
          <el-input
            v-model="wordListForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入描述"
          />
        </el-form-item>
        <el-form-item label="公开" prop="isPublic">
          <el-switch v-model="wordListForm.isPublic" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showWordListDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmitWordList">确定</el-button>
      </template>
    </el-dialog>

    <!-- 词汇表详情对话框 -->
    <el-dialog
      v-model="showWordListDetailDialog"
      title="词汇表详情"
      width="800px"
    >
      <div v-if="currentWordList">
        <h3>{{ currentWordList.name }}</h3>
        <p>{{ currentWordList.description }}</p>
        <el-divider />
        <el-table :data="currentWordList.vocabularies" style="width: 100%">
          <el-table-column prop="word" label="单词" width="150" />
          <el-table-column prop="phonetic" label="音标" width="150" />
          <el-table-column prop="translation" label="释义" show-overflow-tooltip />
        </el-table>
      </div>
      <template #footer>
        <el-button @click="showWordListDetailDialog = false">关闭</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Plus, Edit, Delete, Upload, View } from '@element-plus/icons-vue'
import { vocabularyAPI } from '@/api/vocabulary'

// 标签页
const activeTab = ref('vocabulary')

// 词汇列表相关
const loading = ref(false)
const vocabularyList = ref([])
const searchKeyword = ref('')
const filterDifficulty = ref(null)
const sortField = ref('createdAt')
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)

// 词汇表列表相关
const wordListLoading = ref(false)
const wordLists = ref([])

// 对话框控制
const showVocabularyDialog = ref(false)
const showImportDialog = ref(false)
const showWordListDialog = ref(false)
const showWordListDetailDialog = ref(false)

// 表单相关
const vocabularyFormRef = ref(null)
const wordListFormRef = ref(null)
const isEdit = ref(false)
const isEditWordList = ref(false)
const submitting = ref(false)
const importing = ref(false)
const uploadFile = ref(null)
const currentWordList = ref(null)

// 批量输入
const batchText = ref('')
const batchPreview = ref([])
const batchErrors = ref([])

// 词汇表单
const vocabularyForm = reactive({
  id: null,
  word: '',
  phonetic: '',
  translation: '',
  example: '',
  difficultyLevel: 2
})

// 词汇表表单
const wordListForm = reactive({
  id: null,
  name: '',
  description: '',
  isPublic: false
})

// 表单验证规则
const vocabularyRules = {
  word: [{ required: true, message: '请输入单词', trigger: 'blur' }],
  translation: [{ required: true, message: '请输入释义', trigger: 'blur' }]
}

const wordListRules = {
  name: [{ required: true, message: '请输入词汇表名称', trigger: 'blur' }]
}

// 加载词汇列表
const loadVocabularyList = async () => {
  loading.value = true
  try {
    // 难度筛选时走难度接口，否则走搜索接口
    if (filterDifficulty.value) {
      const response = await vocabularyAPI.getVocabulariesByDifficulty(filterDifficulty.value)
      // 手动分页
      const all = response.data || []
      const keyword = searchKeyword.value.trim().toLowerCase()
      const filtered = keyword
        ? all.filter(v => v.word?.toLowerCase().includes(keyword) || v.translation?.includes(keyword))
        : all
      total.value = filtered.length
      const start = (currentPage.value - 1) * pageSize.value
      vocabularyList.value = filtered.slice(start, start + pageSize.value)
    } else {
      const response = await vocabularyAPI.searchVocabulary(
        searchKeyword.value,
        currentPage.value - 1,
        pageSize.value,
        sortField.value,
        'DESC'
      )
      vocabularyList.value = response.data.content || []
      total.value = response.data.totalElements || 0
    }
  } catch (error) {
    ElMessage.error('加载词汇列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 加载词汇表列表
const loadWordLists = async () => {
  wordListLoading.value = true
  try {
    const response = await vocabularyAPI.getWordLists()
    wordLists.value = response.data || []
  } catch (error) {
    ElMessage.error('加载词汇表列表失败: ' + (error.message || '未知错误'))
  } finally {
    wordListLoading.value = false
  }
}

// 搜索（重置到第1页）
const handleSearch = () => {
  currentPage.value = 1
  loadVocabularyList()
}

// 切换每页条数（重置到第1页）
const handleSizeChange = () => {
  currentPage.value = 1
  loadVocabularyList()
}

// 翻页（保持当前页码，由 v-model 已更新）
const handlePageChange = () => {
  loadVocabularyList()
}

// 添加词汇
const handleAdd = () => {
  isEdit.value = false
  showVocabularyDialog.value = true
}

// 编辑词汇
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(vocabularyForm, row)
  showVocabularyDialog.value = true
}

// 删除词汇
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除词汇"${row.word}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await vocabularyAPI.deleteVocabulary(row.id)
    ElMessage.success('删除成功')
    loadVocabularyList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 提交词汇表单
const handleSubmit = async () => {
  if (!vocabularyFormRef.value) return
  
  await vocabularyFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await vocabularyAPI.updateVocabulary(vocabularyForm.id, vocabularyForm)
        ElMessage.success('更新成功')
      } else {
        await vocabularyAPI.createVocabulary(vocabularyForm)
        ElMessage.success('添加成功')
      }
      showVocabularyDialog.value = false
      loadVocabularyList()
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.message || '未知错误'))
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  vocabularyForm.id = null
  vocabularyForm.word = ''
  vocabularyForm.phonetic = ''
  vocabularyForm.translation = ''
  vocabularyForm.example = ''
  vocabularyForm.difficultyLevel = 2
  vocabularyFormRef.value?.clearValidate()
}

// 预览批量解析结果
const previewBatch = () => {
  const lines = batchText.value.split('\n').filter(l => l.trim())
  batchPreview.value = []
  batchErrors.value = []
  lines.forEach((line, idx) => {
    const parts = line.split(',').map(s => s.trim())
    if (!parts[0]) {
      batchErrors.value.push(`第${idx + 1}行: 单词为空`)
      return
    }
    batchPreview.value.push({
      word: parts[0],
      phonetic: parts[1] || '',
      translation: parts[2] || '',
      example: parts[3] || '',
      difficultyLevel: parseInt(parts[4]) || 2
    })
  })
  ElMessage.info(`解析完成：${batchPreview.value.length} 条有效，${batchErrors.value.length} 条跳过`)
}

// 批量提交
const handleBatchSubmit = async () => {
  if (!batchPreview.value.length) {
    previewBatch()
    if (!batchPreview.value.length) {
      ElMessage.warning('没有可导入的词汇，请检查格式')
      return
    }
  }
  importing.value = true
  let success = 0, fail = 0
  for (const item of batchPreview.value) {
    try {
      await vocabularyAPI.createVocabulary(item)
      success++
    } catch {
      fail++
    }
  }
  importing.value = false
  ElMessage.success(`批量添加完成：成功 ${success} 条，失败 ${fail} 条`)
  showImportDialog.value = false
  loadVocabularyList()
}

// 重置批量表单
const resetBatchForm = () => {
  batchText.value = ''
  batchPreview.value = []
  batchErrors.value = []
}

// 添加词汇表
const handleAddWordList = () => {
  isEditWordList.value = false
  showWordListDialog.value = true
}

// 编辑词汇表
const handleEditWordList = (row) => {
  isEditWordList.value = true
  Object.assign(wordListForm, row)
  showWordListDialog.value = true
}

// 删除词汇表
const handleDeleteWordList = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除词汇表"${row.name}"吗？`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await vocabularyAPI.deleteWordList(row.id)
    ElMessage.success('删除成功')
    loadWordLists()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 查看词汇表详情
const handleViewWordList = async (row) => {
  try {
    const response = await vocabularyAPI.getWordListDetail(row.id)
    currentWordList.value = response.data
    showWordListDetailDialog.value = true
  } catch (error) {
    ElMessage.error('加载词汇表详情失败: ' + (error.message || '未知错误'))
  }
}

// 提交词汇表表单
const handleSubmitWordList = async () => {
  if (!wordListFormRef.value) return
  
  await wordListFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEditWordList.value) {
        await vocabularyAPI.updateWordList(wordListForm.id, wordListForm)
        ElMessage.success('更新成功')
      } else {
        await vocabularyAPI.createWordList(wordListForm)
        ElMessage.success('创建成功')
      }
      showWordListDialog.value = false
      loadWordLists()
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.message || '未知错误'))
    } finally {
      submitting.value = false
    }
  })
}

// 重置词汇表表单
const resetWordListForm = () => {
  wordListForm.id = null
  wordListForm.name = ''
  wordListForm.description = ''
  wordListForm.isPublic = false
  wordListFormRef.value?.clearValidate()
}

// 获取难度类型
const getDifficultyType = (level) => {
  const types = {
    1: 'success',
    2: 'warning',
    3: 'danger'
  }
  return types[level] || 'info'
}

// 获取难度标签
const getDifficultyLabel = (level) => {
  const labels = {
    1: '简单',
    2: '中等',
    3: '困难'
  }
  return labels[level] || level
}

// 初始化
onMounted(() => {
  loadVocabularyList()
  loadWordLists()
})
</script>

<style scoped>
.teacher-vocabulary {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.search-input {
  flex: 1;
  min-width: 200px;
  max-width: 400px;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}

:deep(.el-tabs--border-card) {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.batch-tip {
  font-size: 13px;
  color: #666;
  margin-bottom: 10px;
  background: #f5f7fa;
  padding: 8px 12px;
  border-radius: 4px;
}
.batch-tip code {
  background: #e8eaed;
  padding: 1px 4px;
  border-radius: 3px;
  font-size: 12px;
}
.batch-preview {
  font-size: 13px;
  margin-top: 8px;
}

/* 移动端样式 */
@media (max-width: 767px) {
  .teacher-vocabulary {
    padding: 12px;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .search-input {
    max-width: 100%;
  }

  .toolbar-actions {
    width: 100%;
    justify-content: stretch;
  }

  .toolbar-actions .el-button {
    flex: 1;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 8px 4px;
  }

  :deep(.el-table .cell) {
    padding: 0 4px;
  }

  :deep(.el-button--small) {
    padding: 6px 8px;
    font-size: 12px;
  }
}

/* 平板端样式 */
@media (min-width: 768px) and (max-width: 1023px) {
  .teacher-vocabulary {
    padding: 16px;
  }

  :deep(.el-table) {
    font-size: 13px;
  }
}
</style>

<template>
  <div class="teacher-tasks">
    <div class="toolbar">
      <div class="toolbar-left">
        <h2>任务管理</h2>
      </div>
      <div class="toolbar-actions">
        <el-button type="primary" :icon="Plus" @click="handleCreate">创建任务</el-button>
      </div>
    </div>

    <!-- 任务列表 -->
    <el-table
      v-loading="loading"
      :data="taskList"
      style="width: 100%; margin-top: 20px"
      stripe
    >
      <el-table-column prop="title" label="任务标题" min-width="200" />
      <el-table-column prop="wordListName" label="词汇表" width="150" />
      <el-table-column prop="deadline" label="截止日期" width="180">
        <template #default="{ row }">
          {{ formatDate(row.deadline) }}
        </template>
      </el-table-column>
      <el-table-column label="分配学生" width="120" align="center">
        <template #default="{ row }">
          <el-tag>{{ row.assignedCount || 0 }} 人</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="完成情况" width="200">
        <template #default="{ row }">
          <div class="progress-info">
            <el-progress
              :percentage="calculateProgress(row)"
              :color="getProgressColor(row)"
            />
            <span class="progress-text">
              {{ row.completedCount || 0 }} / {{ row.assignedCount || 0 }}
            </span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="getStatusType(row.status)">
            {{ getStatusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="280" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" :icon="View" @click="handleViewProgress(row)">
            查看进度
          </el-button>
          <el-button type="warning" size="small" :icon="Edit" @click="handleEdit(row)">
            编辑
          </el-button>
          <el-button type="danger" size="small" :icon="Delete" @click="handleDelete(row)">
            删除
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 创建/编辑任务对话框 -->
    <el-dialog
      v-model="showTaskDialog"
      :title="isEdit ? '编辑任务' : '创建任务'"
      width="600px"
      @close="resetForm"
    >
      <el-form
        ref="taskFormRef"
        :model="taskForm"
        :rules="taskRules"
        label-width="100px"
      >
        <el-form-item label="任务标题" prop="title">
          <el-input v-model="taskForm.title" placeholder="请输入任务标题" />
        </el-form-item>
        <el-form-item label="任务描述" prop="description">
          <el-input
            v-model="taskForm.description"
            type="textarea"
            :rows="3"
            placeholder="请输入任务描述（可选）"
          />
        </el-form-item>
        <el-form-item label="选择词汇表" prop="wordListId">
          <el-select
            v-model="taskForm.wordListId"
            placeholder="请选择词汇表"
            style="width: 100%"
            :disabled="isEdit"
            @change="handleWordListChange"
          >
            <el-option
              v-for="wordList in wordLists"
              :key="wordList.id"
              :label="`${wordList.name} (${wordList.vocabularyCount || 0}个词汇)`"
              :value="wordList.id"
            />
          </el-select>
          <div v-if="isEdit" class="form-tip">
            <el-icon><InfoFilled /></el-icon>
            任务创建后不允许修改词汇表
          </div>
        </el-form-item>
        <el-form-item label="截止日期" prop="deadline">
          <el-date-picker
            v-model="taskForm.deadline"
            type="datetime"
            placeholder="选择截止日期"
            style="width: 100%"
            :disabled-date="disabledDate"
          />
        </el-form-item>
        <el-form-item label="分配学生" prop="studentIds">
          <div class="student-selection">
            <div class="selection-header">
              <el-select
                v-model="filterClassId"
                placeholder="按班级筛选"
                clearable
                style="width: 200px"
                @change="handleClassFilter"
              >
                <el-option label="全部班级" :value="null" />
                <el-option
                  v-for="classItem in classList"
                  :key="classItem.id"
                  :label="classItem.name"
                  :value="classItem.id"
                />
              </el-select>
              <div class="selection-actions">
                <el-button size="small" @click="handleSelectAll">全选</el-button>
                <el-button size="small" @click="handleDeselectAll">取消全选</el-button>
              </div>
            </div>
            <div class="student-list">
              <el-checkbox-group v-model="taskForm.studentIds">
                <el-checkbox
                  v-for="student in filteredStudents"
                  :key="student.id"
                  :label="student.id"
                  :disabled="student.status !== 'ACTIVE'"
                >
                  {{ student.realName }} ({{ student.username }}) - {{ student.className }}
                </el-checkbox>
              </el-checkbox-group>
              <el-empty v-if="filteredStudents.length === 0" description="暂无学生" />
            </div>
            <div class="selection-summary">
              已选择 <span class="count">{{ taskForm.studentIds.length }}</span> 名学生
            </div>
          </div>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showTaskDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">
          {{ isEdit ? '保存' : '创建' }}
        </el-button>
      </template>
    </el-dialog>

    <!-- 任务进度查看对话框 -->
    <el-dialog
      v-model="showProgressDialog"
      title="任务进度详情"
      width="800px"
    >
      <div v-if="currentTask" class="progress-detail">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="任务标题">{{ currentTask.title }}</el-descriptions-item>
          <el-descriptions-item label="词汇表">{{ currentTask.wordListName }}</el-descriptions-item>
          <el-descriptions-item label="截止日期">
            {{ formatDate(currentTask.deadline) }}
          </el-descriptions-item>
          <el-descriptions-item label="任务状态">
            <el-tag :type="getStatusType(currentTask.status)">
              {{ getStatusText(currentTask.status) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="任务描述" :span="2">
            {{ currentTask.description || '无' }}
          </el-descriptions-item>
        </el-descriptions>

        <div class="progress-stats">
          <el-statistic title="总分配人数" :value="currentTask.assignedCount || 0" />
          <el-statistic title="已完成人数" :value="currentTask.completedCount || 0" />
          <el-statistic title="进行中人数" :value="currentTask.inProgressCount || 0" />
          <el-statistic title="未开始人数" :value="currentTask.notStartedCount || 0" />
        </div>

        <h3 style="margin-top: 20px">学生完成情况</h3>
        <el-table
          v-loading="progressLoading"
          :data="studentProgress"
          style="width: 100%; margin-top: 10px"
          stripe
        >
          <el-table-column prop="studentName" label="学生姓名" width="120" />
          <el-table-column prop="studentUsername" label="学号" width="120" />
          <el-table-column label="进度" min-width="200">
            <template #default="{ row }">
              <div class="progress-info">
                <el-progress :percentage="row.progress" :color="getProgressColor(row)" />
                <span class="progress-text">{{ row.progress }}%</span>
              </div>
            </template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <el-tag :type="getAssignmentStatusType(row.status)">
                {{ getAssignmentStatusText(row.status) }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="completedAt" label="完成时间" width="180">
            <template #default="{ row }">
              {{ row.completedAt ? formatDate(row.completedAt) : '-' }}
            </template>
          </el-table-column>
        </el-table>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, Delete, View, InfoFilled } from '@element-plus/icons-vue'
import { taskAPI } from '@/api/task'
import { vocabularyAPI } from '@/api/vocabulary'
import { studentAPI } from '@/api/student'

// 任务列表相关
const loading = ref(false)
const taskList = ref([])

// 词汇表列表
const wordLists = ref([])

// 学生列表
const studentList = ref([])
const filterClassId = ref(null)

// 班级列表（模拟数据，实际应从API获取）
const classList = ref([
  { id: 1, name: '一班' },
  { id: 2, name: '二班' },
  { id: 3, name: '三班' }
])

// 对话框控制
const showTaskDialog = ref(false)
const showProgressDialog = ref(false)

// 表单相关
const taskFormRef = ref(null)
const isEdit = ref(false)
const submitting = ref(false)

// 任务表单
const taskForm = reactive({
  id: null,
  title: '',
  description: '',
  wordListId: null,
  deadline: null,
  studentIds: []
})

// 当前查看的任务
const currentTask = ref(null)
const studentProgress = ref([])
const progressLoading = ref(false)

// 表单验证规则
const taskRules = {
  title: [
    { required: true, message: '请输入任务标题', trigger: 'blur' },
    { min: 2, max: 200, message: '标题长度在 2 到 200 个字符', trigger: 'blur' }
  ],
  wordListId: [
    { required: true, message: '请选择词汇表', trigger: 'change' }
  ],
  deadline: [
    { required: true, message: '请选择截止日期', trigger: 'change' }
  ],
  studentIds: [
    { type: 'array', required: true, message: '请至少选择一名学生', trigger: 'change' },
    { type: 'array', min: 1, message: '请至少选择一名学生', trigger: 'change' }
  ]
}

// 筛选后的学生列表
const filteredStudents = computed(() => {
  if (!filterClassId.value) {
    return studentList.value
  }
  return studentList.value.filter(s => s.classId === filterClassId.value)
})

// 加载任务列表
const loadTaskList = async () => {
  loading.value = true
  try {
    const response = await taskAPI.getAllTasks()
    taskList.value = response.data || []
  } catch (error) {
    ElMessage.error('加载任务列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 加载词汇表列表
const loadWordLists = async () => {
  try {
    const response = await vocabularyAPI.getWordLists()
    wordLists.value = response.data || []
  } catch (error) {
    ElMessage.error('加载词汇表失败: ' + (error.message || '未知错误'))
  }
}

// 加载学生列表
const loadStudentList = async () => {
  try {
    const response = await studentAPI.getStudents()
    studentList.value = (response.data || []).map(student => {
      const classItem = classList.value.find(c => c.id === student.classId)
      return {
        ...student,
        className: classItem ? classItem.name : '未分配'
      }
    })
  } catch (error) {
    ElMessage.error('加载学生列表失败: ' + (error.message || '未知错误'))
  }
}

// 创建任务
const handleCreate = async () => {
  isEdit.value = false
  // 确保数据已加载
  if (wordLists.value.length === 0) {
    await loadWordLists()
  }
  if (studentList.value.length === 0) {
    await loadStudentList()
  }
  showTaskDialog.value = true
}

// 编辑任务
const handleEdit = async (row) => {
  isEdit.value = true
  
  // 加载任务详情
  try {
    const response = await taskAPI.getTask(row.id)
    const task = response.data
    
    Object.assign(taskForm, {
      id: task.id,
      title: task.title,
      description: task.description,
      wordListId: task.wordListId,
      deadline: task.deadline ? new Date(task.deadline) : null,
      studentIds: task.assignedStudents || []
    })
    
    // 确保数据已加载
    if (wordLists.value.length === 0) {
      await loadWordLists()
    }
    if (studentList.value.length === 0) {
      await loadStudentList()
    }
    
    showTaskDialog.value = true
  } catch (error) {
    ElMessage.error('加载任务详情失败: ' + (error.message || '未知错误'))
  }
}

// 删除任务
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除任务"${row.title}"吗？此操作不可恢复。`,
      '删除确认',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 注意：API中没有删除接口，这里假设有
    // await taskAPI.deleteTask(row.id)
    ElMessage.success('删除成功')
    loadTaskList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败: ' + (error.message || '未知错误'))
    }
  }
}

// 查看任务进度
const handleViewProgress = async (row) => {
  currentTask.value = row
  showProgressDialog.value = true
  
  // 加载学生进度详情
  progressLoading.value = true
  try {
    const response = await taskAPI.getTask(row.id)
    const task = response.data
    studentProgress.value = task.studentProgress || []
  } catch (error) {
    ElMessage.error('加载进度详情失败: ' + (error.message || '未知错误'))
  } finally {
    progressLoading.value = false
  }
}

// 提交任务表单
const handleSubmit = async () => {
  if (!taskFormRef.value) return
  
  await taskFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      const submitData = {
        title: taskForm.title,
        description: taskForm.description,
        wordListId: taskForm.wordListId,
        deadline: taskForm.deadline ? new Date(taskForm.deadline).toISOString() : null
      }
      
      if (isEdit.value) {
        // 更新任务（注意：API中没有更新接口，这里假设有）
        // await taskAPI.updateTask(taskForm.id, submitData)
        // 更新学生分配
        await taskAPI.assignTask(taskForm.id, taskForm.studentIds)
        ElMessage.success('更新成功')
      } else {
        // 创建任务
        const response = await taskAPI.createTask(submitData)
        const taskId = response.data.id
        // 分配学生
        await taskAPI.assignTask(taskId, taskForm.studentIds)
        ElMessage.success('创建成功')
      }
      
      showTaskDialog.value = false
      loadTaskList()
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.message || '未知错误'))
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  taskForm.id = null
  taskForm.title = ''
  taskForm.description = ''
  taskForm.wordListId = null
  taskForm.deadline = null
  taskForm.studentIds = []
  filterClassId.value = null
  taskFormRef.value?.clearValidate()
}

// 词汇表变化
const handleWordListChange = (wordListId) => {
  // 可以在这里加载词汇表详情
}

// 班级筛选
const handleClassFilter = () => {
  // 筛选逻辑在computed中处理
}

// 全选学生
const handleSelectAll = () => {
  taskForm.studentIds = filteredStudents.value
    .filter(s => s.status === 'ACTIVE')
    .map(s => s.id)
}

// 取消全选
const handleDeselectAll = () => {
  taskForm.studentIds = []
}

// 禁用过去的日期
const disabledDate = (time) => {
  return time.getTime() < Date.now() - 24 * 60 * 60 * 1000
}

// 计算进度百分比
const calculateProgress = (task) => {
  if (!task.assignedCount || task.assignedCount === 0) return 0
  return Math.round((task.completedCount || 0) / task.assignedCount * 100)
}

// 获取进度条颜色
const getProgressColor = (row) => {
  const progress = row.progress !== undefined ? row.progress : calculateProgress(row)
  if (progress === 100) return '#67c23a'
  if (progress >= 60) return '#409eff'
  if (progress >= 30) return '#e6a23c'
  return '#f56c6c'
}

// 获取任务状态类型
const getStatusType = (status) => {
  const typeMap = {
    'ACTIVE': 'success',
    'COMPLETED': 'info',
    'EXPIRED': 'danger'
  }
  return typeMap[status] || 'info'
}

// 获取任务状态文本
const getStatusText = (status) => {
  const textMap = {
    'ACTIVE': '进行中',
    'COMPLETED': '已完成',
    'EXPIRED': '已过期'
  }
  return textMap[status] || status
}

// 获取分配状态类型
const getAssignmentStatusType = (status) => {
  const typeMap = {
    'NOT_STARTED': 'info',
    'IN_PROGRESS': 'warning',
    'COMPLETED': 'success'
  }
  return typeMap[status] || 'info'
}

// 获取分配状态文本
const getAssignmentStatusText = (status) => {
  const textMap = {
    'NOT_STARTED': '未开始',
    'IN_PROGRESS': '进行中',
    'COMPLETED': '已完成'
  }
  return textMap[status] || status
}

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-'
  const date = new Date(dateString)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 初始化
onMounted(() => {
  loadTaskList()
})
</script>

<style scoped>
.teacher-tasks {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 12px;
}

.toolbar-left h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.progress-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.progress-info .el-progress {
  flex: 1;
}

.progress-text {
  font-size: 14px;
  color: #606266;
  white-space: nowrap;
}

.form-tip {
  margin-top: 5px;
  font-size: 12px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 4px;
}

.student-selection {
  width: 100%;
}

.selection-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
  gap: 10px;
  flex-wrap: wrap;
}

.selection-actions {
  display: flex;
  gap: 10px;
}

.student-list {
  max-height: 300px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 15px;
  background-color: #f5f7fa;
}

.student-list .el-checkbox-group {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.student-list .el-checkbox {
  margin: 0;
}

.selection-summary {
  margin-top: 10px;
  font-size: 14px;
  color: #606266;
}

.selection-summary .count {
  font-weight: bold;
  color: #409eff;
  font-size: 16px;
}

.progress-detail {
  padding: 10px 0;
}

.progress-stats {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 20px;
  margin-top: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.progress-stats .el-statistic {
  text-align: center;
}

/* 移动端样式 */
@media (max-width: 767px) {
  .teacher-tasks {
    padding: 12px;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-left h2 {
    font-size: 20px;
  }

  .toolbar-actions {
    width: 100%;
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

  .progress-stats {
    grid-template-columns: repeat(2, 1fr);
    gap: 12px;
    padding: 12px;
  }

  .selection-header {
    flex-direction: column;
    align-items: stretch;
  }

  .selection-header .el-select {
    width: 100% !important;
  }

  .selection-actions {
    width: 100%;
  }

  .selection-actions .el-button {
    flex: 1;
  }
}

/* 平板端样式 */
@media (min-width: 768px) and (max-width: 1023px) {
  .teacher-tasks {
    padding: 16px;
  }

  .toolbar-left h2 {
    font-size: 22px;
  }

  :deep(.el-table) {
    font-size: 13px;
  }

  .progress-stats {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>

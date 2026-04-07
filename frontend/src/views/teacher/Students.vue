<template>
  <div class="teacher-students">
    <div class="toolbar">
      <div class="toolbar-left">
        <el-select
          v-model="selectedClassId"
          placeholder="选择班级"
          clearable
          style="width: 200px; margin-right: 10px"
          @change="handleClassChange"
        >
          <el-option label="全部班级" :value="null" />
          <el-option
            v-for="classItem in classList"
            :key="classItem.id"
            :label="classItem.name"
            :value="classItem.id"
          />
        </el-select>
      </div>
      <div class="toolbar-actions">
        <el-button type="primary" :icon="Plus" @click="handleAdd">添加学生</el-button>
      </div>
    </div>

    <el-table
      v-loading="loading"
      :data="studentList"
      style="width: 100%; margin-top: 20px"
      stripe
    >
      <el-table-column prop="username" label="学号" width="150" />
      <el-table-column prop="realName" label="姓名" width="120" />
      <el-table-column prop="email" label="邮箱" width="200" />
      <el-table-column prop="className" label="班级" width="150" />
      <el-table-column prop="status" label="状态" width="100">
        <template #default="{ row }">
          <el-tag :type="row.status === 'ACTIVE' ? 'success' : 'danger'">
            {{ row.status === 'ACTIVE' ? '启用' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="创建时间" width="180">
        <template #default="{ row }">
          {{ formatDate(row.createdAt) }}
        </template>
      </el-table-column>
      <el-table-column label="操作" width="200" fixed="right">
        <template #default="{ row }">
          <el-button type="primary" size="small" :icon="Edit" @click="handleEdit(row)">编辑</el-button>
          <el-button
            :type="row.status === 'ACTIVE' ? 'danger' : 'success'"
            size="small"
            @click="handleToggleStatus(row)"
          >
            {{ row.status === 'ACTIVE' ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <!-- 学生添加/编辑对话框 -->
    <el-dialog
      v-model="showStudentDialog"
      :title="isEdit ? '编辑学生' : '添加学生'"
      width="500px"
      @close="resetForm"
    >
      <el-form
        ref="studentFormRef"
        :model="studentForm"
        :rules="studentRules"
        label-width="100px"
      >
        <el-form-item label="学号" prop="username">
          <el-input
            v-model="studentForm.username"
            placeholder="请输入学号"
            :disabled="isEdit"
          />
        </el-form-item>
        <el-form-item label="姓名" prop="realName">
          <el-input v-model="studentForm.realName" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="studentForm.email" placeholder="请输入邮箱" />
        </el-form-item>
        <el-form-item label="班级" prop="classId">
          <el-select v-model="studentForm.classId" placeholder="请选择班级" style="width: 100%">
            <el-option
              v-for="classItem in classList"
              :key="classItem.id"
              :label="classItem.name"
              :value="classItem.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showStudentDialog = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <!-- 初始密码显示对话框 -->
    <el-dialog
      v-model="showPasswordDialog"
      title="学生账户创建成功"
      width="450px"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
    >
      <el-alert
        title="请记录学生初始密码"
        type="success"
        :closable="false"
        style="margin-bottom: 20px"
      >
        <p>学生账户已创建成功，请将以下信息告知学生：</p>
      </el-alert>
      <div class="password-info">
        <el-descriptions :column="1" border>
          <el-descriptions-item label="学号">{{ createdStudent.username }}</el-descriptions-item>
          <el-descriptions-item label="姓名">{{ createdStudent.realName }}</el-descriptions-item>
          <el-descriptions-item label="初始密码">
            <div class="password-display">
              <span class="password-text">{{ createdStudent.initialPassword }}</span>
              <el-button
                type="primary"
                size="small"
                :icon="CopyDocument"
                @click="copyPassword"
              >
                复制
              </el-button>
            </div>
          </el-descriptions-item>
        </el-descriptions>
      </div>
      <el-alert
        title="重要提示"
        type="warning"
        :closable="false"
        style="margin-top: 20px"
      >
        <p>初始密码仅显示一次，关闭后将无法再次查看，请务必记录！</p>
      </el-alert>
      <template #footer>
        <el-button type="primary" @click="handlePasswordDialogClose">我已记录</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Plus, Edit, CopyDocument } from '@element-plus/icons-vue'
import { studentAPI } from '@/api/student'

// 学生列表相关
const loading = ref(false)
const studentList = ref([])
const selectedClassId = ref(null)

// 班级列表（模拟数据，实际应从API获取）
const classList = ref([
  { id: 1, name: '一班' },
  { id: 2, name: '二班' },
  { id: 3, name: '三班' }
])

// 对话框控制
const showStudentDialog = ref(false)
const showPasswordDialog = ref(false)

// 表单相关
const studentFormRef = ref(null)
const isEdit = ref(false)
const submitting = ref(false)

// 学生表单
const studentForm = reactive({
  id: null,
  username: '',
  realName: '',
  email: '',
  classId: null
})

// 创建成功的学生信息
const createdStudent = reactive({
  username: '',
  realName: '',
  initialPassword: ''
})

// 表单验证规则
const studentRules = {
  username: [
    { required: true, message: '请输入学号', trigger: 'blur' },
    { min: 3, max: 50, message: '学号长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  realName: [
    { required: true, message: '请输入姓名', trigger: 'blur' }
  ],
  email: [
    { type: 'email', message: '请输入正确的邮箱地址', trigger: 'blur' }
  ],
  classId: [
    { required: true, message: '请选择班级', trigger: 'change' }
  ]
}

// 加载学生列表
const loadStudentList = async () => {
  loading.value = true
  try {
    const response = await studentAPI.getStudents(selectedClassId.value)
    studentList.value = response.data || []
    // 添加班级名称
    studentList.value.forEach(student => {
      const classItem = classList.value.find(c => c.id === student.classId)
      student.className = classItem ? classItem.name : '未分配'
    })
  } catch (error) {
    ElMessage.error('加载学生列表失败: ' + (error.message || '未知错误'))
  } finally {
    loading.value = false
  }
}

// 班级筛选
const handleClassChange = () => {
  loadStudentList()
}

// 添加学生
const handleAdd = () => {
  isEdit.value = false
  showStudentDialog.value = true
}

// 编辑学生
const handleEdit = (row) => {
  isEdit.value = true
  Object.assign(studentForm, {
    id: row.id,
    username: row.username,
    realName: row.realName,
    email: row.email,
    classId: row.classId
  })
  showStudentDialog.value = true
}

// 切换学生状态
const handleToggleStatus = async (row) => {
  const action = row.status === 'ACTIVE' ? '禁用' : '启用'
  const newStatus = row.status === 'ACTIVE' ? 'DISABLED' : 'ACTIVE'
  
  try {
    await ElMessageBox.confirm(
      `确定要${action}学生"${row.realName}"吗？`,
      `${action}确认`,
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    await studentAPI.toggleStudentStatus(row.id, newStatus)
    ElMessage.success(`${action}成功`)
    loadStudentList()
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(`${action}失败: ` + (error.message || '未知错误'))
    }
  }
}

// 提交学生表单
const handleSubmit = async () => {
  if (!studentFormRef.value) return
  
  await studentFormRef.value.validate(async (valid) => {
    if (!valid) return
    
    submitting.value = true
    try {
      if (isEdit.value) {
        await studentAPI.updateStudent(studentForm.id, studentForm)
        ElMessage.success('更新成功')
        showStudentDialog.value = false
        loadStudentList()
      } else {
        const response = await studentAPI.createStudent(studentForm)
        // 显示初始密码
        Object.assign(createdStudent, {
          username: response.data.username,
          realName: response.data.realName,
          initialPassword: response.data.initialPassword
        })
        showStudentDialog.value = false
        showPasswordDialog.value = true
      }
    } catch (error) {
      ElMessage.error('操作失败: ' + (error.message || '未知错误'))
    } finally {
      submitting.value = false
    }
  })
}

// 重置表单
const resetForm = () => {
  studentForm.id = null
  studentForm.username = ''
  studentForm.realName = ''
  studentForm.email = ''
  studentForm.classId = null
  studentFormRef.value?.clearValidate()
}

// 复制密码
const copyPassword = async () => {
  try {
    await navigator.clipboard.writeText(createdStudent.initialPassword)
    ElMessage.success('密码已复制到剪贴板')
  } catch (error) {
    ElMessage.error('复制失败，请手动复制')
  }
}

// 关闭密码对话框
const handlePasswordDialogClose = () => {
  showPasswordDialog.value = false
  // 清空密码信息
  createdStudent.username = ''
  createdStudent.realName = ''
  createdStudent.initialPassword = ''
  // 刷新列表
  loadStudentList()
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
  loadStudentList()
})
</script>

<style scoped>
.teacher-students {
  padding: 20px;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.toolbar-left {
  display: flex;
  align-items: center;
  flex: 1;
  min-width: 200px;
}

.toolbar-actions {
  display: flex;
  gap: 10px;
}

.password-info {
  margin: 20px 0;
}

.password-display {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
}

.password-text {
  font-size: 18px;
  font-weight: bold;
  color: #409eff;
  font-family: 'Courier New', monospace;
  letter-spacing: 2px;
}

/* 移动端样式 */
@media (max-width: 767px) {
  .teacher-students {
    padding: 12px;
  }

  .toolbar {
    flex-direction: column;
    align-items: stretch;
  }

  .toolbar-left {
    width: 100%;
  }

  .toolbar-left .el-select {
    width: 100% !important;
    margin-right: 0 !important;
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

  .password-text {
    font-size: 16px;
    word-break: break-all;
  }
}

/* 平板端样式 */
@media (min-width: 768px) and (max-width: 1023px) {
  .teacher-students {
    padding: 16px;
  }

  :deep(.el-table) {
    font-size: 13px;
  }
}
</style>

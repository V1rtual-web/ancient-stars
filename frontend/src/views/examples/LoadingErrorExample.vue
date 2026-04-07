<template>
  <div class="loading-error-example">
    <el-card>
      <template #header>
        <h2>加载状态和错误处理示例</h2>
      </template>

      <!-- 示例1: 基本加载状态 -->
      <el-divider content-position="left">示例1: 基本加载状态</el-divider>
      <div class="example-section">
        <el-button @click="loadBasicData" :loading="basicLoading">
          加载数据
        </el-button>
        <div v-loading="basicLoading" style="min-height: 100px; margin-top: 10px;">
          <p v-if="basicData">数据: {{ basicData }}</p>
        </div>
      </div>

      <!-- 示例2: 使用LoadingSpinner组件 -->
      <el-divider content-position="left">示例2: LoadingSpinner组件</el-divider>
      <div class="example-section">
        <el-button @click="showSpinner = !showSpinner">
          切换Spinner
        </el-button>
        <LoadingSpinner v-if="showSpinner" text="正在加载..." />
      </div>

      <!-- 示例3: 空状态 -->
      <el-divider content-position="left">示例3: 空状态</el-divider>
      <div class="example-section">
        <el-button @click="emptyList = []">清空列表</el-button>
        <el-button @click="emptyList = ['项目1', '项目2']">添加数据</el-button>
        <EmptyState v-if="emptyList.length === 0" description="列表为空">
          <el-button type="primary" @click="emptyList = ['新项目']">
            添加第一项
          </el-button>
        </EmptyState>
        <ul v-else>
          <li v-for="(item, index) in emptyList" :key="index">{{ item }}</li>
        </ul>
      </div>

      <!-- 示例4: 错误处理 -->
      <el-divider content-position="left">示例4: 错误处理</el-divider>
      <div class="example-section">
        <el-space wrap>
          <el-button @click="triggerNetworkError">触发网络错误</el-button>
          <el-button @click="triggerServerError">触发服务器错误</el-button>
          <el-button @click="triggerAuthError">触发权限错误</el-button>
          <el-button @click="triggerCustomError">触发自定义错误</el-button>
        </el-space>
      </div>

      <!-- 示例5: withLoading包装 -->
      <el-divider content-position="left">示例5: withLoading自动包装</el-divider>
      <div class="example-section">
        <el-button @click="loadWithWrapper" :loading="wrapperLoading">
          自动管理Loading
        </el-button>
        <p v-if="wrapperData">结果: {{ wrapperData }}</p>
      </div>

      <!-- 示例6: 多个加载状态 -->
      <el-divider content-position="left">示例6: 多个独立加载状态</el-divider>
      <div class="example-section">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-button @click="loadUsers" :loading="isLoading('users')">
              加载用户
            </el-button>
            <div v-loading="isLoading('users')" style="min-height: 80px; margin-top: 10px;">
              <p v-if="users.length > 0">用户数: {{ users.length }}</p>
            </div>
          </el-col>
          <el-col :span="12">
            <el-button @click="loadTasks" :loading="isLoading('tasks')">
              加载任务
            </el-button>
            <div v-loading="isLoading('tasks')" style="min-height: 80px; margin-top: 10px;">
              <p v-if="tasks.length > 0">任务数: {{ tasks.length }}</p>
            </div>
          </el-col>
        </el-row>
        <p v-if="isAnyLoading()" style="color: #409EFF; margin-top: 10px;">
          有数据正在加载中...
        </p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { ElMessage } from 'element-plus'
import { useLoading, useMultipleLoading } from '@/composables/useLoading'
import { handleError } from '@/utils/errorHandler'
import LoadingSpinner from '@/components/LoadingSpinner.vue'
import EmptyState from '@/components/EmptyState.vue'

// 示例1: 基本加载
const basicLoading = ref(false)
const basicData = ref(null)

const loadBasicData = async () => {
  basicLoading.value = true
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    basicData.value = '加载成功的数据'
    ElMessage.success('数据加载成功')
  } catch (error) {
    handleError(error, '加载失败')
  } finally {
    basicLoading.value = false
  }
}

// 示例2: LoadingSpinner
const showSpinner = ref(false)

// 示例3: 空状态
const emptyList = ref([])

// 示例4: 错误处理
const triggerNetworkError = () => {
  const error = new Error('Network Error')
  error.request = {}
  handleError(error, '网络请求失败')
}

const triggerServerError = () => {
  const error = new Error('Server Error')
  error.response = { status: 500 }
  handleError(error, '服务器错误')
}

const triggerAuthError = () => {
  const error = new Error('Unauthorized')
  error.response = { status: 401 }
  handleError(error, '认证失败')
}

const triggerCustomError = () => {
  handleError(new Error('这是一个自定义错误'), '操作失败')
}

// 示例5: withLoading
const { loading: wrapperLoading, withLoading } = useLoading()
const wrapperData = ref(null)

const loadWithWrapper = withLoading(async () => {
  await new Promise(resolve => setTimeout(resolve, 2000))
  wrapperData.value = '通过withLoading加载的数据'
  ElMessage.success('加载完成')
})

// 示例6: 多个加载状态
const { setLoading, isLoading, isAnyLoading } = useMultipleLoading()
const users = ref([])
const tasks = ref([])

const loadUsers = async () => {
  setLoading('users', true)
  try {
    await new Promise(resolve => setTimeout(resolve, 1500))
    users.value = ['用户1', '用户2', '用户3']
    ElMessage.success('用户加载成功')
  } catch (error) {
    handleError(error, '加载用户失败')
  } finally {
    setLoading('users', false)
  }
}

const loadTasks = async () => {
  setLoading('tasks', true)
  try {
    await new Promise(resolve => setTimeout(resolve, 2000))
    tasks.value = ['任务1', '任务2']
    ElMessage.success('任务加载成功')
  } catch (error) {
    handleError(error, '加载任务失败')
  } finally {
    setLoading('tasks', false)
  }
}
</script>

<style scoped>
.loading-error-example {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.example-section {
  padding: 20px;
  background: #f5f7fa;
  border-radius: 4px;
  margin-bottom: 20px;
}

h2 {
  margin: 0;
  font-size: 24px;
  color: #303133;
}
</style>

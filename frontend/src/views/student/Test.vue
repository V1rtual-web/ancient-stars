<template>
  <div class="student-test">
    <!-- 测试开始页面 -->
    <div v-if="currentView === 'start'" class="test-start">
      <el-card class="start-card">
        <template #header>
          <div class="card-header">
            <h2>开始测试</h2>
          </div>
        </template>
        
        <el-form :model="testConfig" label-width="120px">
          <el-form-item label="选择任务">
            <el-select 
              v-model="testConfig.taskId" 
              placeholder="请选择学习任务"
              style="width: 100%"
              @change="handleTaskChange"
            >
              <el-option
                v-for="task in tasks"
                :key="task.id"
                :label="task.title"
                :value="task.id"
              >
                <span>{{ task.title }}</span>
                <span style="float: right; color: #8492a6; font-size: 13px">
                  {{ task.wordCount }} 个单词
                </span>
              </el-option>
            </el-select>
          </el-form-item>

          <el-form-item label="题型">
            <el-select v-model="testConfig.testType" placeholder="请选择题型" style="width: 100%">
              <el-option label="选择题" value="CHOICE" />
              <el-option label="填空题" value="FILL_BLANK" />
              <el-option label="翻译题" value="TRANSLATION" />
              <el-option label="混合题型" value="MIXED" />
            </el-select>
          </el-form-item>

          <el-form-item label="题目数量">
            <el-input-number 
              v-model="testConfig.questionCount" 
              :min="5" 
              :max="maxQuestions"
              :step="5"
            />
            <span class="hint">（最多 {{ maxQuestions }} 题）</span>
          </el-form-item>

          <el-form-item label="预计时长">
            <span class="time-estimate">约 {{ estimatedTime }} 分钟</span>
          </el-form-item>
        </el-form>

        <div class="start-actions">
          <el-button type="primary" size="large" @click="startTest" :disabled="!testConfig.taskId">
            开始测试
          </el-button>
          <el-button size="large" @click="showHistory">
            查看历史成绩
          </el-button>
        </div>
      </el-card>
    </div>

    <!-- 答题页面 -->
    <div v-else-if="currentView === 'testing'" class="test-testing">
      <div class="test-header">
        <div class="test-info">
          <span class="question-progress">
            题目 {{ currentQuestionIndex + 1 }} / {{ questions.length }}
          </span>
          <el-progress 
            :percentage="Math.round(((currentQuestionIndex + 1) / questions.length) * 100)" 
            :show-text="false"
            style="width: 200px; margin: 0 20px;"
          />
        </div>
        <div class="test-timer">
          <el-icon><Timer /></el-icon>
          <span class="timer-text">{{ formattedTime }}</span>
        </div>
      </div>

      <el-card class="question-card">
        <div class="question-content">
          <h3 class="question-title">{{ currentQuestion.questionText }}</h3>
          
          <!-- 选择题 -->
          <div v-if="currentQuestion.questionType === 'CHOICE'" class="question-options">
            <el-radio-group v-model="answers[currentQuestionIndex]" class="option-group">
              <el-radio 
                v-for="(option, index) in currentQuestion.options" 
                :key="index"
                :label="option"
                class="option-item"
              >
                {{ option }}
              </el-radio>
            </el-radio-group>
          </div>

          <!-- 填空题/翻译题 -->
          <div v-else class="question-input">
            <el-input
              v-model="answers[currentQuestionIndex]"
              type="textarea"
              :rows="4"
              placeholder="请输入答案"
              maxlength="500"
              show-word-limit
            />
          </div>
        </div>

        <div class="question-actions">
          <el-button 
            @click="previousQuestion" 
            :disabled="currentQuestionIndex === 0"
          >
            上一题
          </el-button>
          <el-button 
            v-if="currentQuestionIndex < questions.length - 1"
            type="primary" 
            @click="nextQuestion"
          >
            下一题
          </el-button>
          <el-button 
            v-else
            type="success" 
            @click="confirmSubmit"
          >
            提交测试
          </el-button>
        </div>
      </el-card>

      <!-- 题目导航 -->
      <el-card class="question-nav">
        <div class="nav-title">答题卡</div>
        <div class="nav-grid">
          <div
            v-for="(question, index) in questions"
            :key="index"
            class="nav-item"
            :class="{
              'active': index === currentQuestionIndex,
              'answered': answers[index] !== null && answers[index] !== ''
            }"
            @click="jumpToQuestion(index)"
          >
            {{ index + 1 }}
          </div>
        </div>
      </el-card>
    </div>

    <!-- 测试结果页面 -->
    <div v-else-if="currentView === 'result'" class="test-result">
      <el-card class="result-card">
        <div class="result-header">
          <el-icon class="result-icon" :class="result.accuracy >= 60 ? 'success' : 'fail'">
            <component :is="result.accuracy >= 60 ? 'SuccessFilled' : 'CircleCloseFilled'" />
          </el-icon>
          <h2>测试完成</h2>
        </div>

        <div class="result-stats">
          <div class="stat-item">
            <div class="stat-value">{{ result.score }}</div>
            <div class="stat-label">得分</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ result.accuracy }}%</div>
            <div class="stat-label">正确率</div>
          </div>
          <div class="stat-item">
            <div class="stat-value">{{ result.correctAnswers }}/{{ result.totalQuestions }}</div>
            <div class="stat-label">正确题数</div>
          </div>
        </div>

        <div v-if="result.wrongQuestions && result.wrongQuestions.length > 0" class="wrong-questions">
          <h3>错题列表</h3>
          <el-table :data="result.wrongQuestions" stripe>
            <el-table-column prop="word" label="单词" width="150" />
            <el-table-column prop="questionText" label="题目" />
            <el-table-column prop="studentAnswer" label="你的答案" width="150">
              <template #default="{ row }">
                <span class="wrong-answer">{{ row.studentAnswer || '未作答' }}</span>
              </template>
            </el-table-column>
            <el-table-column prop="correctAnswer" label="正确答案" width="150">
              <template #default="{ row }">
                <span class="correct-answer">{{ row.correctAnswer }}</span>
              </template>
            </el-table-column>
          </el-table>
        </div>

        <div class="result-actions">
          <el-button type="primary" @click="backToStart">再测一次</el-button>
          <el-button @click="showHistory">查看历史成绩</el-button>
        </div>
      </el-card>
    </div>

    <!-- 历史成绩页面 -->
    <div v-else-if="currentView === 'history'" class="test-history">
      <el-card>
        <template #header>
          <div class="card-header">
            <h2>历史成绩</h2>
            <el-button @click="backToStart">返回</el-button>
          </div>
        </template>

        <el-table :data="historyList" stripe v-loading="historyLoading">
          <el-table-column prop="taskTitle" label="任务名称" />
          <el-table-column prop="testType" label="题型" width="100">
            <template #default="{ row }">
              {{ getTestTypeName(row.testType) }}
            </template>
          </el-table-column>
          <el-table-column prop="score" label="得分" width="100" />
          <el-table-column prop="accuracy" label="正确率" width="100">
            <template #default="{ row }">
              {{ row.accuracy }}%
            </template>
          </el-table-column>
          <el-table-column prop="totalQuestions" label="题目数" width="100">
            <template #default="{ row }">
              {{ row.correctAnswers }}/{{ row.totalQuestions }}
            </template>
          </el-table-column>
          <el-table-column prop="durationSeconds" label="用时" width="100">
            <template #default="{ row }">
              {{ formatDuration(row.durationSeconds) }}
            </template>
          </el-table-column>
          <el-table-column prop="createdAt" label="测试时间" width="180">
            <template #default="{ row }">
              {{ formatDate(row.createdAt) }}
            </template>
          </el-table-column>
          <el-table-column label="操作" width="100">
            <template #default="{ row }">
              <el-button link type="primary" @click="viewTestDetail(row.id)">
                查看详情
              </el-button>
            </template>
          </el-table-column>
        </el-table>

        <el-pagination
          v-if="historyTotal > 0"
          v-model:current-page="historyPage"
          v-model:page-size="historySize"
          :total="historyTotal"
          layout="total, prev, pager, next"
          @current-change="loadHistory"
          style="margin-top: 20px; justify-content: center;"
        />
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Timer, SuccessFilled, CircleCloseFilled } from '@element-plus/icons-vue'
import { learningAPI } from '@/api/learning'
import { taskAPI } from '@/api/task'
import { useUserStore } from '@/store/modules/user'

const userStore = useUserStore()

// 当前视图：start, testing, result, history
const currentView = ref('start')

// 测试配置
const testConfig = ref({
  taskId: null,
  testType: 'CHOICE',
  questionCount: 10
})

// 任务列表
const tasks = ref([])
const maxQuestions = ref(50)

// 题目数据
const questions = ref([])
const answers = ref([])
const currentQuestionIndex = ref(0)

// 计时器
const startTime = ref(null)
const elapsedSeconds = ref(0)
let timerInterval = null

// 测试结果
const result = ref({})

// 历史成绩
const historyList = ref([])
const historyLoading = ref(false)
const historyPage = ref(1)
const historySize = ref(10)
const historyTotal = ref(0)

// 计算属性
const currentQuestion = computed(() => questions.value[currentQuestionIndex.value] || {})

const estimatedTime = computed(() => {
  const baseTime = testConfig.value.questionCount * 1.5 // 每题1.5分钟
  return Math.ceil(baseTime)
})

const formattedTime = computed(() => {
  const minutes = Math.floor(elapsedSeconds.value / 60)
  const seconds = elapsedSeconds.value % 60
  return `${String(minutes).padStart(2, '0')}:${String(seconds).padStart(2, '0')}`
})

// 加载学生任务列表
const loadTasks = async () => {
  try {
    const userId = userStore.userInfo.id
    const response = await taskAPI.getStudentTasks(userId)
    tasks.value = response.data.filter(task => task.status !== 'COMPLETED')
  } catch (error) {
    ElMessage.error('加载任务列表失败')
  }
}

// 任务变化时更新最大题目数
const handleTaskChange = () => {
  const selectedTask = tasks.value.find(t => t.id === testConfig.value.taskId)
  if (selectedTask) {
    maxQuestions.value = Math.min(selectedTask.wordCount || 50, 50)
    if (testConfig.value.questionCount > maxQuestions.value) {
      testConfig.value.questionCount = maxQuestions.value
    }
  }
}

// 开始测试
const startTest = async () => {
  try {
    const response = await learningAPI.generateTest({
      taskId: testConfig.value.taskId,
      questionCount: testConfig.value.questionCount,
      testType: testConfig.value.testType
    })
    
    questions.value = response.data
    answers.value = new Array(questions.value.length).fill('')
    currentQuestionIndex.value = 0
    
    // 开始计时
    startTime.value = Date.now()
    elapsedSeconds.value = 0
    timerInterval = setInterval(() => {
      elapsedSeconds.value = Math.floor((Date.now() - startTime.value) / 1000)
    }, 1000)
    
    currentView.value = 'testing'
  } catch (error) {
    ElMessage.error('生成测试失败：' + (error.response?.data?.message || error.message))
  }
}

// 上一题
const previousQuestion = () => {
  if (currentQuestionIndex.value > 0) {
    currentQuestionIndex.value--
  }
}

// 下一题
const nextQuestion = () => {
  if (currentQuestionIndex.value < questions.value.length - 1) {
    currentQuestionIndex.value++
  }
}

// 跳转到指定题目
const jumpToQuestion = (index) => {
  currentQuestionIndex.value = index
}

// 确认提交
const confirmSubmit = async () => {
  const unansweredCount = answers.value.filter(a => !a || a.trim() === '').length
  
  if (unansweredCount > 0) {
    try {
      await ElMessageBox.confirm(
        `还有 ${unansweredCount} 题未作答，确定要提交吗？`,
        '提交确认',
        {
          confirmButtonText: '确定提交',
          cancelButtonText: '继续答题',
          type: 'warning'
        }
      )
      submitTest()
    } catch {
      // 用户取消
    }
  } else {
    submitTest()
  }
}

// 提交测试
const submitTest = async () => {
  try {
    // 停止计时
    if (timerInterval) {
      clearInterval(timerInterval)
      timerInterval = null
    }
    
    const userId = userStore.userInfo.id
    const submitData = {
      studentId: userId,
      taskId: testConfig.value.taskId,
      answers: questions.value.map((q, index) => ({
        vocabularyId: q.vocabularyId,
        answer: answers.value[index] || ''
      })),
      durationSeconds: elapsedSeconds.value
    }
    
    const response = await learningAPI.submitTest(submitData)
    result.value = response.data
    currentView.value = 'result'
    
    ElMessage.success('测试提交成功')
  } catch (error) {
    ElMessage.error('提交测试失败：' + (error.response?.data?.message || error.message))
  }
}

// 返回开始页面
const backToStart = () => {
  currentView.value = 'start'
  testConfig.value = {
    taskId: null,
    testType: 'CHOICE',
    questionCount: 10
  }
  questions.value = []
  answers.value = []
  result.value = {}
  
  if (timerInterval) {
    clearInterval(timerInterval)
    timerInterval = null
  }
}

// 显示历史成绩
const showHistory = () => {
  currentView.value = 'history'
  historyPage.value = 1
  loadHistory()
}

// 加载历史成绩
const loadHistory = async () => {
  try {
    historyLoading.value = true
    const userId = userStore.userInfo.id
    const response = await learningAPI.getTestHistory(userId, historyPage.value - 1, historySize.value)
    historyList.value = response.data.content
    historyTotal.value = response.data.totalElements
  } catch (error) {
    ElMessage.error('加载历史成绩失败')
  } finally {
    historyLoading.value = false
  }
}

// 查看测试详情
const viewTestDetail = async (testId) => {
  try {
    const response = await learningAPI.getTestDetail(testId)
    result.value = response.data
    currentView.value = 'result'
  } catch (error) {
    ElMessage.error('加载测试详情失败')
  }
}

// 格式化题型名称
const getTestTypeName = (type) => {
  const typeMap = {
    CHOICE: '选择题',
    FILL_BLANK: '填空题',
    TRANSLATION: '翻译题',
    MIXED: '混合题型'
  }
  return typeMap[type] || type
}

// 格式化时长
const formatDuration = (seconds) => {
  if (!seconds) return '-'
  const minutes = Math.floor(seconds / 60)
  const secs = seconds % 60
  return `${minutes}分${secs}秒`
}

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '-'
  const date = new Date(dateStr)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  })
}

// 组件挂载
onMounted(() => {
  loadTasks()
})

// 组件卸载
onUnmounted(() => {
  if (timerInterval) {
    clearInterval(timerInterval)
  }
})
</script>

<style scoped>
.student-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 测试开始页面 */
.test-start {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: 500px;
}

.start-card {
  width: 600px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.card-header h2 {
  margin: 0;
  font-size: 24px;
}

.hint {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.time-estimate {
  font-size: 16px;
  color: #409eff;
  font-weight: bold;
}

.start-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

/* 答题页面 */
.test-testing {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.test-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px;
  background: white;
  border-radius: 4px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.test-info {
  display: flex;
  align-items: center;
}

.question-progress {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.test-timer {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 24px;
  font-weight: bold;
  color: #409eff;
}

.timer-text {
  font-family: 'Courier New', monospace;
}

.question-card {
  min-height: 400px;
}

.question-content {
  padding: 20px 0;
}

.question-title {
  font-size: 20px;
  margin-bottom: 30px;
  color: #303133;
  line-height: 1.6;
}

.question-options {
  margin: 20px 0;
}

.option-group {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.option-item {
  padding: 15px;
  border: 2px solid #dcdfe6;
  border-radius: 4px;
  transition: all 0.3s;
}

.option-item:hover {
  border-color: #409eff;
  background-color: #ecf5ff;
}

.question-input {
  margin: 20px 0;
}

.question-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.question-nav {
  position: sticky;
  top: 20px;
}

.nav-title {
  font-size: 16px;
  font-weight: bold;
  margin-bottom: 15px;
  color: #303133;
}

.nav-grid {
  display: grid;
  grid-template-columns: repeat(10, 1fr);
  gap: 10px;
}

.nav-item {
  width: 40px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 2px solid #dcdfe6;
  border-radius: 4px;
  cursor: pointer;
  transition: all 0.3s;
  font-weight: bold;
}

.nav-item:hover {
  border-color: #409eff;
  color: #409eff;
}

.nav-item.active {
  background-color: #409eff;
  color: white;
  border-color: #409eff;
}

.nav-item.answered {
  background-color: #67c23a;
  color: white;
  border-color: #67c23a;
}

.nav-item.answered:hover {
  background-color: #85ce61;
}

/* 测试结果页面 */
.test-result {
  display: flex;
  justify-content: center;
  padding: 20px 0;
}

.result-card {
  width: 100%;
  max-width: 1000px;
}

.result-header {
  text-align: center;
  padding: 30px 0;
}

.result-icon {
  font-size: 80px;
  margin-bottom: 20px;
}

.result-icon.success {
  color: #67c23a;
}

.result-icon.fail {
  color: #f56c6c;
}

.result-header h2 {
  font-size: 28px;
  margin: 0;
}

.result-stats {
  display: flex;
  justify-content: space-around;
  padding: 40px 0;
  border-bottom: 1px solid #ebeef5;
}

.stat-item {
  text-align: center;
}

.stat-value {
  font-size: 36px;
  font-weight: bold;
  color: #409eff;
  margin-bottom: 10px;
}

.stat-label {
  font-size: 16px;
  color: #909399;
}

.wrong-questions {
  margin-top: 30px;
}

.wrong-questions h3 {
  font-size: 20px;
  margin-bottom: 20px;
  color: #303133;
}

.wrong-answer {
  color: #f56c6c;
}

.correct-answer {
  color: #67c23a;
  font-weight: bold;
}

.result-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 30px;
}

/* 历史成绩页面 */
.test-history {
  /* 样式继承自card */
}

/* 响应式设计 */
@media (max-width: 767px) {
  .student-test {
    padding: 12px;
  }

  .start-card {
    width: 100%;
  }

  .card-header h2 {
    font-size: 20px;
  }

  .start-actions {
    flex-direction: column;
    width: 100%;
  }

  .start-actions .el-button {
    width: 100%;
  }

  .test-header {
    flex-direction: column;
    gap: 15px;
    padding: 15px;
  }

  .test-info {
    flex-direction: column;
    gap: 10px;
    width: 100%;
  }

  .test-info .el-progress {
    width: 100% !important;
  }

  .test-timer {
    font-size: 20px;
  }

  .question-title {
    font-size: 18px;
  }

  .question-actions {
    flex-direction: column;
  }

  .question-actions .el-button {
    width: 100%;
  }

  .nav-grid {
    grid-template-columns: repeat(5, 1fr);
    gap: 8px;
  }

  .nav-item {
    width: 100%;
    height: 36px;
  }

  .result-stats {
    flex-direction: column;
    gap: 20px;
    padding: 20px 0;
  }

  .stat-value {
    font-size: 28px;
  }

  .result-actions {
    flex-direction: column;
  }

  .result-actions .el-button {
    width: 100%;
  }

  :deep(.el-table) {
    font-size: 12px;
  }

  :deep(.el-table th),
  :deep(.el-table td) {
    padding: 8px 4px;
  }
}

/* 平板端样式 */
@media (min-width: 768px) and (max-width: 1023px) {
  .student-test {
    padding: 16px;
  }

  .start-card {
    width: 90%;
  }

  .nav-grid {
    grid-template-columns: repeat(8, 1fr);
  }

  .result-stats {
    padding: 30px 0;
  }
}
</style>

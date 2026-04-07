<template>
  <div class="student-learn">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <el-button @click="goBack" icon="ArrowLeft">返回任务列表</el-button>
      <div class="mode-switch">
        <span class="mode-label">学习模式：</span>
        <el-switch
          v-model="isMemoryMode"
          active-text="记忆模式"
          inactive-text="浏览模式"
          @change="handleModeChange"
        />
      </div>
    </div>

    <!-- 加载状态 -->
    <div v-if="loading" class="loading-container">
      <el-icon class="is-loading" :size="40"><Loading /></el-icon>
      <p>加载中...</p>
    </div>

    <!-- 词汇学习区域 -->
    <div v-else-if="vocabularies.length > 0" class="learn-container">
      <!-- 进度信息 -->
      <div class="progress-info">
        <div class="progress-text">
          <span>学习进度：{{ currentIndex + 1 }} / {{ vocabularies.length }}</span>
          <span class="mastered-count">已掌握：{{ masteredCount }} / {{ vocabularies.length }}</span>
        </div>
        <el-progress 
          :percentage="progressPercentage" 
          :color="progressColor"
          :stroke-width="8"
        />
      </div>

      <!-- 词汇卡片 -->
      <transition name="card-fade" mode="out-in">
        <el-card :key="currentVocabulary.id" class="vocabulary-card" shadow="hover">
          <!-- 单词和音标 -->
          <div class="word-section">
            <h1 class="word">{{ currentVocabulary.word }}</h1>
            <p v-if="currentVocabulary.phonetic" class="phonetic">{{ currentVocabulary.phonetic }}</p>
          </div>

          <!-- 记忆模式：显示答案按钮 -->
          <div v-if="isMemoryMode && !showAnswer" class="show-answer-section">
            <el-button type="primary" size="large" @click="showAnswer = true">
              显示答案
            </el-button>
            <p class="hint">先尝试回忆这个单词的意思</p>
          </div>

          <!-- 浏览模式或已显示答案：显示释义和例句 -->
          <div v-if="!isMemoryMode || showAnswer" class="content-section">
            <div class="translation-section">
              <h3>释义</h3>
              <p class="translation">{{ currentVocabulary.translation }}</p>
            </div>

            <div v-if="currentVocabulary.exampleSentence" class="example-section">
              <h3>例句</h3>
              <p class="example">{{ currentVocabulary.exampleSentence }}</p>
            </div>
          </div>

          <!-- 掌握度标记按钮 -->
          <div class="mastery-buttons">
            <el-button 
              :type="currentMastery === 'UNKNOWN' ? 'info' : 'default'"
              @click="markMastery('UNKNOWN')"
              :loading="markingMastery"
            >
              未学习
            </el-button>
            <el-button 
              :type="currentMastery === 'LEARNING' ? 'warning' : 'default'"
              @click="markMastery('LEARNING')"
              :loading="markingMastery"
            >
              学习中
            </el-button>
            <el-button 
              :type="currentMastery === 'FAMILIAR' ? 'primary' : 'default'"
              @click="markMastery('FAMILIAR')"
              :loading="markingMastery"
            >
              熟悉
            </el-button>
            <el-button 
              :type="currentMastery === 'MASTERED' ? 'success' : 'default'"
              @click="markMastery('MASTERED')"
              :loading="markingMastery"
            >
              已掌握
            </el-button>
          </div>
        </el-card>
      </transition>

      <!-- 导航按钮 -->
      <div class="navigation-buttons">
        <el-button 
          size="large" 
          @click="previousVocabulary" 
          :disabled="currentIndex === 0"
          icon="ArrowLeft"
        >
          上一个
        </el-button>
        <span class="nav-info">{{ currentIndex + 1 }} / {{ vocabularies.length }}</span>
        <el-button 
          size="large" 
          @click="nextVocabulary" 
          :disabled="currentIndex === vocabularies.length - 1"
        >
          下一个
          <el-icon class="el-icon--right"><ArrowRight /></el-icon>
        </el-button>
      </div>
    </div>

    <!-- 空状态 -->
    <el-empty v-else description="该任务暂无词汇" />

    <!-- 完成提示对话框 -->
    <el-dialog v-model="showCompletionDialog" title="恭喜完成！" width="400px" center>
      <div class="completion-content">
        <el-icon :size="60" color="#67C23A"><CircleCheck /></el-icon>
        <p>您已完成所有词汇的学习！</p>
        <p class="stats">已掌握：{{ masteredCount }} / {{ vocabularies.length }}</p>
      </div>
      <template #footer>
        <el-button @click="showCompletionDialog = false">继续复习</el-button>
        <el-button type="primary" @click="goBack">返回任务列表</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '../../store/modules/user'
import { taskAPI } from '../../api/task'
import { learningAPI } from '../../api/learning'
import { ElMessage } from 'element-plus'
import { ArrowLeft, ArrowRight, Loading, CircleCheck } from '@element-plus/icons-vue'
import request from '../../utils/request'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

// 数据状态
const loading = ref(true)
const taskId = ref(route.params.taskId)
const vocabularies = ref([])
const currentIndex = ref(0)
const isMemoryMode = ref(false)
const showAnswer = ref(false)
const markingMastery = ref(false)
const learningRecords = ref({}) // 存储学习记录，key为vocabularyId
const showCompletionDialog = ref(false)

// 当前词汇
const currentVocabulary = computed(() => {
  return vocabularies.value[currentIndex.value] || {}
})

// 当前词汇的掌握度
const currentMastery = computed(() => {
  const record = learningRecords.value[currentVocabulary.value.id]
  return record?.masteryLevel || 'UNKNOWN'
})

// 已掌握词汇数
const masteredCount = computed(() => {
  return Object.values(learningRecords.value).filter(
    record => record.masteryLevel === 'MASTERED'
  ).length
})

// 进度百分比
const progressPercentage = computed(() => {
  if (vocabularies.value.length === 0) return 0
  return Math.round((masteredCount.value / vocabularies.value.length) * 100)
})

// 进度条颜色
const progressColor = computed(() => {
  const percentage = progressPercentage.value
  if (percentage < 30) return '#F56C6C'
  if (percentage < 70) return '#E6A23C'
  return '#67C23A'
})

// 加载任务词汇
const loadTaskVocabularies = async () => {
  try {
    loading.value = true
    
    // 获取任务详情
    const taskResponse = await taskAPI.getTask(taskId.value)
    if (!taskResponse.data || !taskResponse.data.wordListId) {
      ElMessage.error('任务数据异常')
      return
    }
    
    const wordListId = taskResponse.data.wordListId
    
    // 获取词汇表详情（包含词汇列表）
    const wordListResponse = await request.get(`/wordlist/${wordListId}/detail`)
    if (wordListResponse.data && wordListResponse.data.vocabularies) {
      vocabularies.value = wordListResponse.data.vocabularies
    }
    
    // 加载学习记录
    await loadLearningRecords()
    
  } catch (error) {
    console.error('加载词汇失败:', error)
    ElMessage.error('加载词汇失败')
  } finally {
    loading.value = false
  }
}

// 加载学习记录
const loadLearningRecords = async () => {
  try {
    const studentId = userStore.userInfo.id
    const response = await request.get(`/api/learning/student/${studentId}/task/${taskId.value}`)
    
    if (response.data) {
      // 将学习记录转换为以vocabularyId为key的对象
      const records = {}
      response.data.forEach(record => {
        records[record.vocabularyId] = record
      })
      learningRecords.value = records
    }
  } catch (error) {
    console.error('加载学习记录失败:', error)
  }
}

// 标记掌握度
const markMastery = async (masteryLevel) => {
  try {
    markingMastery.value = true
    
    const studentId = userStore.userInfo.id
    const vocabularyId = currentVocabulary.value.id
    
    await request.post('/api/learning/mark-mastery', {
      studentId,
      vocabularyId,
      taskId: taskId.value,
      masteryLevel
    })
    
    // 更新本地学习记录
    learningRecords.value[vocabularyId] = {
      ...learningRecords.value[vocabularyId],
      vocabularyId,
      masteryLevel
    }
    
    ElMessage.success('标记成功')
    
    // 自动跳转到下一个词汇
    setTimeout(() => {
      if (currentIndex.value < vocabularies.value.length - 1) {
        nextVocabulary()
      } else {
        // 已经是最后一个词汇，显示完成提示
        showCompletionDialog.value = true
      }
    }, 500)
    
  } catch (error) {
    console.error('标记掌握度失败:', error)
    ElMessage.error('标记失败')
  } finally {
    markingMastery.value = false
  }
}

// 上一个词汇
const previousVocabulary = () => {
  if (currentIndex.value > 0) {
    currentIndex.value--
    resetAnswerState()
  }
}

// 下一个词汇
const nextVocabulary = () => {
  if (currentIndex.value < vocabularies.value.length - 1) {
    currentIndex.value++
    resetAnswerState()
  }
}

// 重置答案显示状态
const resetAnswerState = () => {
  if (isMemoryMode.value) {
    showAnswer.value = false
  }
}

// 模式切换
const handleModeChange = () => {
  resetAnswerState()
}

// 返回任务列表
const goBack = () => {
  router.push('/student/tasks')
}

// 键盘快捷键
const handleKeyPress = (event) => {
  if (event.key === 'ArrowLeft') {
    previousVocabulary()
  } else if (event.key === 'ArrowRight') {
    nextVocabulary()
  } else if (event.key === ' ' && isMemoryMode.value && !showAnswer.value) {
    event.preventDefault()
    showAnswer.value = true
  }
}

// 生命周期
onMounted(() => {
  loadTaskVocabularies()
  window.addEventListener('keydown', handleKeyPress)
})

onUnmounted(() => {
  window.removeEventListener('keydown', handleKeyPress)
})
</script>

<style scoped>
.student-learn {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

/* 工具栏 */
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
  padding: 15px 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.mode-switch {
  display: flex;
  align-items: center;
  gap: 10px;
}

.mode-label {
  font-weight: 500;
  color: #606266;
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

/* 学习容器 */
.learn-container {
  display: flex;
  flex-direction: column;
  gap: 30px;
}

/* 进度信息 */
.progress-info {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.progress-text {
  display: flex;
  justify-content: space-between;
  margin-bottom: 15px;
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.mastered-count {
  color: #67C23A;
}

/* 词汇卡片 */
.vocabulary-card {
  min-height: 500px;
  display: flex;
  flex-direction: column;
  justify-content: space-between;
}

.vocabulary-card :deep(.el-card__body) {
  padding: 40px;
  display: flex;
  flex-direction: column;
  gap: 30px;
  min-height: 500px;
}

/* 单词区域 */
.word-section {
  text-align: center;
  padding: 20px 0;
  border-bottom: 2px solid #EBEEF5;
}

.word {
  font-size: 48px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 15px 0;
}

.phonetic {
  font-size: 24px;
  color: #909399;
  font-style: italic;
  margin: 0;
}

/* 显示答案区域 */
.show-answer-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 15px;
}

.show-answer-section .el-button {
  padding: 15px 40px;
  font-size: 18px;
}

.hint {
  color: #909399;
  font-size: 14px;
  margin: 0;
}

/* 内容区域 */
.content-section {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 25px;
}

.translation-section,
.example-section {
  padding: 20px;
  background: #F5F7FA;
  border-radius: 8px;
}

.translation-section h3,
.example-section h3 {
  font-size: 16px;
  color: #606266;
  margin: 0 0 12px 0;
  font-weight: 600;
}

.translation {
  font-size: 20px;
  color: #303133;
  line-height: 1.6;
  margin: 0;
}

.example {
  font-size: 16px;
  color: #606266;
  line-height: 1.8;
  margin: 0;
  font-style: italic;
}

/* 掌握度按钮 */
.mastery-buttons {
  display: flex;
  gap: 15px;
  justify-content: center;
  padding-top: 20px;
  border-top: 2px solid #EBEEF5;
}

.mastery-buttons .el-button {
  flex: 1;
  max-width: 150px;
  height: 45px;
  font-size: 16px;
}

/* 导航按钮 */
.navigation-buttons {
  display: flex;
  justify-content: center;
  align-items: center;
  gap: 30px;
  padding: 20px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

.nav-info {
  font-size: 18px;
  font-weight: 500;
  color: #606266;
  min-width: 100px;
  text-align: center;
}

/* 卡片切换动画 */
.card-fade-enter-active,
.card-fade-leave-active {
  transition: all 0.3s ease;
}

.card-fade-enter-from {
  opacity: 0;
  transform: translateX(30px);
}

.card-fade-leave-to {
  opacity: 0;
  transform: translateX(-30px);
}

/* 完成对话框 */
.completion-content {
  text-align: center;
  padding: 20px;
}

.completion-content p {
  margin: 15px 0;
  font-size: 18px;
  color: #303133;
}

.completion-content .stats {
  font-size: 20px;
  font-weight: bold;
  color: #67C23A;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .student-learn {
    padding: 10px;
  }

  .toolbar {
    flex-direction: column;
    gap: 15px;
    align-items: stretch;
  }

  .word {
    font-size: 36px;
  }

  .phonetic {
    font-size: 20px;
  }

  .vocabulary-card :deep(.el-card__body) {
    padding: 20px;
    min-height: 400px;
  }

  .mastery-buttons {
    flex-wrap: wrap;
  }

  .mastery-buttons .el-button {
    flex: 1 1 calc(50% - 10px);
    max-width: none;
  }

  .navigation-buttons {
    gap: 15px;
  }
}
</style>

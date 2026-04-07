<template>
  <div class="demo-page">
    <h1>StudyCard 组件演示</h1>
    
    <StudyCard
      :vocabularies="mockVocabularies"
      @learned="handleLearned"
      @index-change="handleIndexChange"
    />

    <div class="demo-info">
      <h3>事件日志</h3>
      <ul>
        <li v-for="(log, index) in eventLogs" :key="index">
          {{ log }}
        </li>
      </ul>
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import StudyCard from '../../components/StudyCard.vue'

// 模拟词汇数据
const mockVocabularies = ref([
  {
    id: 1,
    word: 'apple',
    phonetic: '/ˈæpl/',
    translation: 'n. 苹果',
    example: 'I like apples.'
  },
  {
    id: 2,
    word: 'banana',
    phonetic: '/bəˈnɑːnə/',
    translation: 'n. 香蕉',
    example: 'Bananas are yellow.'
  },
  {
    id: 3,
    word: 'computer',
    phonetic: '/kəmˈpjuːtər/',
    translation: 'n. 计算机；电脑',
    example: 'I use a computer every day.'
  },
  {
    id: 4,
    word: 'beautiful',
    phonetic: '/ˈbjuːtɪfl/',
    translation: 'adj. 美丽的；漂亮的',
    example: 'She is a beautiful girl.'
  },
  {
    id: 5,
    word: 'education',
    phonetic: '/ˌedʒuˈkeɪʃn/',
    translation: 'n. 教育；培养',
    example: 'Education is very important.'
  }
])

// 事件日志
const eventLogs = ref([])

// 处理已掌握事件
const handleLearned = (vocabularyId) => {
  const timestamp = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${timestamp}] 已掌握词汇 ID: ${vocabularyId}`)
  
  // 限制日志数量
  if (eventLogs.value.length > 10) {
    eventLogs.value.pop()
  }
}

// 处理索引变化事件
const handleIndexChange = (newIndex) => {
  const timestamp = new Date().toLocaleTimeString()
  eventLogs.value.unshift(`[${timestamp}] 切换到索引: ${newIndex}`)
  
  // 限制日志数量
  if (eventLogs.value.length > 10) {
    eventLogs.value.pop()
  }
}
</script>

<style scoped>
.demo-page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.demo-page h1 {
  text-align: center;
  margin-bottom: 30px;
  color: #303133;
}

.demo-info {
  margin-top: 40px;
  padding: 20px;
  background: #f5f7fa;
  border-radius: 8px;
}

.demo-info h3 {
  margin: 0 0 15px 0;
  color: #606266;
}

.demo-info ul {
  list-style: none;
  padding: 0;
  margin: 0;
}

.demo-info li {
  padding: 8px 12px;
  margin-bottom: 8px;
  background: #fff;
  border-radius: 4px;
  font-size: 14px;
  color: #606266;
  border-left: 3px solid #409EFF;
}
</style>

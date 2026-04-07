<template>
  <div class="vocabulary-list-component">
    <!-- 词汇网格 -->
    <div v-if="vocabularies.length > 0" class="vocabulary-grid">
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
            @click="handleMarkAsLearned(vocab)"
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

    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        :current-page="currentPage"
        :page-size="pageSize"
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
import { ref } from 'vue'
import { Check } from '@element-plus/icons-vue'

// Props
const props = defineProps({
  vocabularies: {
    type: Array,
    required: true,
    default: () => []
  },
  total: {
    type: Number,
    default: 0
  },
  currentPage: {
    type: Number,
    default: 1
  },
  pageSize: {
    type: Number,
    default: 20
  },
  loading: {
    type: Boolean,
    default: false
  }
})

// Emits
const emit = defineEmits(['learned', 'pageChange', 'sizeChange'])

// 状态
const markingId = ref(null)

// 标记已掌握
const handleMarkAsLearned = async (vocab) => {
  markingId.value = vocab.id
  emit('learned', vocab.id)
  
  // 延迟重置状态，等待父组件处理
  setTimeout(() => {
    markingId.value = null
  }, 1000)
}

// 处理页码变化
const handlePageChange = (page) => {
  emit('pageChange', page)
}

// 处理每页数量变化
const handleSizeChange = (size) => {
  emit('sizeChange', size)
}
</script>

<style scoped>
.vocabulary-list-component {
  width: 100%;
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

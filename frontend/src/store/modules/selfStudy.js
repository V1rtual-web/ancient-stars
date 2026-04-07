import { defineStore } from 'pinia'
import { selfStudyAPI } from '../../api/selfStudy'

export const useSelfStudyStore = defineStore('selfStudy', {
  state: () => ({
    // 词汇列表
    vocabularies: [],
    currentIndex: 0,
    total: 0,
    
    // 加载状态
    loading: false,
    
    // 筛选和分页
    filters: {
      wordListId: null,
      keyword: ''
    },
    pagination: {
      page: 1,
      size: 20
    },
    
    // 复习提醒
    reviewReminders: [],
    
    // 学习进度
    progress: null
  }),

  getters: {
    // 当前词汇
    currentVocabulary: (state) => {
      return state.vocabularies[state.currentIndex] || null
    },
    
    // 是否有下一个词汇
    hasNext: (state) => {
      return state.currentIndex < state.vocabularies.length - 1
    },
    
    // 是否有上一个词汇
    hasPrevious: (state) => {
      return state.currentIndex > 0
    },
    
    // 学习进度百分比
    progressPercentage: (state) => {
      if (state.vocabularies.length === 0) return 0
      return Math.round((state.currentIndex / state.vocabularies.length) * 100)
    }
  },

  actions: {
    // 加载词汇列表
    async loadVocabularies(params = {}) {
      try {
        this.loading = true
        
        const requestParams = {
          page: params.page || this.pagination.page,
          size: params.size || this.pagination.size
        }
        
        if (params.wordListId !== undefined) {
          this.filters.wordListId = params.wordListId
        }
        if (this.filters.wordListId) {
          requestParams.wordListId = this.filters.wordListId
        }
        
        if (params.keyword !== undefined) {
          this.filters.keyword = params.keyword
        }
        if (this.filters.keyword) {
          requestParams.keyword = this.filters.keyword
        }
        
        const response = await selfStudyAPI.getVocabularies(requestParams)
        
        if (response.data) {
          this.vocabularies = response.data.items || []
          this.total = response.data.total || 0
          this.currentIndex = 0 // 重置索引
          
          // 更新分页信息
          if (params.page !== undefined) {
            this.pagination.page = params.page
          }
          if (params.size !== undefined) {
            this.pagination.size = params.size
          }
        }
        
        return { success: true, data: response.data }
      } catch (error) {
        console.error('加载词汇失败:', error)
        return { success: false, message: error.message || '加载词汇失败' }
      } finally {
        this.loading = false
      }
    },

    // 标记已掌握
    async markAsLearned(vocabularyId) {
      try {
        await selfStudyAPI.createStudyRecord(vocabularyId)
        
        // 更新本地状态
        const vocab = this.vocabularies.find(v => v.id === vocabularyId)
        if (vocab) {
          vocab.isLearned = true
        }
        
        return { success: true }
      } catch (error) {
        console.error('标记失败:', error)
        return { success: false, message: error.message || '标记失败' }
      }
    },

    // 切换到下一个词汇
    nextVocabulary() {
      if (this.hasNext) {
        this.currentIndex++
        return true
      }
      return false
    },

    // 切换到上一个词汇
    previousVocabulary() {
      if (this.hasPrevious) {
        this.currentIndex--
        return true
      }
      return false
    },

    // 设置当前索引
    setCurrentIndex(index) {
      if (index >= 0 && index < this.vocabularies.length) {
        this.currentIndex = index
      }
    },

    // 加载复习提醒
    async loadReviewReminders() {
      try {
        const response = await selfStudyAPI.getReviewReminders()
        if (response.data) {
          this.reviewReminders = response.data
        }
        return { success: true, data: response.data }
      } catch (error) {
        console.error('加载复习提醒失败:', error)
        return { success: false, message: error.message || '加载复习提醒失败' }
      }
    },

    // 完成复习
    async completeReview(id) {
      try {
        await selfStudyAPI.completeReview(id)
        
        // 从列表中移除已完成的复习
        this.reviewReminders = this.reviewReminders.filter(r => r.id !== id)
        
        return { success: true }
      } catch (error) {
        console.error('完成复习失败:', error)
        return { success: false, message: error.message || '完成复习失败' }
      }
    },

    // 加载学习进度
    async loadProgress(params = {}) {
      try {
        const response = await selfStudyAPI.getProgress(params)
        if (response.data) {
          this.progress = response.data
        }
        return { success: true, data: response.data }
      } catch (error) {
        console.error('加载学习进度失败:', error)
        return { success: false, message: error.message || '加载学习进度失败' }
      }
    },

    // 重置筛选条件
    resetFilters() {
      this.filters = {
        wordListId: null,
        keyword: ''
      }
      this.pagination.page = 1
    },

    // 重置状态
    reset() {
      this.vocabularies = []
      this.currentIndex = 0
      this.total = 0
      this.loading = false
      this.resetFilters()
      this.reviewReminders = []
      this.progress = null
    }
  }
})

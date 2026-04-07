import request from '../utils/request'

export const selfStudyAPI = {
  // 获取词汇列表（支持分页、筛选、搜索）
  getVocabularies(params) {
    return request.get('/api/student/vocabularies', { params })
  },

  // 创建学习记录
  createStudyRecord(vocabularyId) {
    return request.post('/api/student/study-records', { vocabularyId })
  },

  // 获取待复习词汇列表
  getReviewReminders() {
    return request.get('/api/student/review-reminders')
  },

  // 完成复习
  completeReview(id) {
    return request.post(`/api/student/review-reminders/${id}/complete`)
  },

  // 获取学习进度统计
  getProgress(params) {
    return request.get('/api/student/progress', { params })
  }
}

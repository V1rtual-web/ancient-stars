import request from '../utils/request'

export const learningAPI = {
  // 记录学习进度
  recordLearning(data) {
    return request.post('/learning/record', data)
  },

  // 标记词汇掌握度
  markMastery(data) {
    return request.post('/learning/mastery', data)
  },

  // 获取学习历史
  getLearningHistory(studentId) {
    return request.get(`/learning/history/${studentId}`)
  },

  // 生成测试
  generateTest(data) {
    return request.post('/test/generate', data)
  },

  // 提交测试
  submitTest(data) {
    return request.post('/test/submit', data)
  },

  // 获取测试历史
  getTestHistory(studentId, page = 0, size = 10) {
    return request.get(`/test/history/${studentId}`, {
      params: { page, size }
    })
  },

  // 获取测试详情
  getTestDetail(testId) {
    return request.get(`/test/${testId}/details`)
  }
}

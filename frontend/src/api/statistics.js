import request from '../utils/request'

export const statisticsAPI = {
  // 获取学生个人统计
  getStudentStatistics(studentId, startDate, endDate) {
    return request.get(`/statistics/student/${studentId}`, {
      params: { startDate, endDate }
    })
  },

  // 获取班级统计
  getClassStatistics(classId, startDate, endDate) {
    return request.get(`/statistics/class/${classId}`, {
      params: { startDate, endDate }
    })
  },

  // 生成学习报告
  generateReport(studentId, startDate, endDate) {
    return request.get(`/statistics/report/${studentId}`, {
      params: { startDate, endDate }
    })
  },

  // 获取成绩分布
  getScoreDistribution(classId) {
    return request.get(`/statistics/score-distribution/${classId}`)
  }
}

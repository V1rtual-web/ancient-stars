import request from '../utils/request'

export const teacherAPI = {
  // 获取学生学习进度
  getStudentProgress(studentId, params) {
    return request.get(`/api/teacher/student-progress/${studentId}`, { params })
  },

  // 获取班级学习统计
  getClassProgress(params) {
    return request.get('/api/teacher/class-progress', { params })
  },

  // 获取班级列表
  getClasses() {
    return request.get('/api/teacher/classes')
  },

  // 获取班级学生列表
  getClassStudents(classId) {
    return request.get(`/api/teacher/classes/${classId}/students`)
  }
}

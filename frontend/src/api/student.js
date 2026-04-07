import request from '../utils/request'

export const studentAPI = {
  // 创建学生账户
  createStudent(data) {
    return request.post('/teacher/students', data)
  },

  // 批量创建学生
  createStudentsBatch(students) {
    return request.post('/teacher/students/batch', students)
  },

  // 更新学生信息
  updateStudent(id, data) {
    return request.put(`/teacher/students/${id}`, data)
  },

  // 禁用/启用学生账户
  toggleStudentStatus(id, status) {
    const endpoint = status === 'ACTIVE' ? 'enable' : 'disable'
    return request.post(`/teacher/students/${id}/${endpoint}`)
  },

  // 获取学生列表
  getStudents(classId = null) {
    return request.get('/teacher/students', {
      params: classId ? { classId } : {}
    })
  },

  // 获取学生详情
  getStudent(id) {
    return request.get(`/teacher/students/${id}`)
  }
}

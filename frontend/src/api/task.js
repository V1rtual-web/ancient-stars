import request from '../utils/request'

export const taskAPI = {
  // 创建任务
  createTask(data) {
    return request.post('/tasks', data)
  },

  // 分配任务给学生
  assignTask(taskId, studentIds) {
    return request.post(`/tasks/${taskId}/assign`, studentIds)
  },

  // 获取任务详情
  getTask(id) {
    return request.get(`/tasks/${id}`)
  },

  // 获取学生的任务列表
  getStudentTasks(studentId) {
    return request.get(`/tasks/student/${studentId}`)
  },

  // 更新任务进度
  updateProgress(taskId, data) {
    return request.put(`/tasks/${taskId}/progress`, data)
  },

  // 获取所有任务（教师端）
  getAllTasks() {
    return request.get('/tasks')
  },

  // 更新任务
  updateTask(id, data) {
    return request.put(`/tasks/${id}`, data)
  },

  // 删除任务
  deleteTask(id) {
    return request.delete(`/tasks/${id}`)
  }
}

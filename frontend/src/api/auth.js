import request from '../utils/request'

export const authAPI = {
  // 用户登录
  login(username, password) {
    return request.post('/auth/login', { username, password })
  },
  
  // 用户注册
  register(data) {
    return request.post('/auth/register', data)
  },
  
  // 用户登出
  logout() {
    return request.post('/auth/logout')
  },
  
  // 刷新令牌
  refreshToken(refreshToken) {
    return request.post('/auth/refresh', { refreshToken })
  }
}

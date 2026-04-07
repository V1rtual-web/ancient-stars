import { defineStore } from 'pinia'
import { authAPI } from '../../api/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: localStorage.getItem('accessToken') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    userInfo: JSON.parse(localStorage.getItem('userInfo') || 'null')
  }),

  getters: {
    // 是否已登录
    isLoggedIn: (state) => !!state.token,
    
    // 用户角色
    userRole: (state) => state.userInfo?.role || '',
    
    // 是否是教师
    isTeacher: (state) => state.userInfo?.role === 'TEACHER',
    
    // 是否是学生
    isStudent: (state) => state.userInfo?.role === 'STUDENT',
    
    // 用户名
    username: (state) => state.userInfo?.username || '',
    
    // 真实姓名
    realName: (state) => state.userInfo?.realName || ''
  },

  actions: {
    // 设置token
    setToken(token, refreshToken) {
      this.token = token
      this.refreshToken = refreshToken
      localStorage.setItem('accessToken', token)
      localStorage.setItem('refreshToken', refreshToken)
    },

    // 设置用户信息
    setUserInfo(userInfo) {
      this.userInfo = userInfo
      localStorage.setItem('userInfo', JSON.stringify(userInfo))
    },

    // 登录
    async login(username, password, rememberMe = false) {
      try {
        const response = await authAPI.login(username, password)
        
        // 后端返回 success: true 表示成功
        if (response.success && response.data) {
          const data = response.data
          
          // 保存token
          this.setToken(data.accessToken, data.refreshToken)
          
          // 构建用户信息对象
          const userInfo = {
            userId: data.userId,
            username: data.username,
            role: data.role
          }
          this.setUserInfo(userInfo)
          
          // 如果勾选记住密码
          if (rememberMe) {
            localStorage.setItem('rememberedUsername', username)
          } else {
            localStorage.removeItem('rememberedUsername')
          }
          
          return { success: true, data: response.data }
        } else {
          return { success: false, message: response.error?.message || '登录失败' }
        }
      } catch (error) {
        return { success: false, message: error.message || '登录失败' }
      }
    },

    // 登出
    async logout() {
      try {
        await authAPI.logout()
      } catch (error) {
        console.error('登出失败:', error)
      } finally {
        // 清除本地存储
        this.token = ''
        this.refreshToken = ''
        this.userInfo = null
        localStorage.removeItem('accessToken')
        localStorage.removeItem('refreshToken')
        localStorage.removeItem('userInfo')
      }
    },

    // 刷新token
    async refreshAccessToken() {
      try {
        const response = await authAPI.refreshToken(this.refreshToken)
        if (response.success && response.data) {
          this.setToken(response.data.accessToken, this.refreshToken)
          return true
        }
        return false
      } catch (error) {
        console.error('刷新token失败:', error)
        return false
      }
    },

    // 获取用户信息（从localStorage恢复）
    async fetchUserInfo() {
      // 从localStorage读取已保存的用户信息
      const userInfo = JSON.parse(localStorage.getItem('userInfo') || 'null')
      if (userInfo) {
        this.userInfo = userInfo
        return true
      }
      return false
    }
  }
})

import axios from 'axios'
import { ElMessage } from 'element-plus'
import router from '../router'

// 创建axios实例
const request = axios.create({
  baseURL: '/api',
  timeout: 10000,
  headers: {
    'Content-Type': 'application/json'
  }
})

// 请求拦截器 - 添加Token
request.interceptors.request.use(
  config => {
    // 从localStorage获取token
    const token = localStorage.getItem('accessToken')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('请求错误:', error)
    return Promise.reject(error)
  }
)

// 响应拦截器 - 统一处理响应
request.interceptors.response.use(
  response => {
    const res = response.data
    
    // 后端返回的是 success 字段（boolean）
    if (res.success === true) {
      return res
    } else {
      // 失败响应
      const errorMsg = res.error?.message || res.message || '请求失败'
      ElMessage.error(errorMsg)
      return Promise.reject(new Error(errorMsg))
    }
  },
  error => {
    console.error('响应错误:', error)
    
    // 处理HTTP错误状态码
    if (error.response) {
      const status = error.response.status
      const errorData = error.response.data
      
      // 尝试从不同的路径获取错误消息
      const errorMsg = errorData?.message || errorData?.error?.message || errorData?.msg
      
      switch (status) {
        case 400:
          ElMessage.error(errorMsg || '请求参数错误')
          break
        case 401:
          // 未授权，清除token并跳转到登录页
          ElMessage.error('登录已过期，请重新登录')
          localStorage.removeItem('accessToken')
          localStorage.removeItem('refreshToken')
          localStorage.removeItem('userInfo')
          router.push('/login')
          break
        case 403:
          ElMessage.error('没有权限访问该资源')
          break
        case 404:
          ElMessage.error('请求的资源不存在')
          break
        case 500:
          ElMessage.error(errorMsg || '服务器错误，请稍后重试')
          break
        case 502:
          ElMessage.error('网关错误，请稍后重试')
          break
        case 503:
          ElMessage.error('服务暂时不可用，请稍后重试')
          break
        case 504:
          ElMessage.error('请求超时，请稍后重试')
          break
        default:
          ElMessage.error(errorMsg || `请求失败 (${status})`)
      }
    } else if (error.request) {
      // 请求已发出但没有收到响应 - 网络错误
      if (error.code === 'ECONNABORTED') {
        ElMessage.error('请求超时，请检查网络连接')
      } else if (error.message === 'Network Error') {
        ElMessage.error('网络连接失败，请检查网络设置')
      } else {
        ElMessage.error('网络错误，请检查网络连接')
      }
    } else {
      // 其他错误
      ElMessage.error(error.message || '请求失败')
    }
    
    return Promise.reject(error)
  }
)

export default request

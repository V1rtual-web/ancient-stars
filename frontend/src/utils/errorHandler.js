import { ElMessage } from 'element-plus'

/**
 * 统一错误处理函数
 * @param {Error} error - 错误对象
 * @param {string} defaultMessage - 默认错误消息
 * @param {boolean} showMessage - 是否显示错误消息
 * @returns {string} 错误消息
 */
export function handleError(error, defaultMessage = '操作失败', showMessage = true) {
  let errorMessage = defaultMessage

  if (error) {
    // 如果是取消操作（如确认对话框取消）
    if (error === 'cancel') {
      return null
    }

    // 从响应中提取错误消息
    if (error.response?.data?.message) {
      errorMessage = error.response.data.message
    } else if (error.response?.data?.error?.message) {
      errorMessage = error.response.data.error.message
    } else if (error.message) {
      errorMessage = error.message
    }
  }

  // 显示错误消息
  if (showMessage) {
    ElMessage.error(errorMessage)
  }

  return errorMessage
}

/**
 * 网络错误检测
 * @param {Error} error - 错误对象
 * @returns {boolean} 是否为网络错误
 */
export function isNetworkError(error) {
  return (
    !error.response &&
    (error.code === 'ECONNABORTED' ||
      error.message === 'Network Error' ||
      error.message.includes('timeout'))
  )
}

/**
 * 服务器错误检测
 * @param {Error} error - 错误对象
 * @returns {boolean} 是否为服务器错误
 */
export function isServerError(error) {
  return error.response && error.response.status >= 500
}

/**
 * 权限错误检测
 * @param {Error} error - 错误对象
 * @returns {boolean} 是否为权限错误
 */
export function isAuthError(error) {
  return error.response && (error.response.status === 401 || error.response.status === 403)
}

/**
 * 获取友好的错误提示
 * @param {Error} error - 错误对象
 * @returns {string} 友好的错误提示
 */
export function getFriendlyErrorMessage(error) {
  if (isNetworkError(error)) {
    return '网络连接失败，请检查网络设置'
  }

  if (isServerError(error)) {
    return '服务器错误，请稍后重试'
  }

  if (isAuthError(error)) {
    if (error.response.status === 401) {
      return '登录已过期，请重新登录'
    }
    return '没有权限访问该资源'
  }

  if (error.response?.data?.message) {
    return error.response.data.message
  }

  if (error.message) {
    return error.message
  }

  return '操作失败，请稍后重试'
}

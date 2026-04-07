import { ref } from 'vue'
import { ElLoading } from 'element-plus'

/**
 * 加载状态管理 Composable
 * @param {boolean} initialState - 初始加载状态
 * @returns {Object} 加载状态和方法
 */
export function useLoading(initialState = false) {
  const loading = ref(initialState)
  let loadingInstance = null

  /**
   * 开始加载
   * @param {Object} options - ElLoading 配置选项
   */
  const startLoading = (options = {}) => {
    loading.value = true
    if (options.fullscreen) {
      loadingInstance = ElLoading.service({
        lock: true,
        text: options.text || '加载中...',
        background: 'rgba(0, 0, 0, 0.7)',
        ...options
      })
    }
  }

  /**
   * 停止加载
   */
  const stopLoading = () => {
    loading.value = false
    if (loadingInstance) {
      loadingInstance.close()
      loadingInstance = null
    }
  }

  /**
   * 包装异步函数，自动管理加载状态
   * @param {Function} asyncFn - 异步函数
   * @param {Object} options - 加载选项
   * @returns {Function} 包装后的函数
   */
  const withLoading = (asyncFn, options = {}) => {
    return async (...args) => {
      try {
        startLoading(options)
        return await asyncFn(...args)
      } finally {
        stopLoading()
      }
    }
  }

  return {
    loading,
    startLoading,
    stopLoading,
    withLoading
  }
}

/**
 * 多个加载状态管理
 * @returns {Object} 加载状态管理方法
 */
export function useMultipleLoading() {
  const loadingStates = ref({})

  const setLoading = (key, value) => {
    loadingStates.value[key] = value
  }

  const isLoading = (key) => {
    return loadingStates.value[key] || false
  }

  const isAnyLoading = () => {
    return Object.values(loadingStates.value).some(state => state === true)
  }

  return {
    loadingStates,
    setLoading,
    isLoading,
    isAnyLoading
  }
}

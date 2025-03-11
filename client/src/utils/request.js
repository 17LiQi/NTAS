import axios from 'axios'
import { ElMessage } from 'element-plus'

// 创建axios实例
const service = axios.create({
  baseURL: '',
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json;charset=UTF-8',
    'Accept': 'application/json'
  },
  retry: 3,
  retryDelay: 1000
})

// 请求拦截器
service.interceptors.request.use(
  config => {
    // 处理参数编码，避免重复编码
    if (config.params) {
      const encodedParams = {}
      Object.keys(config.params).forEach(key => {
        const value = config.params[key]
        if (typeof value === 'string') {
          try {
            // 尝试解码，如果解码失败说明未编码
            decodeURIComponent(value)
            // 如果能解码成功，说明已经编码过，直接使用
            encodedParams[key] = value
          } catch {
            // 解码失败，说明未编码，进行编码
            encodedParams[key] = encodeURIComponent(value)
          }
        } else {
          encodedParams[key] = value
        }
      })
      config.params = encodedParams
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

// 响应拦截器
service.interceptors.response.use(
  response => {
    console.log('收到响应:', response.config.url, response.data)
    const res = response.data
    
    // 处理空响应
    if (!res) {
      return {
        code: 200,
        message: 'success',
        data: null
      }
    }

    // 处理错误响应
    if (res.code !== 200) {
      ElMessage({
        message: res.msg || res.message || '请求失败',
        type: 'error',
        duration: 5 * 1000
      })
      return Promise.reject(new Error(res.msg || res.message || '请求失败'))
    }

    // 处理成功响应
    return {
      code: res.code,
      message: res.msg || res.message || 'success',
      data: res.data || null
    }
  },
  error => {
    console.error('响应错误:', error.config?.url, error.response?.data || error.message)
    const errMsg = error.response?.data?.msg || error.response?.data?.message || error.message || '网络错误'
    ElMessage({
      message: errMsg,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  }
)

export default service 
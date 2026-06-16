import axios from 'axios'
import type { AxiosInstance, AxiosError, InternalAxiosRequestConfig } from 'axios'
import { ElMessage } from 'element-plus'
import type { R } from '@shared/types'

const http: AxiosInstance = axios.create({
  baseURL: '',
  timeout: 15000,
  headers: { 'Content-Type': 'application/json' },
})

http.interceptors.request.use((config: InternalAxiosRequestConfig) => {
  const token = localStorage.getItem('token')
  if (token && config.headers) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => {
    const body = response.data as R<unknown>
    if (body.code !== 200) {
      ElMessage.error(body.message || '请求失败')
      if (body.code === 40005) {
        localStorage.removeItem('token')
        window.location.href = '/login'
      }
      return Promise.reject(new Error(body.message))
    }
    return response
  },
  (error: AxiosError) => {
    if (error.response) {
      const status = error.response.status
      if (status === 401) {
        ElMessage.error('登录已过期，请重新登录')
        localStorage.removeItem('token')
        window.location.href = '/login'
      } else if (status >= 500) {
        ElMessage.error('服务器错误，请稍后重试')
      }
    } else if (error.code === 'ECONNABORTED') {
      ElMessage.error('请求超时，请检查网络')
    } else {
      ElMessage.error('网络连接失败')
    }
    return Promise.reject(error)
  }
)

export default http

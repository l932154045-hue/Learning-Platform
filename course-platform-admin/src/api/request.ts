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
  const token = localStorage.getItem('admin-token')
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
        localStorage.removeItem('admin-token')
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
        localStorage.removeItem('admin-token')
        window.location.href = '/login'
      }
    }
    ElMessage.error('请求失败')
    return Promise.reject(error)
  }
)

export default http

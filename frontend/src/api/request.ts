import axios from 'axios'
import type { AxiosInstance, AxiosResponse } from 'axios'
import { ElMessage } from 'element-plus'

// 声明 Vite 环境变量
declare global {
  interface ImportMeta {
    env: {
      VITE_API_BASE_URL?: string
    }
  }
}

const API_BASE_URL = import.meta.env?.VITE_API_BASE_URL || '/api'

/**
 * API 响应类型
 */
export interface ApiResponse<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

/**
 * 创建 Axios 实例
 */
const request: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  timeout: 30000,
  headers: {
    'Content-Type': 'application/json'
  }
})

/**
 * 请求拦截器
 */
request.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('access_token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  (error) => {
    return Promise.reject(error)
  }
)

/**
 * 响应拦截器
 */
request.interceptors.response.use(
  (response: AxiosResponse<ApiResponse<unknown>>) => {
    const { code, message } = response.data

    if (code === 200) {
      return response
    }

    // 错误处理
    ElMessage.error(message || '请求失败')

    // 401 跳转登录
    if (code === 401) {
      localStorage.removeItem('access_token')
      window.location.href = '/login'
    }

    return Promise.reject(new Error(message))
  },
  (error) => {
    const message = error.response?.data?.message || error.message || '网络错误'
    ElMessage.error(message)

    if (error.response?.status === 401) {
      localStorage.removeItem('access_token')
      window.location.href = '/login'
    }

    return Promise.reject(error)
  }
)

/**
 * 封装请求方法
 */
export function get<T>(url: string, params?: Record<string, unknown>): Promise<ApiResponse<T>> {
  return request.get(url, { params }).then(res => res.data as ApiResponse<T>)
}

export function post<T>(url: string, data?: unknown): Promise<ApiResponse<T>> {
  return request.post(url, data).then(res => res.data as ApiResponse<T>)
}

export function put<T>(url: string, data?: unknown): Promise<ApiResponse<T>> {
  return request.put(url, data).then(res => res.data as ApiResponse<T>)
}

export function del<T>(url: string): Promise<ApiResponse<T>> {
  return request.delete(url).then(res => res.data as ApiResponse<T>)
}

export default request

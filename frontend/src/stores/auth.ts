import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { User } from '@/types'

export const useAuthStore = defineStore('auth', () => {
  const user = ref<User | null>(null)
  const token = ref<string>('')

  // 初始化时从 localStorage 恢复
  const savedToken = localStorage.getItem('access_token')
  const savedUser = localStorage.getItem('user_info')
  if (savedToken && savedUser) {
    token.value = savedToken
    try {
      user.value = JSON.parse(savedUser) as User
    } catch {
      token.value = ''
      user.value = null
    }
  }

  const isAuthenticated = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 3)

  function setAuth(newToken: string, newUser: User) {
    token.value = newToken
    user.value = newUser
    localStorage.setItem('access_token', newToken)
    localStorage.setItem('user_info', JSON.stringify(newUser))
  }

  function clearAuth() {
    token.value = ''
    user.value = null
    localStorage.removeItem('access_token')
    localStorage.removeItem('user_info')
  }

  return {
    user,
    token,
    isAuthenticated,
    isAdmin,
    setAuth,
    clearAuth
  }
})

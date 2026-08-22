import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { authApi } from '@/api/auth'
import { userApi } from '@/api/user'
import type { LoginResult, UserInfo } from '@/types'

export const useUserStore = defineStore('user', () => {
  const token = ref(localStorage.getItem('token') || '')
  const userInfo = ref<UserInfo | null>(null)

  const isAdmin = computed(() => userInfo.value?.role === 'ADMIN')

  async function login(username: string, password: string) {
    const res: LoginResult = await authApi.login({ username, password })
    token.value = res.token
    localStorage.setItem('token', res.token)
    localStorage.setItem('role', res.role)
    userInfo.value = {
      id: res.userId,
      username: res.username,
      avatar: res.avatar,
      role: res.role,
      status: 1,
    }
  }

  async function fetchInfo() {
    if (!token.value) return
    userInfo.value = await userApi.info()
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    localStorage.removeItem('token')
    localStorage.removeItem('role')
  }

  return { token, userInfo, isAdmin, login, fetchInfo, logout }
})

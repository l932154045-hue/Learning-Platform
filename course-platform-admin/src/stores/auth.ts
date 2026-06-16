import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { UserInfoResp } from '@shared/types'

export const useAdminAuthStore = defineStore('admin-auth', () => {
  const token = ref<string>('')
  const user = ref<UserInfoResp | null>(null)
  const initialized = ref(false)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 1)

  function setLogin(data: { token: string; userId: number; nickname: string; role: number }) {
    token.value = data.token
    localStorage.setItem('admin-token', data.token)
  }

  function setUser(data: UserInfoResp) { user.value = data }

  async function restore() {
    const saved = localStorage.getItem('admin-token')
    if (saved) token.value = saved
    initialized.value = true
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('admin-token')
  }

  return { token, user, initialized, isLoggedIn, isAdmin, setLogin, setUser, restore, logout }
})

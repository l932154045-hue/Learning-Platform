import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import type { LoginResp, UserInfoResp } from '@shared/types'
import { userApi } from '@/api/modules/user'

export const useAuthStore = defineStore('auth', () => {
  const token = ref<string>('')
  const user = ref<UserInfoResp | null>(null)
  const initialized = ref(false)

  const isLoggedIn = computed(() => !!token.value)
  const isAdmin = computed(() => user.value?.role === 1)
  const userId = computed(() => user.value?.id ?? 0)

  function setLogin(data: LoginResp) {
    token.value = data.token
    localStorage.setItem('token', data.token)
  }

  function setUser(data: UserInfoResp) {
    user.value = data
  }

  async function restore() {
    const saved = localStorage.getItem('token')
    if (saved) {
      token.value = saved
      try {
        const res = await userApi.getInfo()
        if (res.data.data) {
          user.value = res.data.data
        }
      } catch {
        token.value = ''
        localStorage.removeItem('token')
      }
    }
    initialized.value = true
  }

  function logout() {
    token.value = ''
    user.value = null
    localStorage.removeItem('token')
  }

  return { token, user, initialized, isLoggedIn, isAdmin, userId, setLogin, setUser, restore, logout }
})

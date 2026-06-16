<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { userApi } from '@/api/modules/user'
import { useAuthStore } from '@/stores/auth'
import type { LoginReq } from '@shared/types'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<LoginReq>({ phone: '', password: '' })

const rules: FormRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await userApi.login(form)
    const data = res.data.data
    if (data) {
      authStore.setLogin({ token: data.token, userId: data.userId, nickname: data.nickname, role: data.role })
      ElMessage.success('登录成功')
      const redirect = (route.query.redirect as string) || '/'
      router.push(redirect)
    }
  } catch {
    /* error handled by interceptor */
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="auth-page">
    <div class="auth-card">
      <div class="auth-header">
        <span class="auth-logo" @click="router.push('/')">📚 学途</span>
        <h2>欢迎回来</h2>
        <p>登录你的账号，继续学习之旅</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="请输入密码" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleLogin">登录</el-button>
      </el-form>
      <p class="auth-switch">
        还没有账号？
        <router-link to="/register">立即注册 →</router-link>
      </p>
    </div>
  </div>
</template>

<style scoped>
.auth-page {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--color-surface);
  padding: var(--space-lg);
}
.auth-card {
  width: 100%;
  max-width: 400px;
  background: white;
  border-radius: var(--radius-lg);
  padding: var(--space-2xl);
  box-shadow: var(--shadow-card-hover);
}
.auth-header {
  text-align: center;
  margin-bottom: var(--space-xl);
}
.auth-logo { font-size: 28px; font-weight: 700; color: var(--color-primary); cursor: pointer; }
.auth-header h2 { font-size: var(--font-size-xl); font-weight: 700; margin-top: var(--space-md); }
.auth-header p { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: var(--space-xs); }
.submit-btn { width: 100%; }
.auth-switch { text-align: center; margin-top: var(--space-lg); font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.auth-switch a { color: var(--color-primary-hover); font-weight: 600; }
</style>

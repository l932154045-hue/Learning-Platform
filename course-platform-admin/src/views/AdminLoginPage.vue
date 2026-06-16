<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import axios from 'axios'
import type { R, LoginResp } from '@shared/types'
import { useAdminAuthStore } from '@/stores/auth'

const router = useRouter()
const auth = useAdminAuthStore()
const formRef = ref<FormInstance>()
const loading = ref(false)
const form = reactive({ phone: '', password: '' })

const rules: FormRules = {
  phone: [{ required: true, message: '请输入手机号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
}

async function handleLogin() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    const res = await axios.post<R<LoginResp>>('/api/user/login', form)
    const data = res.data.data
    if (data && data.role === 1) {
      auth.setLogin({ token: data.token, userId: data.userId, nickname: data.nickname, role: data.role })
      ElMessage.success('登录成功')
      router.push('/')
    } else {
      ElMessage.error('非管理员账号')
    }
  } catch { /* handled */ } finally { loading.value = false }
}
</script>

<template>
  <div class="login-page">
    <div class="login-card">
      <h1>📚 学途管理</h1>
      <p>管理员登录</p>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleLogin">
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" size="large" show-password />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleLogin">登录</el-button>
      </el-form>
    </div>
  </div>
</template>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: var(--color-surface); }
.login-card { width: 100%; max-width: 400px; background: white; border-radius: var(--radius-lg); padding: var(--space-2xl); box-shadow: var(--shadow-card-hover); text-align: center; }
.login-card h1 { font-size: var(--font-size-2xl); font-weight: 700; color: var(--color-primary); }
.login-card p { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin: var(--space-sm) 0 var(--space-xl); }
.submit-btn { width: 100%; }
</style>

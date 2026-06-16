<script setup lang="ts">
import { ref, reactive } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, type FormInstance, type FormRules } from 'element-plus'
import { userApi } from '@/api/modules/user'
import type { RegisterReq } from '@shared/types'

const router = useRouter()
const formRef = ref<FormInstance>()
const loading = ref(false)

const form = reactive<RegisterReq & { confirmPassword: string }>({
  username: '', phone: '', password: '', confirmPassword: '',
})

const rules: FormRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 2, max: 32, message: '用户名长度 2-32 个字符', trigger: 'blur' },
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号', trigger: 'blur' },
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 32, message: '密码长度 6-32 个字符', trigger: 'blur' },
  ],
  confirmPassword: [
    { required: true, message: '请再次输入密码', trigger: 'blur' },
    {
      validator: (_rule, value, callback) => {
        if (value !== form.password) callback(new Error('两次密码不一致'))
        else callback()
      },
      trigger: 'blur',
    },
  ],
}

async function handleRegister() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  loading.value = true
  try {
    await userApi.register({ username: form.username, phone: form.phone, password: form.password })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
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
        <h2>创建账号</h2>
        <p>加入学途，开启你的学习之旅</p>
      </div>
      <el-form ref="formRef" :model="form" :rules="rules" label-position="top" @keyup.enter="handleRegister">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="2-32 个字符" size="large" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入手机号" size="large" />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="form.password" type="password" placeholder="6-32 个字符" size="large" show-password />
        </el-form-item>
        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input v-model="form.confirmPassword" type="password" placeholder="请再次输入密码" size="large" />
        </el-form-item>
        <el-button type="primary" size="large" :loading="loading" class="submit-btn" @click="handleRegister">注册</el-button>
      </el-form>
      <p class="auth-switch">
        已有账号？<router-link to="/login">立即登录 →</router-link>
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
.auth-header { text-align: center; margin-bottom: var(--space-xl); }
.auth-logo { font-size: 28px; font-weight: 700; color: var(--color-primary); cursor: pointer; }
.auth-header h2 { font-size: var(--font-size-xl); font-weight: 700; margin-top: var(--space-md); }
.auth-header p { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: var(--space-xs); }
.submit-btn { width: 100%; }
.auth-switch { text-align: center; margin-top: var(--space-lg); font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.auth-switch a { color: var(--color-primary-hover); font-weight: 600; }
</style>

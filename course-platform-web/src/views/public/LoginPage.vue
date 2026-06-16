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
const loginRole = ref<'student' | 'admin'>('student')

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

      // 身份校验：管理员登录需 role=1
      if (loginRole.value === 'admin' && data.role !== 1) {
        authStore.logout()
        ElMessage.error('该账号不是管理员，请选择"学员登录"')
        loading.value = false
        return
      }

      // 学员登录不允许管理员账号
      if (loginRole.value === 'student' && data.role === 1) {
        authStore.logout()
        ElMessage.error('该账号是管理员，请选择"管理员登录"')
        loading.value = false
        return
      }

      ElMessage.success('登录成功')

      // 根据身份跳转不同页面
      if (loginRole.value === 'admin') {
        router.push('/admin')
      } else {
        const redirect = (route.query.redirect as string) || '/'
        router.push(redirect)
      }
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
        <p>选择身份登录</p>
      </div>

      <!-- 身份选择 -->
      <div class="role-selector">
        <div
          :class="['role-option', { active: loginRole === 'student' }]"
          @click="loginRole = 'student'"
        >
          <span class="role-icon">🎓</span>
          <span class="role-label">学员登录</span>
        </div>
        <div
          :class="['role-option', { active: loginRole === 'admin' }]"
          @click="loginRole = 'admin'"
        >
          <span class="role-icon">🔧</span>
          <span class="role-label">管理员登录</span>
        </div>
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
.role-selector {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
}
.role-option {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-xs);
  padding: var(--space-md);
  border: 2px solid var(--color-border);
  border-radius: var(--radius-md);
  cursor: pointer;
  transition: all 0.2s;
}
.role-option:hover {
  border-color: var(--color-primary-hover);
}
.role-option.active {
  border-color: var(--color-primary);
  background: rgba(28, 27, 59, 0.04);
}
.role-icon { font-size: 28px; }
.role-label { font-size: var(--font-size-sm); font-weight: 600; color: var(--color-text); }
.role-option.active .role-label { color: var(--color-primary); }

.auth-switch { text-align: center; margin-top: var(--space-lg); font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.auth-switch a { color: var(--color-primary-hover); font-weight: 600; }
</style>

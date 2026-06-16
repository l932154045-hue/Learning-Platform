<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/modules/user'

const auth = useAuthStore()
const editing = ref(false)
const form = ref({ nickname: '', email: '', avatarUrl: '' })
const saving = ref(false)

onMounted(() => {
  if (auth.user) {
    form.value = {
      nickname: auth.user.nickname || '',
      email: auth.user.email || '',
      avatarUrl: auth.user.avatarUrl || '',
    }
  }
})

function startEdit() { editing.value = true }

async function save() {
  saving.value = true
  try {
    await userApi.updateInfo(form.value)
    if (auth.user) {
      auth.user.nickname = form.value.nickname
      auth.user.email = form.value.email
      auth.user.avatarUrl = form.value.avatarUrl
    }
    editing.value = false
    ElMessage.success('保存成功')
  } catch { /* handled */ } finally { saving.value = false }
}

function cancel() {
  if (auth.user) {
    form.value = {
      nickname: auth.user.nickname || '',
      email: auth.user.email || '',
      avatarUrl: auth.user.avatarUrl || '',
    }
  }
  editing.value = false
}
</script>

<template>
  <div class="profile-page">
    <AppHeader />
    <div class="container page-section">
      <h1 class="page-title">个人中心</h1>
      <div class="profile-card">
        <div class="profile-head">
          <el-avatar :size="72" icon="UserFilled" />
          <div class="head-info">
            <h2>{{ auth.user?.nickname || auth.user?.username }}</h2>
            <p>{{ auth.user?.phone }}</p>
            <el-tag v-if="auth.isAdmin" type="warning" size="small">管理员</el-tag>
          </div>
        </div>
        <div class="profile-body">
          <div class="info-row">
            <label>昵称</label>
            <template v-if="editing">
              <el-input v-model="form.nickname" size="small" />
            </template>
            <span v-else>{{ auth.user?.nickname || '-' }}</span>
          </div>
          <div class="info-row">
            <label>邮箱</label>
            <template v-if="editing">
              <el-input v-model="form.email" size="small" placeholder="选填" />
            </template>
            <span v-else>{{ auth.user?.email || '-' }}</span>
          </div>
          <div class="info-row">
            <label>头像链接</label>
            <template v-if="editing">
              <el-input v-model="form.avatarUrl" size="small" placeholder="选填" />
            </template>
            <span v-else>{{ auth.user?.avatarUrl || '-' }}</span>
          </div>
        </div>
        <div class="profile-actions">
          <template v-if="editing">
            <el-button type="primary" :loading="saving" @click="save">保存</el-button>
            <el-button @click="cancel">取消</el-button>
          </template>
          <el-button v-else @click="startEdit">编辑资料</el-button>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<style scoped>
.page-title { font-size: var(--font-size-2xl); font-weight: 700; margin-bottom: var(--space-xl); }
.profile-card { background: white; border: 1px solid var(--color-border); border-radius: var(--radius-md); overflow: hidden; max-width: 600px; }
.profile-head { display: flex; align-items: center; gap: var(--space-lg); padding: var(--space-xl); border-bottom: 1px solid var(--color-border); background: var(--color-surface-alt); }
.head-info h2 { font-size: var(--font-size-lg); font-weight: 700; }
.head-info p { font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.profile-body { padding: var(--space-xl); }
.info-row { display: flex; align-items: center; gap: var(--space-lg); padding: var(--space-md) 0; border-bottom: 1px solid var(--color-border); }
.info-row:last-child { border-bottom: none; }
.info-row label { width: 80px; font-weight: 600; font-size: var(--font-size-sm); flex-shrink: 0; }
.info-row span { font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.profile-actions { padding: var(--space-lg) var(--space-xl); border-top: 1px solid var(--color-border); }
</style>

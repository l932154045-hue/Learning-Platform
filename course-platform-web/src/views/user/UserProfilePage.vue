<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { useAuthStore } from '@/stores/auth'
import { userApi } from '@/api/modules/user'
import { learningApi } from '@/api/modules/learning'
import { orderApi } from '@/api/modules/order'

const router = useRouter()
const auth = useAuthStore()
const editing = ref(false)
const form = ref({ nickname: '', email: '', avatarUrl: '' })
const saving = ref(false)

// 统计
const courseCount = ref(0)
const orderCount = ref(0)

// 修改密码
const pwForm = reactive({ oldPassword: '', newPassword: '', confirmPassword: '' })
const pwSaving = ref(false)

onMounted(async () => {
  if (auth.user) {
    form.value = {
      nickname: auth.user.nickname || '',
      email: auth.user.email || '',
      avatarUrl: auth.user.avatarUrl || '',
    }
  }
  // 加载统计
  try {
    const [courses, orders] = await Promise.all([
      learningApi.getMyCourses().catch(() => ({ data: { data: [] } })),
      orderApi.getList().catch(() => ({ data: { data: [] } })),
    ])
    courseCount.value = (courses.data.data || []).length
    orderCount.value = (orders.data.data || []).length
  } catch { /* ignore */ }
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

async function changePassword() {
  if (!pwForm.oldPassword || !pwForm.newPassword) {
    ElMessage.warning('请填写密码')
    return
  }
  if (pwForm.newPassword !== pwForm.confirmPassword) {
    ElMessage.warning('两次密码不一致')
    return
  }
  pwSaving.value = true
  try {
    await userApi.changePassword(pwForm.oldPassword, pwForm.newPassword)
    ElMessage.success('密码修改成功')
    pwForm.oldPassword = ''
    pwForm.newPassword = ''
    pwForm.confirmPassword = ''
  } catch { /* handled */ } finally { pwSaving.value = false }
}
</script>

<template>
  <div class="profile-page">
    <AppHeader />
    <div class="container page-section">
      <h1 class="page-title">个人中心</h1>

      <div class="profile-grid">
        <!-- 左栏：用户信息 + 修改密码 -->
        <div class="profile-left">
          <!-- 用户信息卡片 -->
          <div class="card">
            <div class="card-head">
              <el-avatar :size="64" icon="UserFilled" />
              <div class="head-info">
                <h2>{{ auth.user?.nickname || auth.user?.username }}</h2>
                <p>{{ auth.user?.phone }}</p>
                <el-tag v-if="auth.isAdmin" type="warning" size="small">管理员</el-tag>
              </div>
            </div>
            <div class="card-body">
              <div class="info-row">
                <label>昵称</label>
                <el-input v-if="editing" v-model="form.nickname" size="small" />
                <span v-else>{{ auth.user?.nickname || '-' }}</span>
              </div>
              <div class="info-row">
                <label>邮箱</label>
                <el-input v-if="editing" v-model="form.email" size="small" placeholder="选填" />
                <span v-else>{{ auth.user?.email || '-' }}</span>
              </div>
              <div class="info-row">
                <label>头像链接</label>
                <el-input v-if="editing" v-model="form.avatarUrl" size="small" placeholder="选填" />
                <span v-else class="ellipsis">{{ auth.user?.avatarUrl || '-' }}</span>
              </div>
            </div>
            <div class="card-foot">
              <template v-if="editing">
                <el-button type="primary" size="small" :loading="saving" @click="save">保存</el-button>
                <el-button size="small" @click="cancel">取消</el-button>
              </template>
              <el-button v-else size="small" @click="startEdit">编辑资料</el-button>
            </div>
          </div>

          <!-- 修改密码 -->
          <div class="card">
            <h3 class="card-title">修改密码</h3>
            <div class="card-body">
              <div class="info-row">
                <label>旧密码</label>
                <el-input v-model="pwForm.oldPassword" type="password" size="small" show-password />
              </div>
              <div class="info-row">
                <label>新密码</label>
                <el-input v-model="pwForm.newPassword" type="password" size="small" show-password />
              </div>
              <div class="info-row">
                <label>确认密码</label>
                <el-input v-model="pwForm.confirmPassword" type="password" size="small" show-password />
              </div>
            </div>
            <div class="card-foot">
              <el-button type="primary" size="small" :loading="pwSaving" @click="changePassword">修改密码</el-button>
            </div>
          </div>
        </div>

        <!-- 右栏：统计 + 快捷入口 -->
        <div class="profile-right">
          <div class="card">
            <h3 class="card-title">学习概览</h3>
            <div class="stats">
              <div class="stat-item" @click="router.push('/my-learning')">
                <span class="stat-num">{{ courseCount }}</span>
                <span class="stat-label">已购课程</span>
              </div>
              <div class="stat-item" @click="router.push('/orders')">
                <span class="stat-num">{{ orderCount }}</span>
                <span class="stat-label">历史订单</span>
              </div>
            </div>
          </div>

          <div class="card">
            <h3 class="card-title">快捷入口</h3>
            <div class="quick-links">
              <div class="link-item" @click="router.push('/my-learning')">
                <span>📚</span> 我的学习
              </div>
              <div class="link-item" @click="router.push('/orders')">
                <span>📦</span> 我的订单
              </div>
              <div class="link-item" v-if="auth.isAdmin" @click="router.push('/admin')">
                <span>🔧</span> 管理后台
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
    <AppFooter />
  </div>
</template>

<style scoped>
.page-title { font-size: var(--font-size-2xl); font-weight: 700; margin-bottom: var(--space-xl); }

.profile-grid {
  display: grid;
  grid-template-columns: 1fr 320px;
  gap: var(--space-lg);
  align-items: start;
}
@media (max-width: 768px) { .profile-grid { grid-template-columns: 1fr; } }

.profile-left { display: flex; flex-direction: column; gap: var(--space-lg); }
.profile-right { display: flex; flex-direction: column; gap: var(--space-lg); }

.card {
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  overflow: hidden;
}
.card-head {
  display: flex; align-items: center; gap: var(--space-lg);
  padding: var(--space-lg); border-bottom: 1px solid var(--color-border);
  background: var(--color-surface-alt);
}
.head-info h2 { font-size: var(--font-size-base); font-weight: 700; }
.head-info p { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: 2px; }

.card-title {
  font-size: var(--font-size-base); font-weight: 700;
  padding: var(--space-lg); border-bottom: 1px solid var(--color-border);
}
.card-body { padding: var(--space-lg); }
.card-foot {
  padding: var(--space-md) var(--space-lg);
  border-top: 1px solid var(--color-border);
}

.info-row { display: flex; align-items: center; gap: var(--space-md); padding: var(--space-sm) 0; }
.info-row label { width: 72px; font-weight: 600; font-size: var(--font-size-sm); flex-shrink: 0; color: var(--color-text-secondary); }
.info-row span { font-size: var(--font-size-sm); }
.ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; max-width: 300px; }

.stats { display: flex; gap: var(--space-lg); }
.stat-item {
  flex: 1; text-align: center; cursor: pointer; padding: var(--space-md);
  border-radius: var(--radius-sm); transition: background 0.15s;
}
.stat-item:hover { background: var(--color-surface-alt); }
.stat-num { display: block; font-size: var(--font-size-2xl); font-weight: 700; color: var(--color-primary); }
.stat-label { display: block; font-size: var(--font-size-xs); color: var(--color-text-secondary); margin-top: 4px; }

.quick-links { display: flex; flex-direction: column; }
.link-item {
  display: flex; align-items: center; gap: var(--space-md);
  padding: var(--space-md) var(--space-lg); font-size: var(--font-size-sm);
  cursor: pointer; transition: background 0.15s;
}
.link-item:hover { background: var(--color-surface-alt); }
.link-item span { font-size: 18px; }
</style>

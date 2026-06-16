<script setup lang="ts">
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const menuItems = [
  { path: '/admin', label: '仪表盘', icon: 'DataBoard' },
  { path: '/admin/courses', label: '课程管理', icon: 'Reading' },
  { path: '/admin/categories', label: '分类管理', icon: 'Collection' },
  { path: '/admin/orders', label: '订单管理', icon: 'Document' },
  { path: '/admin/users', label: '用户管理', icon: 'User' },
]

function isActive(path: string) {
  if (path === '/admin') return route.path === '/admin'
  return route.path.startsWith(path)
}

function goTo(path: string) { router.push(path) }
function handleLogout() { auth.logout(); router.push('/') }
</script>

<template>
  <div class="sidebar">
    <div class="sidebar-logo" @click="goTo('/')">
      <span>📚</span><span>学途管理</span>
    </div>
    <div class="sidebar-menu">
      <div
        v-for="item in menuItems"
        :key="item.path"
        :class="['menu-item', { active: isActive(item.path) }]"
        @click="goTo(item.path)"
      >
        <el-icon><component :is="item.icon" /></el-icon>
        <span>{{ item.label }}</span>
      </div>
    </div>
    <div class="sidebar-footer">
      <div class="menu-item" @click="handleLogout">
        <el-icon><svg viewBox="0 0 24 24" width="18" height="18" fill="none" stroke="currentColor" stroke-width="2"><path d="M9 21H5a2 2 0 01-2-2V5a2 2 0 012-2h4M16 17l5-5-5-5M21 12H9"/></svg></el-icon>
        <span>退出</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.sidebar { display: flex; flex-direction: column; height: 100vh; }
.sidebar-logo {
  display: flex; align-items: center; gap: var(--space-sm);
  padding: var(--space-lg); font-size: var(--font-size-lg); font-weight: 700;
  color: white; cursor: pointer; border-bottom: 1px solid rgba(255,255,255,0.1);
}
.sidebar-menu { flex: 1; padding: var(--space-md) 0; overflow-y: auto; }
.menu-item {
  display: flex; align-items: center; gap: var(--space-md);
  padding: var(--space-md) var(--space-lg); margin: 2px var(--space-sm);
  border-radius: var(--radius-sm); color: rgba(255,255,255,0.65);
  font-size: var(--font-size-sm); cursor: pointer; transition: all 0.15s;
}
.menu-item:hover { color: white; background: rgba(255,255,255,0.08); }
.menu-item.active { color: white; background: rgba(255,255,255,0.15); font-weight: 600; }
.sidebar-footer { padding: var(--space-md) 0; border-top: 1px solid rgba(255,255,255,0.1); }
</style>

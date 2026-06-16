<script setup lang="ts">
import { computed } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '@/stores/auth'
import { useCartStore } from '@/stores/cart'
import { ElMessage } from 'element-plus'

const router = useRouter()
const route = useRoute()
const authStore = useAuthStore()
const cartStore = useCartStore()

const isLoggedIn = computed(() => authStore.isLoggedIn)
const isAdmin = computed(() => authStore.isAdmin)
const cartCount = computed(() => cartStore.totalCount)
const userNickname = computed(() => authStore.user?.nickname ?? '')

const navItems = [
  { path: '/', label: '首页' },
  { path: '/courses', label: '全部课程' },
]

function goTo(path: string) {
  router.push(path)
}

function handleLogout() {
  authStore.logout()
  ElMessage.success('已退出登录')
  router.push('/')
}

if (isLoggedIn.value) {
  cartStore.fetchCart()
}
</script>

<template>
  <header class="app-header">
    <div class="header-inner container">
      <div class="logo" @click="goTo('/')">
        <span class="logo-icon">📚</span>
        <span class="logo-text">学途</span>
      </div>

      <nav class="nav-links">
        <a
          v-for="item in navItems"
          :key="item.path"
          :class="['nav-item', { active: route.path === item.path }]"
          @click="goTo(item.path)"
        >
          {{ item.label }}
        </a>
        <a
          v-if="isLoggedIn"
          :class="['nav-item', { active: route.path === '/my-learning' }]"
          @click="goTo('/my-learning')"
        >
          我的学习
        </a>
      </nav>

      <div class="header-actions">
        <template v-if="isLoggedIn">
          <div class="cart-btn" @click="goTo('/cart')">
            <el-icon :size="20"><svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><circle cx="9" cy="21" r="1"/><circle cx="20" cy="21" r="1"/><path d="M1 1h4l2.68 13.39a2 2 0 0 0 2 1.61h9.72a2 2 0 0 0 2-1.61L23 6H6"/></svg></el-icon>
            <span v-if="cartCount > 0" class="cart-badge">{{ cartCount }}</span>
          </div>
          <el-dropdown trigger="click">
            <span class="user-btn">
              <el-avatar :size="32" icon="UserFilled" />
              <span class="user-name">{{ userNickname }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="goTo('/profile')">个人中心</el-dropdown-item>
                <el-dropdown-item @click="goTo('/orders')">我的订单</el-dropdown-item>
                <el-dropdown-item v-if="isAdmin" @click="goTo('/admin')">管理后台</el-dropdown-item>
                <el-dropdown-item divided @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </template>
        <template v-else>
          <el-button text @click="goTo('/login')">登录</el-button>
          <el-button type="primary" size="small" @click="goTo('/register')">注册</el-button>
        </template>
      </div>
    </div>
  </header>
</template>

<style scoped>
.app-header {
  position: sticky;
  top: 0;
  z-index: 100;
  height: var(--header-height);
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  border-bottom: 1px solid var(--color-border);
}

.header-inner {
  display: flex;
  align-items: center;
  height: 100%;
  gap: var(--space-xl);
}

.logo {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
  flex-shrink: 0;
}
.logo-icon {
  font-size: 24px;
}
.logo-text {
  font-size: var(--font-size-xl);
  font-weight: 700;
  color: var(--color-primary);
  letter-spacing: -0.5px;
}

.nav-links {
  display: flex;
  gap: var(--space-xs);
  flex: 1;
}
.nav-item {
  padding: var(--space-sm) var(--space-md);
  border-radius: var(--radius-sm);
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-text-secondary);
  cursor: pointer;
  transition: color 0.15s, background 0.15s;
}
.nav-item:hover,
.nav-item.active {
  color: var(--color-primary);
  background: rgba(30, 30, 74, 0.05);
}

.header-actions {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  flex-shrink: 0;
}

.cart-btn {
  position: relative;
  cursor: pointer;
  color: var(--color-text-secondary);
  padding: var(--space-sm);
  border-radius: var(--radius-sm);
  transition: color 0.15s;
}
.cart-btn:hover {
  color: var(--color-primary);
}
.cart-badge {
  position: absolute;
  top: -2px;
  right: -4px;
  min-width: 16px;
  height: 16px;
  border-radius: 8px;
  background: var(--color-accent);
  color: white;
  font-size: 10px;
  font-weight: 600;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 4px;
}

.user-btn {
  display: flex;
  align-items: center;
  gap: var(--space-sm);
  cursor: pointer;
}
.user-name {
  font-size: var(--font-size-sm);
  font-weight: 500;
}
</style>

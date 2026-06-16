<script setup lang="ts">
import AdminSidebar from './AdminSidebar.vue'
</script>

<template>
  <el-container class="admin-layout">
    <el-aside width="220px">
      <AdminSidebar />
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <span class="header-title">学途管理后台</span>
      </el-header>
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<style scoped>
.admin-layout { min-height: 100vh; }
.el-aside { background: var(--color-primary); }
.admin-header {
  background: white;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  padding: 0 var(--space-lg);
}
.header-title { font-size: var(--font-size-base); font-weight: 600; color: var(--color-text-secondary); }
.el-main { background: var(--color-surface); min-height: calc(100vh - 60px); }
</style>

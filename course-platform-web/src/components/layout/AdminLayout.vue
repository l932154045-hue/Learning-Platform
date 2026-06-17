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
.admin-layout {
  --sidebar-width: 220px;
  min-height: 100vh;
}
.el-aside {
  width: var(--sidebar-width) !important;
  background: var(--color-primary);
  position: fixed;
  top: 0;
  left: 0;
  bottom: 0;
  z-index: 10;
  overflow-y: auto;
}
.admin-header {
  background: white;
  border-bottom: 1px solid var(--color-border);
  display: flex;
  align-items: center;
  padding: 0 var(--space-lg);
  position: sticky;
  top: 0;
  z-index: 5;
}
.header-title { font-size: var(--font-size-base); font-weight: 600; color: var(--color-text-secondary); }
.el-main { background: var(--color-surface); min-height: calc(100vh - 60px); }
.admin-layout > .el-container {
  margin-left: var(--sidebar-width);
}
</style>

<!-- 管理页面共享样式：侧边栏内子页面继承 -->
<style>
.search-bar { display: flex; gap: var(--space-sm); margin-bottom: var(--space-md); align-items: center; }
.pagination-wrap { display: flex; justify-content: center; margin-top: var(--space-xl); }
</style>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminUserApi } from '@/api/modules/admin-user'
// Note: user listing endpoint is admin-only in backend but not yet implemented as a proper list
// Using a fallback — the admin can manage users by ID
const users = ref<any[]>([])
const loading = ref(false)

onMounted(() => { fetchUsers() })

async function fetchUsers() {
  loading.value = true
  // Backend does not have a user list endpoint exposed yet.
  // This is a placeholder - frontend is ready when backend adds it.
  users.value = []
  loading.value = false
}

async function handleToggleStatus(user: any) {
  const newStatus = user.status === 1 ? 0 : 1
  try {
    await adminUserApi.updateStatus(user.id, newStatus)
    ElMessage.success('状态已更新')
    user.status = newStatus
  } catch { /* handled */ }
}
</script>

<template>
  <div class="page">
    <div class="page-head">
      <h1>用户管理</h1>
    </div>
    <div v-if="users.length === 0 && !loading">
      <el-empty description="用户列表接口待后端实现。当前可通过 /api/admin/user/{id}/status 按ID管理用户状态。">
        <template #default>
          <p style="color: var(--color-text-secondary); font-size: 13px; margin-top: 8px;">
            管理端框架已就绪，待后端补充用户列表 API 后即可展示数据。
          </p>
        </template>
      </el-empty>
    </div>
    <el-table v-else :data="users" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="role" label="角色" width="80">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'warning' : 'info'" size="small">
            {{ row.role === 1 ? '管理员' : '学员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button size="small" :type="row.status === 1 ? 'warning' : 'success'" @click="handleToggleStatus(row)">
            {{ row.status === 1 ? '禁用' : '启用' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

<style scoped>
.page { padding: var(--space-lg); }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.page-head h1 { font-size: var(--font-size-xl); font-weight: 700; }
</style>

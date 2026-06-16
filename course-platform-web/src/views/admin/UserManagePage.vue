<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminUserApi } from '@/api/modules/admin-user'
import type { UserListItem } from '@shared/types'

const router = useRouter()
const users = ref<UserListItem[]>([])
const loading = ref(false)

onMounted(() => { fetchUsers() })

async function fetchUsers() {
  loading.value = true
  try {
    const res = await adminUserApi.list({ pageNum: 1, pageSize: 200 })
    users.value = res.data.data?.records ?? []
  } catch { /* handled */ }
  finally { loading.value = false }
}

async function handleToggleStatus(user: UserListItem) {
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
    <el-button text @click="router.push('/admin')">← 返回仪表盘</el-button>
    <div class="page-head">
      <h1>用户管理</h1>
    </div>
    <el-table :data="users" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="username" label="用户名" width="120" />
      <el-table-column prop="nickname" label="昵称" width="120" />
      <el-table-column prop="phone" label="手机号" width="140" />
      <el-table-column prop="email" label="邮箱" width="180" />
      <el-table-column prop="role" label="角色" width="80">
        <template #default="{ row }">
          <el-tag :type="row.role === 1 ? 'warning' : 'info'" size="small">
            {{ row.role === 1 ? '管理员' : '学员' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="状态" width="80">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'" size="small">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="注册时间" width="180" />
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

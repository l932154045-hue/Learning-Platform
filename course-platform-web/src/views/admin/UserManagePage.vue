<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { adminUserApi } from '@/api/modules/admin-user'
import type { UserListItem } from '@shared/types'

const users = ref<UserListItem[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)
const keyword = ref('')
const roleFilter = ref<number | undefined>(undefined)
const statusFilter = ref<number | undefined>(undefined)

onMounted(() => { fetchUsers() })

async function fetchUsers() {
  loading.value = true
  try {
    const res = await adminUserApi.list({
      pageNum: pageNum.value, pageSize: pageSize.value,
      keyword: keyword.value || undefined,
      roleFilter: roleFilter.value,
      status: statusFilter.value,
    })
    users.value = res.data.data?.records ?? []
    total.value = res.data.data?.total ?? 0
  } catch { /* handled */ }
  finally { loading.value = false }
}

function onSearch() {
  pageNum.value = 1
  fetchUsers()
}

function onPageChange(page: number) {
  pageNum.value = page
  fetchUsers()
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
    <div class="page-head">
      <h1>用户管理</h1>
    </div>

    <div class="search-bar">
      <el-input v-model="keyword" placeholder="搜索用户名/昵称/手机号..." clearable style="width:260px" @keyup.enter="onSearch" @clear="onSearch" />
      <el-select v-model="roleFilter" placeholder="全部角色" clearable style="width:120px" @change="onSearch">
        <el-option :value="1" label="管理员" />
        <el-option :value="0" label="学员" />
      </el-select>
      <el-select v-model="statusFilter" placeholder="全部状态" clearable style="width:120px" @change="onSearch">
        <el-option :value="1" label="正常" />
        <el-option :value="0" label="禁用" />
      </el-select>
      <el-button type="primary" @click="onSearch">搜索</el-button>
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

    <div class="pagination-wrap" v-if="total > pageSize">
      <el-pagination background layout="total, prev, pager, next" :total="total" :page-size="pageSize" :current-page="pageNum" @current-change="onPageChange" />
    </div>
  </div>
</template>

<style scoped>
.page { padding: var(--space-lg); }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.page-head h1 { font-size: var(--font-size-xl); font-weight: 700; }
.page-head h1 { font-size: var(--font-size-xl); font-weight: 700; }
</style>

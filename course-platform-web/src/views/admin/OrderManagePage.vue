<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { adminOrderApi } from '@/api/modules/admin-order'
import { getOrderStatusTag } from '@/utils/order'
import type { OrderListItem } from '@shared/types'

const router = useRouter()
const orders = ref<OrderListItem[]>([])
const loading = ref(false)
const pageNum = ref(1)
const pageSize = ref(20)
const total = ref(0)

onMounted(() => { fetchOrders() })

async function fetchOrders() {
  loading.value = true
  try {
    const res = await adminOrderApi.list({ pageNum: pageNum.value, pageSize: pageSize.value })
    orders.value = res.data.data?.records ?? []
    total.value = res.data.data?.total ?? 0
  } catch { /* handled */ }
  finally { loading.value = false }
}

function onPageChange(page: number) {
  pageNum.value = page
  fetchOrders()
}

async function handleCancel(row: OrderListItem) {
  try {
    await adminOrderApi.updateStatus(row.id, 2)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch { /* handled */ }
}
</script>

<template>
  <div class="page">
    <el-button text @click="router.push('/admin')">← 返回仪表盘</el-button>
    <div class="page-head">
      <h1>订单管理</h1>
    </div>
    <el-table :data="orders" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="orderNo" label="订单号" width="200" />
      <el-table-column prop="userId" label="用户ID" width="80" />
      <el-table-column prop="courseTitle" label="课程" min-width="180" />
      <el-table-column label="金额" width="100">
        <template #default="{ row }">¥{{ (row as OrderListItem).totalAmount }}</template>
      </el-table-column>
      <el-table-column label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="getOrderStatusTag((row as OrderListItem).status).type" size="small">
            {{ getOrderStatusTag((row as OrderListItem).status).text }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="180" />
      <el-table-column label="操作" width="120" fixed="right">
        <template #default="{ row }">
          <el-button
            v-if="(row as OrderListItem).status === 0"
            size="small"
            type="danger"
            @click="handleCancel(row as OrderListItem)"
          >
            取消
          </el-button>
          <span v-else style="color: #999">-</span>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="total > pageSize" class="pagination-wrap">
      <el-pagination
        background
        layout="total, prev, pager, next"
        :total="total"
        :page-size="pageSize"
        :current-page="pageNum"
        @current-change="onPageChange"
      />
    </div>
  </div>
</template>

<style scoped>
.page { padding: var(--space-lg); }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.page-head h1 { font-size: var(--font-size-xl); font-weight: 700; }
.pagination-wrap { display: flex; justify-content: center; margin-top: var(--space-xl); }
</style>

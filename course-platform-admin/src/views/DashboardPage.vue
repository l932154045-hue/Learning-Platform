<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminDashboardApi } from '@/api/modules/admin-dashboard'
import type { DashboardStats, OrderListItem } from '@shared/types'

const stats = ref<DashboardStats>({
  totalCourses: 0, totalUsers: 0, totalOrders: 0, totalRevenue: 0, recentOrders: [],
})
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await adminDashboardApi.getStats()
    if (res.data.data) stats.value = res.data.data
  } catch { /* handled */ }
  finally { loading.value = false }
})

const statCards = [
  { key: 'totalCourses', label: '课程总数', icon: '📚', color: '#409eff' },
  { key: 'totalUsers', label: '用户总数', icon: '👥', color: '#67c23a' },
  { key: 'totalOrders', label: '订单总数', icon: '📋', color: '#e6a23c' },
  { key: 'totalRevenue', label: '总营收(元)', icon: '💰', color: '#f56c6c' },
]

function getCardValue(key: string): string {
  const val = (stats.value as any)[key]
  if (key === 'totalRevenue') return `¥${(val || 0).toFixed(2)}`
  return String(val ?? 0)
}

function getOrderStatusTag(status: number) {
  const map: Record<number, { type: string; text: string }> = {
    0: { type: 'warning', text: '待支付' },
    1: { type: 'success', text: '已支付' },
    2: { type: 'info', text: '已取消' },
    3: { type: 'danger', text: '已退款' },
  }
  return map[status] || { type: 'info', text: '未知' }
}
</script>

<template>
  <div class="dashboard">
    <h1>仪表盘</h1>

    <div class="stat-cards" v-loading="loading">
      <div class="stat-card" v-for="card in statCards" :key="card.key" :style="{ borderTopColor: card.color }">
        <div class="stat-icon">{{ card.icon }}</div>
        <div class="stat-val">{{ getCardValue(card.key) }}</div>
        <div class="stat-label">{{ card.label }}</div>
      </div>
    </div>

    <div class="section">
      <h2>最近订单</h2>
      <el-table :data="stats.recentOrders" border stripe size="small" style="width:100%">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="orderNo" label="订单号" width="200" />
        <el-table-column prop="courseTitle" label="课程" min-width="160" />
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
      </el-table>
    </div>
  </div>
</template>

<style scoped>
h1 { font-size: var(--font-size-xl); font-weight: 700; margin-bottom: var(--space-xl); }
h2 { font-size: var(--font-size-lg); font-weight: 600; margin-bottom: var(--space-md); }
.stat-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: var(--space-lg); margin-bottom: var(--space-2xl); }
.stat-card {
  background: white; border-radius: var(--radius-md); padding: var(--space-xl);
  border: 1px solid var(--color-border); border-top: 4px solid #409eff; text-align: center;
}
.stat-icon { font-size: 36px; margin-bottom: var(--space-sm); }
.stat-val { font-size: var(--font-size-2xl); font-weight: 700; color: var(--color-primary); }
.stat-label { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: 4px; }
.section { background: white; border-radius: var(--radius-md); padding: var(--space-xl); border: 1px solid var(--color-border); }
</style>

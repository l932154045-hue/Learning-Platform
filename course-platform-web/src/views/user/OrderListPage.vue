<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { orderApi } from '@/api/modules/order'
import { paymentApi } from '@/api/modules/payment'
import type { OrderDetailVO } from '@shared/types'

const router = useRouter()
const orders = ref<OrderDetailVO[]>([])
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await orderApi.getList()
    orders.value = res.data.data ?? []
  } finally { loading.value = false }
})

function viewDetail(id: number) { router.push(`/order/${id}`) }
function getStatusTag(status: number) {
  const map: Record<number, { type: string; text: string }> = {
    0: { type: 'warning', text: '待支付' },
    1: { type: 'success', text: '已支付' },
    2: { type: 'info', text: '已取消' },
    3: { type: 'info', text: '已退款' },
  }
  return map[status] || { type: 'info', text: '未知' }
}

async function handleCancel(id: number) {
  try {
    await orderApi.cancel(id)
    const o = orders.value.find(o => o.id === id)
    if (o) o.status = 2
  } catch { /* handled */ }
}

async function handlePay(id: number) {
  try {
    await paymentApi.pay(id)
    router.push(`/pay-result/${id}`)
  } catch { /* handled */ }
}
</script>

<template>
  <div class="order-page">
    <AppHeader />
    <div class="container page-section">
      <h1 class="page-title">我的订单</h1>
      <div v-if="loading"><el-skeleton :rows="6" animated /></div>
      <template v-else-if="orders.length > 0">
        <div v-for="order in orders" :key="order.id" class="order-card">
          <div class="order-head">
            <span class="order-no">{{ order.orderNo }}</span>
            <el-tag :type="getStatusTag(order.status).type as any" size="small">
              {{ getStatusTag(order.status).text }}
            </el-tag>
            <span class="order-date">{{ order.createdAt?.slice(0, 10) }}</span>
          </div>
          <div v-for="item in order.orderItems" :key="item.id" class="order-item">
            <span>{{ item.courseTitle }}</span>
            <span>&yen;{{ item.price }}</span>
          </div>
          <div class="order-foot">
            <span>合计：<strong>&yen;{{ order.totalAmount }}</strong></span>
            <div class="order-actions">
              <el-button size="small" @click="viewDetail(order.id)">查看详情</el-button>
              <el-button v-if="order.status === 0" type="primary" size="small" @click="handlePay(order.id)">去支付</el-button>
              <el-button v-if="order.status === 0" size="small" @click="handleCancel(order.id)">取消</el-button>
            </div>
          </div>
        </div>
      </template>
      <EmptyState v-else description="暂无订单" />
    </div>
    <AppFooter />
  </div>
</template>

<style scoped>
.page-title { font-size: var(--font-size-2xl); font-weight: 700; margin-bottom: var(--space-xl); }
.order-card { background: white; border: 1px solid var(--color-border); border-radius: var(--radius-md); margin-bottom: var(--space-md); overflow: hidden; }
.order-head { display: flex; align-items: center; gap: var(--space-md); padding: var(--space-md) var(--space-lg); background: var(--color-surface-alt); }
.order-no { font-size: var(--font-size-sm); font-weight: 600; }
.order-date { font-size: var(--font-size-xs); color: var(--color-text-secondary); margin-left: auto; }
.order-item { display: flex; justify-content: space-between; padding: var(--space-md) var(--space-lg); border-bottom: 1px solid var(--color-border); font-size: var(--font-size-sm); }
.order-foot { display: flex; justify-content: space-between; align-items: center; padding: var(--space-md) var(--space-lg); }
.order-foot strong { color: var(--color-accent); font-size: var(--font-size-lg); }
.order-actions { display: flex; gap: var(--space-sm); }
</style>

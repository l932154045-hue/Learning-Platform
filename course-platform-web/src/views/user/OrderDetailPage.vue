<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { orderApi } from '@/api/modules/order'
import { paymentApi } from '@/api/modules/payment'
import type { OrderDetailVO } from '@shared/types'

const route = useRoute()
const router = useRouter()
const order = ref<OrderDetailVO | null>(null)
const loading = ref(true)

const orderId = Number(route.params.id)

onMounted(async () => {
  try {
    const res = await orderApi.getDetail(orderId)
    order.value = res.data.data ?? null
  } finally { loading.value = false }
})

async function handlePay() {
  try {
    await paymentApi.pay(orderId)
    ElMessage.success('支付成功')
    router.push(`/pay-result/${orderId}`)
  } catch { /* handled */ }
}

async function handleCancel() {
  try {
    await orderApi.cancel(orderId)
    if (order.value) order.value.status = 2
    ElMessage.success('已取消')
  } catch { /* handled */ }
}

function getStatusTag(status: number) {
  const map: Record<number, { type: string; text: string }> = {
    0: { type: 'warning', text: '待支付' },
    1: { type: 'success', text: '已支付' },
    2: { type: 'info', text: '已取消' },
  }
  return map[status] || { type: 'info', text: '未知' }
}
</script>

<template>
  <div class="detail-page">
    <AppHeader />
    <div class="container page-section">
      <el-button text @click="router.push('/orders')">← 返回订单列表</el-button>
      <div v-if="loading" class="mt"><el-skeleton :rows="8" animated /></div>
      <template v-else-if="order">
        <div class="order-detail">
          <div class="od-head">
            <h2>订单详情</h2>
            <el-tag :type="getStatusTag(order.status).type as any">{{ getStatusTag(order.status).text }}</el-tag>
          </div>
          <div class="od-info">
            <div class="od-row"><span>订单编号</span><span>{{ order.orderNo }}</span></div>
            <div class="od-row"><span>下单时间</span><span>{{ order.createdAt?.slice(0, 19) }}</span></div>
            <div class="od-row" v-if="order.paidAt"><span>支付时间</span><span>{{ order.paidAt?.slice(0, 19) }}</span></div>
          </div>
          <div class="od-items">
            <h4>订单商品</h4>
            <div v-for="item in order.orderItems" :key="item.id" class="od-item">
              <span>{{ item.courseTitle }}</span>
              <span>&yen;{{ item.price }}</span>
            </div>
          </div>
          <div class="od-total">
            <span>实付金额：</span>
            <strong>&yen;{{ order.totalAmount }}</strong>
          </div>
          <div class="od-actions" v-if="order.status === 0">
            <el-button type="primary" size="large" @click="handlePay">立即支付</el-button>
            <el-button size="large" @click="handleCancel">取消订单</el-button>
          </div>
        </div>
      </template>
    </div>
    <AppFooter />
  </div>
</template>

<style scoped>
.mt { margin-top: var(--space-lg); }
.order-detail { background: white; border: 1px solid var(--color-border); border-radius: var(--radius-md); overflow: hidden; margin-top: var(--space-md); }
.od-head { display: flex; align-items: center; gap: var(--space-md); padding: var(--space-lg); border-bottom: 1px solid var(--color-border); }
.od-head h2 { font-size: var(--font-size-xl); font-weight: 700; }
.od-info { padding: var(--space-lg); border-bottom: 1px solid var(--color-border); }
.od-row { display: flex; justify-content: space-between; padding: var(--space-xs) 0; font-size: var(--font-size-sm); }
.od-row span:first-child { color: var(--color-text-secondary); }
.od-items { padding: var(--space-lg); border-bottom: 1px solid var(--color-border); }
.od-items h4 { font-size: var(--font-size-base); font-weight: 600; margin-bottom: var(--space-md); }
.od-item { display: flex; justify-content: space-between; padding: var(--space-sm) 0; font-size: var(--font-size-sm); }
.od-total { display: flex; justify-content: flex-end; align-items: center; gap: var(--space-sm); padding: var(--space-lg); font-size: var(--font-size-base); }
.od-total strong { color: var(--color-accent); font-size: var(--font-size-2xl); }
.od-actions { display: flex; gap: var(--space-md); justify-content: flex-end; padding: var(--space-lg); border-top: 1px solid var(--color-border); }
</style>

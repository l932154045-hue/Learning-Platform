<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { paymentApi } from '@/api/modules/payment'
import type { PayResultVO } from '@shared/types'

const route = useRoute()
const router = useRouter()
const result = ref<PayResultVO | null>(null)
const loading = ref(true)

onMounted(async () => {
  try {
    const res = await paymentApi.getResult(Number(route.params.orderId))
    result.value = res.data.data ?? null
  } finally { loading.value = false }
})
</script>

<template>
  <div class="result-page">
    <AppHeader />
    <div class="container page-section">
      <div v-if="loading" class="result-card"><el-skeleton :rows="6" animated /></div>
      <template v-else-if="result">
        <div class="result-card" :class="result.status === 1 ? 'success' : 'fail'">
          <div class="result-icon">{{ result.status === 1 ? '✅' : '❌' }}</div>
          <h2>{{ result.status === 1 ? '支付成功' : '支付失败' }}</h2>
          <div class="result-info">
            <div class="ri-row"><span>支付单号</span><span>{{ result.paymentNo }}</span></div>
            <div class="ri-row"><span>订单编号</span><span>{{ result.orderNo }}</span></div>
            <div class="ri-row"><span>支付金额</span><span class="ri-price">&yen;{{ result.amount }}</span></div>
            <div class="ri-row" v-if="result.paidAt"><span>支付时间</span><span>{{ result.paidAt.slice(0, 19) }}</span></div>
          </div>
          <div class="result-actions">
            <el-button type="primary" @click="router.push('/my-learning')">开始学习</el-button>
            <el-button @click="router.push('/orders')">查看订单</el-button>
          </div>
        </div>
      </template>
    </div>
    <AppFooter />
  </div>
</template>

<style scoped>
.result-page { min-height: 100vh; display: flex; flex-direction: column; }
.container.page-section { flex: 1; display: flex; align-items: center; justify-content: center; }
.result-card { text-align: center; background: white; border-radius: var(--radius-lg); padding: var(--space-3xl); max-width: 480px; width: 100%; box-shadow: var(--shadow-card-hover); }
.result-icon { font-size: 64px; margin-bottom: var(--space-md); }
h2 { font-size: var(--font-size-xl); font-weight: 700; margin-bottom: var(--space-xl); }
.result-info { text-align: left; background: var(--color-surface); border-radius: var(--radius-sm); padding: var(--space-lg); margin-bottom: var(--space-xl); }
.ri-row { display: flex; justify-content: space-between; padding: var(--space-xs) 0; font-size: var(--font-size-sm); }
.ri-row span:first-child { color: var(--color-text-secondary); }
.ri-price { font-weight: 700; color: var(--color-accent); font-size: var(--font-size-lg); }
.result-actions { display: flex; gap: var(--space-md); justify-content: center; }
</style>

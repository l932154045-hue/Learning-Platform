<script setup lang="ts">
import { onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useCartStore } from '@/stores/cart'
import { orderApi } from '@/api/modules/order'

const router = useRouter()
const cartStore = useCartStore()
const checkedIds = computed(() => cartStore.items.map(i => i.courseId))

onMounted(() => { cartStore.fetchCart() })

async function handleRemove(courseId: number) {
  try {
    await cartStore.removeFromCart(courseId)
    ElMessage.success('已移出购物车')
  } catch { /* handled */ }
}

async function handleClear() {
  try {
    await ElMessageBox.confirm('确定清空购物车？', '提示', { type: 'warning' })
    await cartStore.clearCart()
    ElMessage.success('购物车已清空')
  } catch { /* cancelled */ }
}

async function handleCheckout(courseId: number) {
  try {
    const res = await orderApi.create(courseId)
    const orderId = res.data.data
    if (orderId) {
      await cartStore.removeFromCart(courseId)
      router.push(`/order/${orderId}`)
    }
  } catch { /* handled */ }
}
</script>

<template>
  <div class="cart-page">
    <AppHeader />
    <div class="container page-section">
      <h1 class="page-title">购物车</h1>
      <div v-if="cartStore.loading" class="sk-wrap">
        <el-skeleton :rows="4" animated />
      </div>
      <template v-else-if="cartStore.items.length > 0">
        <div class="cart-table">
          <div v-for="item in cartStore.items" :key="item.cartId" class="cart-row">
            <img :src="item.coverUrl || 'https://placehold.co/200x120/E8E6DD/1A1A2E?text=Course'" class="cart-cover" />
            <div class="cart-info">
              <h4>{{ item.courseTitle }}</h4>
              <p>{{ item.teacherName }}</p>
            </div>
            <div class="cart-price">&yen;{{ item.price }}</div>
            <div class="cart-actions">
              <el-button type="primary" size="small" @click="handleCheckout(item.courseId)">去结算</el-button>
              <el-button size="small" @click="handleRemove(item.courseId)">删除</el-button>
            </div>
          </div>
        </div>
        <div class="cart-footer">
          <el-button @click="handleClear">清空购物车</el-button>
          <span class="cart-total">合计：<strong>&yen;{{ cartStore.totalPrice }}</strong></span>
        </div>
      </template>
      <EmptyState v-else description="购物车是空的，去逛逛课程吧" />
    </div>
    <AppFooter />
  </div>
</template>

<style scoped>
.page-title { font-size: var(--font-size-2xl); font-weight: 700; margin-bottom: var(--space-xl); }
.cart-row {
  display: flex;
  align-items: center;
  gap: var(--space-lg);
  padding: var(--space-lg);
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  margin-bottom: var(--space-md);
}
.cart-cover { width: 160px; height: 96px; object-fit: cover; border-radius: var(--radius-sm); flex-shrink: 0; }
.cart-info { flex: 1; }
.cart-info h4 { font-size: var(--font-size-base); font-weight: 600; }
.cart-info p { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: 4px; }
.cart-price { font-size: var(--font-size-lg); font-weight: 700; color: var(--color-accent); white-space: nowrap; }
.cart-actions { display: flex; gap: var(--space-sm); flex-shrink: 0; }
.cart-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: var(--space-lg);
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
}
.cart-total { font-size: var(--font-size-lg); }
.cart-total strong { color: var(--color-accent); font-size: var(--font-size-xl); }
.sk-wrap { background: white; border-radius: var(--radius-md); padding: var(--space-lg); }
</style>

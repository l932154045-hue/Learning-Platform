import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { cartApi } from '@/api/modules/cart'
import type { CartItemVO } from '@shared/types'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItemVO[]>([])
  const loading = ref(false)

  const totalCount = computed(() => items.value.length)
  const totalPrice = computed(() => items.value.reduce((sum, item) => sum + item.price, 0))

  async function fetchCart() {
    loading.value = true
    try {
      const res = await cartApi.getList()
      items.value = res.data.data ?? []
    } finally {
      loading.value = false
    }
  }

  async function addToCart(courseId: number) {
    await cartApi.add(courseId)
    await fetchCart()
  }

  async function removeFromCart(courseId: number) {
    await cartApi.remove(courseId)
    items.value = items.value.filter((i) => i.courseId !== courseId)
  }

  async function clearCart() {
    await cartApi.clear()
    items.value = []
  }

  return { items, loading, totalCount, totalPrice, fetchCart, addToCart, removeFromCart, clearCart }
})

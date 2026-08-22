import { computed, ref } from 'vue'
import { defineStore } from 'pinia'
import { cartApi } from '@/api/cart'
import type { CartItem } from '@/types'

export const useCartStore = defineStore('cart', () => {
  const items = ref<CartItem[]>([])

  const count = computed(() => items.value.reduce((sum, item) => sum + item.quantity, 0))
  const selectedCount = computed(() =>
    items.value.filter((item) => item.selected === 1).reduce((sum, item) => sum + item.quantity, 0),
  )

  async function fetchCart() {
    items.value = await cartApi.list()
  }

  return { items, count, selectedCount, fetchCart }
})

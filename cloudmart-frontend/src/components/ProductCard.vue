<script setup lang="ts">
import { computed } from 'vue'
import type { Product } from '@/types'

const props = defineProps<{ product: Product }>()

const image = computed(() => {
  try {
    const list = JSON.parse(props.product.images || '[]')
    return list[0] || '/placeholder.png'
  } catch {
    return '/placeholder.png'
  }
})
</script>

<template>
  <router-link class="product-card" :to="`/products/${product.id}`">
    <img :src="image" :alt="product.name" />
    <h3>{{ product.name }}</h3>
    <div class="price">¥{{ product.price }}</div>
    <div class="meta">已售 {{ product.sales }} | 库存 {{ product.stock }}</div>
  </router-link>
</template>

<style scoped>
.product-card {
  display: block;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 8px;
  padding: 12px;
  color: inherit;
  text-decoration: none;
}
.product-card img {
  width: 100%;
  height: 160px;
  object-fit: cover;
  border-radius: 6px;
  background: #f3f4f6;
}
.price {
  color: #dc2626;
  font-weight: 700;
}
.meta {
  color: #6b7280;
  font-size: 13px;
}
</style>

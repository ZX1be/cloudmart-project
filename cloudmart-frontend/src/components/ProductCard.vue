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
    <div class="img-wrap">
      <img :src="image" :alt="product.name" loading="lazy" />
      <span v-if="product.stock <= 0" class="sold-out">已售罄</span>
    </div>
    <h3 class="name">{{ product.name }}</h3>
    <div class="meta">
      <span class="price">¥{{ product.price }}</span>
      <span class="sales">已售 {{ product.sales }}</span>
    </div>
  </router-link>
</template>

<style scoped>
.product-card {
  display: block;
  background: #fff;
  border: 1px solid var(--border);
  border-radius: var(--radius);
  padding: 12px;
  color: inherit;
  overflow: hidden;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow);
}

.img-wrap {
  position: relative;
  width: 100%;
  aspect-ratio: 1 / 1;
  border-radius: 8px;
  overflow: hidden;
  background: #f3f4f6;
}

.img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.product-card:hover .img-wrap img {
  transform: scale(1.05);
}

.sold-out {
  position: absolute;
  top: 8px;
  left: 8px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  padding: 2px 8px;
  border-radius: 999px;
}

.name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  margin: 10px 2px 6px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.meta {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  margin: 0 2px;
}

.price {
  font-size: 18px;
}

.sales {
  color: var(--text-2);
  font-size: 13px;
}
</style>

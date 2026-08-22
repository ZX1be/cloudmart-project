<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { cartApi } from '@/api/cart'
import { productApi } from '@/api/product'
import { useCartStore } from '@/stores/cart'
import type { Product } from '@/types'

const router = useRouter()
const cartStore = useCartStore()
const productMap = ref<Record<number, Product>>({})

async function load() {
  await cartStore.fetchCart()
  const entries = await Promise.all(
    cartStore.items.map(async (item) => [
      item.productId,
      await productApi.detail(item.productId),
    ] as const),
  )
  productMap.value = Object.fromEntries(entries)
}

async function updateQuantity(id: number, quantity: number) {
  await cartApi.updateQuantity(id, quantity)
  load()
}

async function toggleSelected(id: number, selected: number) {
  await cartApi.updateSelected(id, selected ? 1 : 0)
  load()
}

async function remove(id: number) {
  await cartApi.remove(id)
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>购物车</h2>
    <el-table :data="cartStore.items">
      <el-table-column label="选择">
        <template #default="{ row }">
          <el-checkbox
            :model-value="row.selected === 1"
            @change="(v: boolean) => toggleSelected(row.id, v ? 1 : 0)"
          />
        </template>
      </el-table-column>
      <el-table-column label="商品">
        <template #default="{ row }">{{ productMap[row.productId]?.name || row.productId }}</template>
      </el-table-column>
      <el-table-column label="数量">
        <template #default="{ row }">
          <el-input-number
            :model-value="row.quantity"
            :min="1"
            @change="(v: number) => updateQuantity(row.id, v)"
          />
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button type="danger" @click="remove(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>
    <el-button type="primary" @click="router.push('/checkout')">去结算</el-button>
  </div>
</template>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { favoriteApi, productApi } from '@/api/product'
import type { Favorite, Product } from '@/types'

const favorites = ref<Favorite[]>([])
const productMap = ref<Record<number, Product>>({})

async function load() {
  favorites.value = await favoriteApi.list()
  const entries = await Promise.all(
    favorites.value.map(
      async (favorite) => [favorite.productId, await productApi.detail(favorite.productId)] as const,
    ),
  )
  productMap.value = Object.fromEntries(entries)
}

async function remove(productId: number) {
  await favoriteApi.remove(productId)
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <h2>我的收藏</h2>
    <el-empty v-if="favorites.length === 0" description="还没有收藏任何商品" />
    <el-table v-else :data="favorites">
      <el-table-column label="商品">
        <template #default="{ row }">
          <router-link :to="`/products/${row.productId}`">
            {{ productMap[row.productId]?.name || `商品 #${row.productId}` }}
          </router-link>
        </template>
      </el-table-column>
      <el-table-column label="价格" width="120">
        <template #default="{ row }">
          ¥{{ productMap[row.productId]?.price }}
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="收藏时间" width="200" />
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button size="small" type="danger" @click="remove(row.productId)">
            取消收藏
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

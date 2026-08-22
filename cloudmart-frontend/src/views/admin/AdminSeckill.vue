<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'
import { productApi } from '@/api/product'
import type { Product, SeckillActivity } from '@/types'

const activities = ref<SeckillActivity[]>([])
const productMap = ref<Record<number, Product>>({})
const warmingId = ref<number | null>(null)

async function load() {
  activities.value = await adminApi.seckillActivities()
  const entries = await Promise.all(
    activities.value.map(
      async (activity) => [activity.productId, await productApi.detail(activity.productId)] as const,
    ),
  )
  productMap.value = Object.fromEntries(entries)
}

async function warmUp(activity: SeckillActivity) {
  warmingId.value = activity.id
  try {
    await adminApi.warmUpSeckill(activity.id)
  } finally {
    warmingId.value = null
  }
}

onMounted(load)
</script>

<template>
  <div>
    <h2>秒杀管理</h2>
    <el-table :data="activities" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商品" min-width="180">
        <template #default="{ row }">
          {{ productMap[row.productId]?.name || `商品 #${row.productId}` }}
        </template>
      </el-table-column>
      <el-table-column prop="seckillPrice" label="秒杀价" width="100" />
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column label="活动时间" min-width="240">
        <template #default="{ row }">
          {{ row.startTime }} ~ {{ row.endTime }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            size="small"
            type="warning"
            :loading="warmingId === row.id"
            @click="warmUp(row)"
          >
            预热库存
          </el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

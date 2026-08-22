<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addressApi } from '@/api/address'
import { productApi } from '@/api/product'
import { seckillApi } from '@/api/seckill'
import { useUserStore } from '@/stores/user'
import type { Address, Product, SeckillActivity } from '@/types'

const router = useRouter()
const userStore = useUserStore()
const activities = ref<SeckillActivity[]>([])
const productMap = ref<Record<number, Product>>({})
const addresses = ref<Address[]>([])
const addressId = ref<number | undefined>()
const buyingId = ref<number | null>(null)

async function load() {
  activities.value = await seckillApi.activities()
  const entries = await Promise.all(
    activities.value.map(
      async (activity) => [activity.productId, await productApi.detail(activity.productId)] as const,
    ),
  )
  productMap.value = Object.fromEntries(entries)
  if (userStore.token) {
    addresses.value = await addressApi.list()
    addressId.value = addresses.value[0]?.id
  }
}

async function buy(activity: SeckillActivity) {
  if (!userStore.token) {
    router.push('/login')
    return
  }
  if (!addressId.value) {
    return
  }
  buyingId.value = activity.id
  try {
    await seckillApi.buy(activity.id, addressId.value)
    router.push('/orders')
  } finally {
    buyingId.value = null
  }
}

onMounted(load)
</script>

<template>
  <div>
    <h2>秒杀活动</h2>
    <div v-if="userStore.token" class="address-select">
      <span>收货地址：</span>
      <el-select v-model="addressId" placeholder="选择收货地址">
        <el-option
          v-for="address in addresses"
          :key="address.id"
          :label="`${address.receiverName} ${address.province} ${address.city} ${address.detail}`"
          :value="address.id"
        />
      </el-select>
      <router-link to="/addresses">
        <el-button link>管理地址</el-button>
      </router-link>
    </div>

    <el-empty v-if="activities.length === 0" description="当前没有进行中的秒杀活动" />
    <el-row v-else :gutter="16">
      <el-col v-for="activity in activities" :key="activity.id" :span="8">
        <el-card class="seckill-card">
          <h3>{{ productMap[activity.productId]?.name || `商品 #${activity.productId}` }}</h3>
          <div class="price">
            <span class="now">¥{{ activity.seckillPrice }}</span>
            <span class="origin">¥{{ productMap[activity.productId]?.price }}</span>
          </div>
          <div class="stock">剩余库存：{{ activity.stock }}</div>
          <el-button
            type="danger"
            :loading="buyingId === activity.id"
            :disabled="activity.stock <= 0"
            @click="buy(activity)"
          >
            {{ activity.stock > 0 ? '立即秒杀' : '已售罄' }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<style scoped>
.address-select {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
}
.seckill-card {
  margin-bottom: 16px;
}
.price {
  display: flex;
  align-items: baseline;
  gap: 8px;
  margin: 8px 0;
}
.now {
  color: #dc2626;
  font-size: 24px;
  font-weight: 700;
}
.origin {
  color: #9ca3af;
  text-decoration: line-through;
}
.stock {
  color: #6b7280;
  margin-bottom: 12px;
}
</style>

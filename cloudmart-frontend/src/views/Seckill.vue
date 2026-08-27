<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
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
const now = ref(Date.now())
let timer: ReturnType<typeof setInterval> | undefined

function parseTime(s: string) {
  return new Date(s.replace(' ', 'T')).getTime()
}

function pad(n: number) {
  return String(n).padStart(2, '0')
}

function formatDiff(ms: number) {
  const s = Math.floor(ms / 1000)
  const h = Math.floor(s / 3600)
  const m = Math.floor((s % 3600) / 60)
  const sec = s % 60
  return `${pad(h)}:${pad(m)}:${pad(sec)}`
}

function countdown(activity: SeckillActivity) {
  const t = now.value
  const start = parseTime(activity.startTime)
  const end = parseTime(activity.endTime)
  if (t >= end) return { text: '已结束', type: 'info' as const }
  if (t < start) return { text: `距开始 ${formatDiff(start - t)}`, type: 'info' as const }
  return { text: `距结束 ${formatDiff(end - t)}`, type: 'danger' as const }
}

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
    ElMessage.warning('请先添加收货地址')
    return
  }
  if (countdown(activity).type === 'info' && countdown(activity).text === '已结束') {
    ElMessage.warning('活动已结束')
    return
  }
  buyingId.value = activity.id
  try {
    const msg = await seckillApi.buy(activity.id, addressId.value)
    ElMessage.success(msg || '排队成功，请稍后查询订单')
    router.push('/orders')
  } finally {
    buyingId.value = null
  }
}

onMounted(() => {
  load()
  timer = setInterval(() => (now.value = Date.now()), 1000)
})

onBeforeUnmount(() => {
  if (timer) clearInterval(timer)
})
</script>

<template>
  <div>
    <h2 style="margin-bottom: 16px">秒杀活动</h2>

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
      <el-col v-for="activity in activities" :key="activity.id" :xs="24" :sm="12" :md="8">
        <el-card class="seckill-card">
          <div class="img-wrap">
            <img
              :src="(() => {
                try {
                  return JSON.parse(productMap[activity.productId]?.images || '[]')[0] || '/placeholder.png'
                } catch {
                  return '/placeholder.png'
                }
              })()"
            />
          </div>
          <h3>{{ productMap[activity.productId]?.name || `商品 #${activity.productId}` }}</h3>
          <el-tag :type="countdown(activity).type" effect="dark" class="countdown">
            {{ countdown(activity).text }}
          </el-tag>
          <div class="price">
            <span class="now">¥{{ activity.seckillPrice }}</span>
            <span class="origin">¥{{ productMap[activity.productId]?.price }}</span>
          </div>
          <div class="stock">剩余库存：{{ activity.stock }}</div>
          <el-button
            type="danger"
            class="buy-btn"
            :loading="buyingId === activity.id"
            :disabled="activity.stock <= 0 || countdown(activity).text === '已结束'"
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
  text-align: center;
  margin-bottom: 16px;
}

.img-wrap {
  width: 100%;
  aspect-ratio: 4 / 3;
  border-radius: 8px;
  overflow: hidden;
  background: #f3f4f6;
  margin-bottom: 12px;
}

.img-wrap img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.countdown {
  margin: 6px 0;
}

.price {
  display: flex;
  align-items: baseline;
  justify-content: center;
  gap: 8px;
  margin: 8px 0;
}

.now {
  color: var(--price);
  font-size: 24px;
  font-weight: 700;
}

.origin {
  color: #9ca3af;
  text-decoration: line-through;
}

.stock {
  color: var(--text-2);
  margin-bottom: 12px;
}

.buy-btn {
  width: 100%;
}
</style>

<script setup lang="ts">
import { onBeforeUnmount, onMounted, ref } from 'vue'
import { orderApi } from '@/api/order'
import type { Order } from '@/types'

const orders = ref<Order[]>([])
const page = ref(1)
const polling = ref(false)
let pollTimer: ReturnType<typeof setInterval> | undefined

const statusText = (s: string) =>
  (
    {
      PENDING: '待支付',
      PAID: '待发货',
      SHIPPED: '已发货',
      RECEIVED: '已收货',
      COMPLETED: '已完成',
      CANCELLED: '已取消',
    } as Record<string, string>
  )[s] || s

async function load() {
  orders.value = (await orderApi.list({ page: page.value })).records
}

function stopPolling() {
  if (pollTimer) {
    clearInterval(pollTimer)
    pollTimer = undefined
  }
  polling.value = false
}

function startPolling() {
  polling.value = true
  let times = 0
  stopPolling()
  pollTimer = setInterval(async () => {
    await load()
    times++
    // 看到秒杀订单或轮询满 10 次（约 30 秒）就停
    if (orders.value.some((order) => order.orderNo.startsWith('SECKILL')) || times >= 10) {
      stopPolling()
    }
  }, 3000)
}

async function pay(id: number) {
  await orderApi.pay(id)
  load()
}

async function cancel(id: number) {
  await orderApi.cancel(id)
  load()
}

async function receive(id: number) {
  await orderApi.receive(id)
  load()
}

onMounted(async () => {
  await load()
  startPolling()
})
onBeforeUnmount(stopPolling)
</script>

<template>
  <div>
    <h2>我的订单</h2>
    <el-alert
      v-if="polling"
      type="info"
      title="正在确认秒杀订单，请稍候..."
      :closable="false"
    />
    <el-table :data="orders">
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="payAmount" label="实付" />
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PENDING' ? 'warning' : row.status === 'PAID' ? 'success' : 'info'">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <router-link :to="`/orders/${row.id}`">详情</router-link>
          <el-button v-if="row.status === 'PENDING'" @click="pay(row.id)">支付</el-button>
          <el-button v-if="row.status === 'PENDING'" @click="cancel(row.id)">取消</el-button>
          <el-button v-if="row.status === 'SHIPPED'" @click="receive(row.id)">确认收货</el-button>
        </template>
      </el-table-column>
    </el-table>
  </div>
</template>

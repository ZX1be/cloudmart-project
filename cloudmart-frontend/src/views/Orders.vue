<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { orderApi } from '@/api/order'
import type { Order } from '@/types'

const orders = ref<Order[]>([])
const page = ref(1)

async function load() {
  orders.value = (await orderApi.list({ page: page.value })).records
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

onMounted(load)
</script>

<template>
  <div>
    <h2>我的订单</h2>
    <el-table :data="orders">
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="payAmount" label="实付" />
      <el-table-column prop="status" label="状态" />
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

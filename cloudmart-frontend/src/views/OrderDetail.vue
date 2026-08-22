<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRoute } from 'vue-router'
import { orderApi } from '@/api/order'
import type { OrderVO } from '@/types'

const route = useRoute()
const order = ref<OrderVO | null>(null)

async function load() {
  order.value = await orderApi.detail(Number(route.params.id))
}

async function pay() {
  await orderApi.pay(order.value!.id)
  load()
}

async function cancel() {
  await orderApi.cancel(order.value!.id)
  load()
}

async function receive() {
  await orderApi.receive(order.value!.id)
  load()
}

onMounted(load)
</script>

<template>
  <div v-if="order">
    <h2>订单详情</h2>
    <el-card>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="订单号">{{ order.orderNo }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="order.status === 'PAID' ? 'success' : 'primary'">
            {{ order.status }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="商品总价">¥{{ order.totalAmount }}</el-descriptions-item>
        <el-descriptions-item label="优惠金额">¥{{ order.discountAmount }}</el-descriptions-item>
        <el-descriptions-item label="实付金额">¥{{ order.payAmount }}</el-descriptions-item>
        <el-descriptions-item label="下单时间">{{ order.createdAt }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-card>
      <h3>商品明细</h3>
      <el-table :data="order.items">
        <el-table-column prop="productName" label="商品" min-width="200" />
        <el-table-column prop="price" label="单价" width="100" />
        <el-table-column prop="quantity" label="数量" width="80" />
        <el-table-column prop="amount" label="小计" width="100" />
      </el-table>
    </el-card>

    <div class="actions">
      <el-button v-if="order.status === 'PENDING'" type="primary" @click="pay">支付</el-button>
      <el-button v-if="order.status === 'PENDING'" @click="cancel">取消订单</el-button>
      <el-button v-if="order.status === 'SHIPPED'" type="success" @click="receive">
        确认收货
      </el-button>
      <router-link to="/orders">
        <el-button>返回订单列表</el-button>
      </router-link>
    </div>
  </div>
</template>

<style scoped>
.actions {
  margin-top: 16px;
  display: flex;
  gap: 12px;
}
</style>

<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { adminApi } from '@/api/admin'
import AdminNav from '@/components/AdminNav.vue'
import Pagination from '@/components/Pagination.vue'
import type { Order } from '@/types'

const orders = ref<Order[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)

async function load() {
  const res = await adminApi.orders({ page: page.value, pageSize: pageSize.value })
  orders.value = res.records
  total.value = res.total
}

async function ship(id: number) {
  await adminApi.shipOrder(id)
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <AdminNav />
    <h2>订单管理</h2>
    <el-table :data="orders" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="orderNo" label="订单号" min-width="180" />
      <el-table-column prop="userId" label="用户ID" width="90" />
      <el-table-column prop="payAmount" label="实付" width="100" />
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 'PAID' ? 'success' : 'primary'">
            {{ row.status }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="下单时间" width="180" />
      <el-table-column label="操作" width="100">
        <template #default="{ row }">
          <el-button v-if="row.status === 'PAID'" size="small" type="primary" @click="ship(row.id)">
            发货
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      :page="page"
      :total="total"
      :page-size="pageSize"
      @change="page = $event; load()"
    />
  </div>
</template>

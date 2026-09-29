<script setup lang="ts">
import { useRoute } from 'vue-router'

const route = useRoute()

const items = [
  { path: '/admin/products', label: '商品管理' },
  { path: '/admin/categories', label: '分类管理' },
  { path: '/admin/orders', label: '订单管理' },
  { path: '/admin/coupons', label: '优惠券管理' },
  { path: '/admin/seckill', label: '秒杀管理' },
]
</script>

<template>
  <el-menu mode="horizontal" :default-active="route.path" router class="admin-nav">
    <el-menu-item v-for="item in items" :key="item.path" :index="item.path">
      {{ item.label }}
    </el-menu-item>
  </el-menu>
</template>

<style scoped>
.admin-nav {
  margin-bottom: 16px;
}
</style>

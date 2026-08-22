<script setup lang="ts">
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const loggedIn = computed(() => Boolean(userStore.token))

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <header class="header">
    <div class="header-inner">
      <router-link class="brand" to="/">CloudMart</router-link>
      <nav class="nav">
        <router-link to="/">首页</router-link>
        <router-link to="/seckill">秒杀</router-link>
        <template v-if="loggedIn">
          <router-link to="/cart">购物车</router-link>
          <router-link to="/orders">订单</router-link>
          <router-link to="/profile">个人中心</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin/products">管理后台</router-link>
          <button class="text-btn" @click="logout">退出</button>
        </template>
        <template v-else>
          <router-link to="/login">登录</router-link>
          <router-link to="/register">注册</router-link>
        </template>
      </nav>
    </div>
  </header>
</template>

<style scoped>
.header {
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}
.header-inner {
  max-width: 1200px;
  margin: 0 auto;
  padding: 0 16px;
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.brand {
  font-size: 20px;
  font-weight: 700;
  color: #2563eb;
  text-decoration: none;
}
.nav {
  display: flex;
  gap: 16px;
  align-items: center;
}
.nav a {
  color: #374151;
  text-decoration: none;
}
.text-btn {
  border: 0;
  background: transparent;
  color: #374151;
  cursor: pointer;
}
</style>

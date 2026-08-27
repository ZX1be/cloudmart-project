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
      <router-link class="brand" to="/">
        <span class="brand-dot" />
        CloudMart
      </router-link>

      <nav class="nav">
        <router-link to="/">首页</router-link>
        <router-link to="/seckill">秒杀</router-link>

        <template v-if="loggedIn">
          <router-link to="/cart">购物车</router-link>
          <router-link to="/orders">订单</router-link>
          <router-link to="/coupons">领券</router-link>
          <router-link to="/favorites">收藏</router-link>
          <router-link to="/profile">个人中心</router-link>
          <router-link v-if="userStore.isAdmin" to="/admin/products">管理后台</router-link>
          <span v-if="userStore.userInfo" class="username">{{ userStore.userInfo.username }}</span>
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
  position: sticky;
  top: 0;
  z-index: 100;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(8px);
  border-bottom: 1px solid var(--border);
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
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  color: var(--brand) !important;
}

.brand-dot {
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: linear-gradient(135deg, #3b82f6, #8b5cf6);
}

.nav {
  display: flex;
  gap: 18px;
  align-items: center;
}

.nav a {
  color: var(--text-2);
  font-size: 15px;
  padding: 6px 2px;
  border-bottom: 2px solid transparent;
  transition: color 0.2s;
}

.nav a:hover {
  color: var(--brand);
}

.nav a.router-link-exact-active,
.nav a.router-link-active {
  color: var(--brand);
  font-weight: 600;
  border-bottom-color: var(--brand);
}

.username {
  color: var(--text);
  font-size: 14px;
  font-weight: 600;
}

.text-btn {
  border: 0;
  background: transparent;
  color: var(--text-2);
  cursor: pointer;
  font-size: 14px;
}

.text-btn:hover {
  color: var(--price);
}

@media (max-width: 768px) {
  .nav {
    gap: 12px;
  }
  .nav a {
    font-size: 14px;
  }
}
</style>

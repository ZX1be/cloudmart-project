<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const form = reactive({ username: '', password: '' })

async function submit() {
  await userStore.login(form.username, form.password)
  await userStore.fetchInfo()
  router.push('/')
}
</script>

<template>
  <el-card class="auth-card">
    <h2>登录</h2>
    <el-form :model="form" label-width="70px">
      <el-form-item label="用户名">
        <el-input v-model="form.username" />
      </el-form-item>
      <el-form-item label="密码">
        <el-input v-model="form.password" type="password" show-password />
      </el-form-item>
      <el-button type="primary" class="submit" @click="submit">登录</el-button>
      <router-link to="/register">没有账号？去注册</router-link>
    </el-form>
  </el-card>
</template>

<style scoped>
.auth-card {
  max-width: 420px;
  margin: 40px auto;
}
.submit {
  width: 100%;
  margin-bottom: 12px;
}
</style>

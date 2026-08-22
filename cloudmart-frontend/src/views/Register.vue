<script setup lang="ts">
import { reactive } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api/auth'

const router = useRouter()
const form = reactive({ username: '', password: '', phone: '' })

async function submit() {
  await authApi.register(form)
  router.push('/login')
}
</script>

<template>
  <el-card class="auth-card">
    <h2>注册</h2>
    <el-form :model="form" label-width="70px">
      <el-form-item label="用户名"><el-input v-model="form.username" /></el-form-item>
      <el-form-item label="密码"><el-input v-model="form.password" type="password" /></el-form-item>
      <el-form-item label="手机号"><el-input v-model="form.phone" /></el-form-item>
      <el-button type="primary" class="submit" @click="submit">注册</el-button>
      <router-link to="/login">已有账号？去登录</router-link>
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

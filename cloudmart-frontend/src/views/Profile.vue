<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { userApi } from '@/api/user'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()
const saving = ref(false)
const form = reactive({
  phone: '',
  email: '',
  avatar: '',
})

onMounted(async () => {
  await userStore.fetchInfo()
  if (userStore.userInfo) {
    form.phone = userStore.userInfo.phone || ''
    form.email = userStore.userInfo.email || ''
    form.avatar = userStore.userInfo.avatar || ''
  }
})

async function save() {
  saving.value = true
  try {
    await userApi.update({
      phone: form.phone,
      email: form.email,
      avatar: form.avatar,
    })
    await userStore.fetchInfo()
  } finally {
    saving.value = false
  }
}

function logout() {
  userStore.logout()
  router.push('/login')
}
</script>

<template>
  <el-card v-if="userStore.userInfo" class="profile">
    <h2>个人中心</h2>
    <el-descriptions :column="2" border>
      <el-descriptions-item label="用户名">{{ userStore.userInfo.username }}</el-descriptions-item>
      <el-descriptions-item label="角色">
        <el-tag :type="userStore.userInfo.role === 'ADMIN' ? 'danger' : 'success'">
          {{ userStore.userInfo.role }}
        </el-tag>
      </el-descriptions-item>
      <el-descriptions-item label="手机号">
        {{ userStore.userInfo.phone || '-' }}
      </el-descriptions-item>
      <el-descriptions-item label="邮箱">
        {{ userStore.userInfo.email || '-' }}
      </el-descriptions-item>
    </el-descriptions>

    <h3>编辑资料</h3>
    <el-form :model="form" label-width="80px" class="edit-form">
      <el-form-item label="手机号">
        <el-input v-model="form.phone" />
      </el-form-item>
      <el-form-item label="邮箱">
        <el-input v-model="form.email" />
      </el-form-item>
      <el-form-item label="头像URL">
        <el-input v-model="form.avatar" />
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="saving" @click="save">保存</el-button>
        <el-button @click="logout">退出登录</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<style scoped>
.profile {
  max-width: 720px;
  margin: 0 auto;
}
.edit-form {
  margin-top: 20px;
  max-width: 480px;
}
</style>

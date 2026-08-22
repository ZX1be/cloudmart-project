<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { addressApi, type AddressForm } from '@/api/address'
import type { Address } from '@/types'

const addresses = ref<Address[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive<AddressForm>({
  receiverName: '',
  phone: '',
  province: '',
  city: '',
  district: '',
  detail: '',
  isDefault: 0,
})

async function load() {
  addresses.value = await addressApi.list()
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    receiverName: '',
    phone: '',
    province: '',
    city: '',
    district: '',
    detail: '',
    isDefault: 0,
  })
  dialogVisible.value = true
}

function openEdit(address: Address) {
  editingId.value = address.id
  Object.assign(form, {
    receiverName: address.receiverName,
    phone: address.phone,
    province: address.province,
    city: address.city,
    district: address.district,
    detail: address.detail,
    isDefault: address.isDefault,
  })
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await addressApi.update(editingId.value, form)
  } else {
    await addressApi.save(form)
  }
  dialogVisible.value = false
  load()
}

async function remove(id: number) {
  await addressApi.remove(id)
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>收货地址</h2>
      <el-button type="primary" @click="openCreate">新增地址</el-button>
    </div>

    <el-table :data="addresses">
      <el-table-column prop="receiverName" label="收货人" width="120" />
      <el-table-column prop="phone" label="联系电话" width="140" />
      <el-table-column label="地址">
        <template #default="{ row }">
          {{ row.province }} {{ row.city }} {{ row.district }} {{ row.detail }}
        </template>
      </el-table-column>
      <el-table-column label="默认" width="80">
        <template #default="{ row }">
          <el-tag v-if="row.isDefault === 1" type="success">默认</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="160">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除该地址？" @confirm="remove(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑地址' : '新增地址'" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="收货人">
          <el-input v-model="form.receiverName" />
        </el-form-item>
        <el-form-item label="联系电话">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="省份">
          <el-input v-model="form.province" />
        </el-form-item>
        <el-form-item label="城市">
          <el-input v-model="form.city" />
        </el-form-item>
        <el-form-item label="区/县">
          <el-input v-model="form.district" />
        </el-form-item>
        <el-form-item label="详细地址">
          <el-input v-model="form.detail" />
        </el-form-item>
        <el-form-item label="设为默认">
          <el-switch v-model="form.isDefault" :active-value="1" :inactive-value="0" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submit">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}
</style>

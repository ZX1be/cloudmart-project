<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { adminApi } from '@/api/admin'
import Pagination from '@/components/Pagination.vue'
import type { Product } from '@/types'

const products = ref<Product[]>([])
const total = ref(0)
const page = ref(1)
const pageSize = ref(10)
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({
  name: '',
  description: '',
  price: 0,
  stock: 0,
  categoryId: 1,
  images: '',
  status: 1,
})

async function load() {
  const res = await adminApi.products({ page: page.value, pageSize: pageSize.value })
  products.value = res.records
  total.value = res.total
}

function openCreate() {
  editingId.value = null
  Object.assign(form, {
    name: '',
    description: '',
    price: 0,
    stock: 0,
    categoryId: 1,
    images: '',
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(product: Product) {
  editingId.value = product.id
  Object.assign(form, {
    name: product.name,
    description: product.description || '',
    price: product.price,
    stock: product.stock,
    categoryId: product.categoryId,
    images: product.images || '',
    status: product.status,
  })
  dialogVisible.value = true
}

async function submit() {
  const data = { ...form }
  if (editingId.value) {
    await adminApi.updateProduct(editingId.value, data)
  } else {
    await adminApi.createProduct(data)
  }
  dialogVisible.value = false
  load()
}

async function remove(id: number) {
  await adminApi.deleteProduct(id)
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>商品管理</h2>
      <el-button type="primary" @click="openCreate">添加商品</el-button>
    </div>

    <el-table :data="products" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="商品名称" min-width="180" />
      <el-table-column prop="price" label="价格" width="100" />
      <el-table-column prop="stock" label="库存" width="80" />
      <el-table-column prop="sales" label="销量" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '上架' : '下架' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="180">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除该商品？" @confirm="remove(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <Pagination
      :page="page"
      :total="total"
      :page-size="pageSize"
      @change="page = $event; load()"
    />

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑商品' : '添加商品'" width="520px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" type="textarea" />
        </el-form-item>
        <el-form-item label="价格">
          <el-input-number v-model="form.price" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="0" />
        </el-form-item>
        <el-form-item label="分类ID">
          <el-input-number v-model="form.categoryId" :min="1" />
        </el-form-item>
        <el-form-item label="图片">
          <el-input v-model="form.images" placeholder='JSON 数组，如 ["http://img.png"]' />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">上架</el-radio>
            <el-radio :value="0">下架</el-radio>
          </el-radio-group>
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

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { adminApi } from '@/api/admin'
import AdminNav from '@/components/AdminNav.vue'
import { productApi } from '@/api/product'
import type { Product, SeckillActivity } from '@/types'

const activities = ref<SeckillActivity[]>([])
const products = ref<Product[]>([])
const productMap = ref<Record<number, Product>>({})
const warmingId = ref<number | null>(null)
const dialogVisible = ref(false)
const saving = ref(false)
const form = reactive({
  productId: undefined as number | undefined,
  seckillPrice: 0,
  stock: 10,
  startTime: '',
  endTime: '',
  status: 1,
})

async function load() {
  activities.value = await adminApi.seckillActivities()
  const entries = await Promise.all(
    activities.value.map(
      async (activity) => [activity.productId, await productApi.detail(activity.productId)] as const,
    ),
  )
  productMap.value = Object.fromEntries(entries)
  const res = await adminApi.products({ page: 1, pageSize: 100 })
  products.value = res.records
}

async function warmUp(activity: SeckillActivity) {
  warmingId.value = activity.id
  try {
    await adminApi.warmUpSeckill(activity.id)
  } finally {
    warmingId.value = null
  }
}

function openCreate() {
  Object.assign(form, {
    productId: undefined,
    seckillPrice: 0,
    stock: 10,
    startTime: '',
    endTime: '',
    status: 1,
  })
  dialogVisible.value = true
}

async function submit() {
  if (!form.productId || !form.startTime || !form.endTime) {
    ElMessage.warning('请选择商品并填写活动时间')
    return
  }
  saving.value = true
  try {
    await adminApi.createSeckill(form)
    ElMessage.success('秒杀活动创建成功')
    dialogVisible.value = false
    load()
  } finally {
    saving.value = false
  }
}

onMounted(load)
</script>

<template>
  <div>
    <AdminNav />
    <div class="toolbar">
      <h2>秒杀管理</h2>
      <el-button type="primary" @click="openCreate">新增秒杀活动</el-button>
    </div>
    <el-table :data="activities" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="商品" min-width="180">
        <template #default="{ row }">
          {{ productMap[row.productId]?.name || `商品 #${row.productId}` }}
        </template>
      </el-table-column>
      <el-table-column prop="seckillPrice" label="秒杀价" width="100" />
      <el-table-column prop="stock" label="设定库存" width="90" />
      <el-table-column label="实时库存" width="90">
        <template #default="{ row }">
          <el-tag v-if="row.realStock !== null && row.realStock !== undefined" :type="row.realStock > 0 ? 'success' : 'danger'">
            {{ row.realStock }}
          </el-tag>
          <span v-else>-</span>
        </template>
      </el-table-column>
      <el-table-column label="活动时间" min-width="240">
        <template #default="{ row }">
          {{ row.startTime }} ~ {{ row.endTime }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '启用' : '停用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="120">
        <template #default="{ row }">
          <el-button
            size="small"
            type="warning"
            :loading="warmingId === row.id"
            @click="warmUp(row)"
          >
            预热库存
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增秒杀活动" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="商品">
          <el-select v-model="form.productId" placeholder="选择商品">
            <el-option v-for="p in products" :key="p.id" :label="p.name" :value="p.id" />
          </el-select>
        </el-form-item>
        <el-form-item label="秒杀价">
          <el-input-number v-model="form.seckillPrice" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="库存">
          <el-input-number v-model="form.stock" :min="1" />
        </el-form-item>
        <el-form-item label="开始时间">
          <el-date-picker
            v-model="form.startTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择开始时间"
          />
        </el-form-item>
        <el-form-item label="结束时间">
          <el-date-picker
            v-model="form.endTime"
            type="datetime"
            value-format="YYYY-MM-DD HH:mm:ss"
            placeholder="选择结束时间"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">启用</el-radio>
            <el-radio :value="0">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="saving" @click="submit">保存</el-button>
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

<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { adminApi } from '@/api/admin'
import type { Coupon } from '@/types'

const coupons = ref<Coupon[]>([])
const dialogVisible = ref(false)
const form = reactive({
  name: '',
  type: 'FIXED',
  discountValue: 0,
  minAmount: 0,
  totalCount: 100,
  startTime: '',
  endTime: '',
})

async function load() {
  coupons.value = await adminApi.coupons()
}

function openCreate() {
  Object.assign(form, {
    name: '',
    type: 'FIXED',
    discountValue: 0,
    minAmount: 0,
    totalCount: 100,
    startTime: '',
    endTime: '',
  })
  dialogVisible.value = true
}

async function submit() {
  await adminApi.createCoupon(form)
  dialogVisible.value = false
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>优惠券管理</h2>
      <el-button type="primary" @click="openCreate">新增优惠券</el-button>
    </div>

    <el-table :data="coupons" border>
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column prop="name" label="名称" min-width="160" />
      <el-table-column prop="type" label="类型" width="90" />
      <el-table-column prop="discountValue" label="优惠值" width="100" />
      <el-table-column prop="minAmount" label="最低消费" width="110" />
      <el-table-column label="领取情况" width="120">
        <template #default="{ row }">{{ row.usedCount }} / {{ row.totalCount }}</template>
      </el-table-column>
      <el-table-column label="有效期" min-width="220">
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
    </el-table>

    <el-dialog v-model="dialogVisible" title="新增优惠券" width="480px">
      <el-form :model="form" label-width="90px">
        <el-form-item label="名称">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="类型">
          <el-radio-group v-model="form.type">
            <el-radio value="FIXED">满减</el-radio>
            <el-radio value="PERCENT">折扣</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="优惠值">
          <el-input-number v-model="form.discountValue" :min="0" :precision="2" />
          <span class="tip">{{ form.type === 'FIXED' ? '减免金额（元）' : '折扣（如 80 表示 8 折）' }}</span>
        </el-form-item>
        <el-form-item label="最低消费">
          <el-input-number v-model="form.minAmount" :min="0" :precision="2" />
        </el-form-item>
        <el-form-item label="发行量">
          <el-input-number v-model="form.totalCount" :min="1" />
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
.tip {
  margin-left: 8px;
  color: #9ca3af;
  font-size: 12px;
}
</style>

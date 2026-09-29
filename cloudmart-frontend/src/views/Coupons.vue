<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { ElMessage } from 'element-plus'
import { couponApi } from '@/api/coupon'
import type { Coupon, UserCoupon } from '@/types'

const available = ref<Coupon[]>([])
const mine = ref<UserCoupon[]>([])
const claimingId = ref<number | null>(null)

const claimedIds = computed(() => new Set(mine.value.map((c) => c.couponId)))

async function load() {
  available.value = await couponApi.list()
  mine.value = await couponApi.mine()
}

async function claim(coupon: Coupon) {
  claimingId.value = coupon.id
  try {
    await couponApi.claim(coupon.id)
    ElMessage.success('领取成功')
    load()
  } finally {
    claimingId.value = null
  }
}

function typeLabel(coupon: Coupon) {
  return coupon.type === 'FIXED' ? `满${coupon.minAmount}减${coupon.discountValue}元` : `${coupon.discountValue}折`
}

function statusText(status: string) {
  return { UNUSED: '未使用', USED: '已使用', EXPIRED: '已过期' }[status] || status
}

onMounted(load)
</script>

<template>
  <div>
    <h2 style="margin-bottom: 16px">领券中心</h2>

    <h3>可领取</h3>
    <el-empty v-if="available.length === 0" description="暂无可领取的优惠券" />
    <el-row v-else :gutter="16">
      <el-col v-for="coupon in available" :key="coupon.id" :xs="24" :sm="12" :md="8">
        <el-card class="coupon-card">
          <div class="coupon-head">
            <span class="coupon-label">{{ typeLabel(coupon) }}</span>
            <el-tag :type="coupon.type === 'FIXED' ? 'danger' : 'warning'">
              {{ coupon.type === 'FIXED' ? '满减' : '折扣' }}
            </el-tag>
          </div>
          <div class="coupon-desc">
            全店可用 · 最低消费 ¥{{ coupon.minAmount }}
          </div>
          <div class="coupon-time">
            {{ coupon.startTime }} ~ {{ coupon.endTime }}
          </div>
          <el-button
            type="primary"
            class="claim-btn"
            :loading="claimingId === coupon.id"
            :disabled="claimedIds.has(coupon.id) || coupon.usedCount >= coupon.totalCount"
            @click="claim(coupon)"
          >
            {{ claimedIds.has(coupon.id) ? '已领取' : coupon.usedCount >= coupon.totalCount ? '已领完' : '领取' }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>

    <h3 style="margin-top: 24px">我的优惠券</h3>
    <el-empty v-if="mine.length === 0" description="你还没有领取优惠券" />
    <el-table v-else :data="mine">
      <el-table-column prop="id" label="ID" width="70" />
      <el-table-column label="优惠券" min-width="180">
        <template #default="{ row }">
          {{ available.find((c) => c.id === row.couponId)?.name || `券 #${row.couponId}` }}
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 'UNUSED' ? 'success' : 'info'">
            {{ statusText(row.status) }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createdAt" label="领取时间" width="180" />
    </el-table>
  </div>
</template>

<style scoped>
.coupon-card {
  margin-bottom: 16px;
}
.coupon-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.coupon-label {
  font-size: 20px;
  font-weight: 700;
  color: var(--price);
}
.coupon-desc {
  color: var(--text-2);
  margin: 8px 0 4px;
}
.coupon-time {
  color: var(--text-2);
  font-size: 13px;
  margin-bottom: 12px;
}
.claim-btn {
  width: 100%;
}
</style>

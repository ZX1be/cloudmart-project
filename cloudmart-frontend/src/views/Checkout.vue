<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addressApi } from '@/api/address'
import { couponApi } from '@/api/coupon'
import { orderApi } from '@/api/order'
import { useCartStore } from '@/stores/cart'
import type { Address, UserCoupon } from '@/types'

const router = useRouter()
const cartStore = useCartStore()
const addresses = ref<Address[]>([])
const coupons = ref<UserCoupon[]>([])
const addressId = ref<number>()
const userCouponId = ref<number | null>(null)

async function submit() {
  await orderApi.create({
    addressId: addressId.value!,
    userCouponId: userCouponId.value,
  })
  router.push('/orders')
}

onMounted(async () => {
  await cartStore.fetchCart()
  addresses.value = await addressApi.list()
  coupons.value = await couponApi.mine()
  addressId.value = addresses.value[0]?.id
})
</script>

<template>
  <div>
    <h2>确认订单</h2>
    <el-card>
      <h3>收货地址</h3>
      <el-radio-group v-model="addressId">
        <el-radio v-for="address in addresses" :key="address.id" :value="address.id">
          {{ address.receiverName }} {{ address.province }} {{ address.city }} {{ address.detail }}
        </el-radio>
      </el-radio-group>
    </el-card>
    <el-card>
      <h3>优惠券</h3>
      <el-select v-model="userCouponId" clearable placeholder="不使用优惠券">
        <el-option
          v-for="coupon in coupons"
          :key="coupon.id"
          :label="`${coupon.id}号优惠券（${coupon.status}）`"
          :value="coupon.id"
        />
      </el-select>
    </el-card>
    <el-button type="primary" :disabled="!addressId" @click="submit">提交订单</el-button>
  </div>
</template>

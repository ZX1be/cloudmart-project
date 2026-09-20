<script setup lang="ts">
import { computed, onMounted, ref } from 'vue'
import { useRouter } from 'vue-router'
import { addressApi } from '@/api/address'
import { couponApi } from '@/api/coupon'
import { orderApi } from '@/api/order'
import { ApiError } from '@/api/request'
import { useCartStore } from '@/stores/cart'
import type { Address, CartItem, UserCoupon } from '@/types'

interface CheckoutContext {
  idempotencyKey: string
  cartFingerprint: string
}

const CHECKOUT_CONTEXT_KEY = 'checkout:context'

const router = useRouter()
const cartStore = useCartStore()

const addresses = ref<Address[]>([])
const coupons = ref<UserCoupon[]>([])
const addressId = ref<number>()
const userCouponId = ref<number | null>(null)
const checkoutContext = ref<CheckoutContext>()
const submitting = ref(false)
const hasSelectedCart = computed(() =>
  cartStore.items.some((item) => item.selected === 1),
)

function createIdempotencyKey() {
  if (typeof crypto.randomUUID === 'function') {
    return crypto.randomUUID()
  }

  return `${Date.now()}-${Math.random().toString(36).slice(2)}`
}

async function sha256(value: string) {
  const data = new TextEncoder().encode(value)
  const hash = await crypto.subtle.digest('SHA-256', data)

  return Array.from(new Uint8Array(hash))
    .map((byte) => byte.toString(16).padStart(2, '0'))
    .join('')
}

function buildCartFingerprint(items: CartItem[]) {
  const canonical = items
    .filter((item) => item.selected === 1)
    .sort((a, b) => a.productId - b.productId)
    .map((item) => `${item.productId}:${item.quantity}`)
    .join('\n')

  return sha256(canonical)
}

function readCheckoutContext(): CheckoutContext | undefined {
  const value = sessionStorage.getItem(CHECKOUT_CONTEXT_KEY)
  if (!value) {
    return undefined
  }

  try {
    const context = JSON.parse(value) as CheckoutContext
    if (context.idempotencyKey && context.cartFingerprint) {
      return context
    }
  } catch {
    sessionStorage.removeItem(CHECKOUT_CONTEXT_KEY)
  }

  return undefined
}

async function initializeCheckoutContext() {
  const saved = readCheckoutContext()
  if (saved) {
    checkoutContext.value = saved
    return
  }

  const context: CheckoutContext = {
    idempotencyKey: createIdempotencyKey(),
    cartFingerprint: await buildCartFingerprint(cartStore.items),
  }

  sessionStorage.setItem(CHECKOUT_CONTEXT_KEY, JSON.stringify(context))
  checkoutContext.value = context
}

async function submit() {
  if (!addressId.value || !checkoutContext.value || submitting.value) {
    return
  }

  submitting.value = true

  try {
    const order = await orderApi.create(
        {
          addressId: addressId.value,
          cartFingerprint: checkoutContext.value.cartFingerprint,
          userCouponId: userCouponId.value,
        },
        checkoutContext.value.idempotencyKey,
    )

    sessionStorage.removeItem(CHECKOUT_CONTEXT_KEY)

    router.push(`/orders/${order.id}`)
  } catch (error) {
    if (error instanceof ApiError && error.code === 409) {
      sessionStorage.removeItem(CHECKOUT_CONTEXT_KEY)
      checkoutContext.value = undefined

      await cartStore.fetchCart()
      if (!hasSelectedCart.value) {
        router.push('/orders')
        return
      }

      await initializeCheckoutContext()
    } else {
      throw error
    }
  } finally {
    submitting.value = false
  }
}

onMounted(async () => {
  await cartStore.fetchCart()
  await initializeCheckoutContext()
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
    <el-button
        type="primary"
        :loading="submitting"
        :disabled="!addressId || !checkoutContext || !hasSelectedCart || submitting"
        @click="submit"
    >
      提交订单
    </el-button>
  </div>
</template>

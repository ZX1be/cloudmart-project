<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { favoriteApi, productApi } from '@/api/product'
import { cartApi } from '@/api/cart'
import { useUserStore } from '@/stores/user'
import type { Product, Review } from '@/types'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const product = ref<Product | null>(null)
const quantity = ref(1)
const reviews = ref<Review[]>([])
const reviewForm = reactive({ rating: 5, content: '' })

const productId = Number(route.params.id)

async function load() {
  product.value = await productApi.detail(productId)
  reviews.value = (await productApi.reviews(productId)).records
}

async function addToCart() {
  await cartApi.add(productId, quantity.value)
  router.push('/cart')
}

async function addFavorite() {
  await favoriteApi.add(productId)
}

async function submitReview() {
  await productApi.addReview(productId, reviewForm)
  reviewForm.content = ''
  load()
}

onMounted(load)
</script>

<template>
  <div v-if="product" class="detail">
    <div class="main">
      <img :src="(JSON.parse(product.images || '[]') as string[])[0] || '/placeholder.png'" />
      <div class="info">
        <h1>{{ product.name }}</h1>
        <p>{{ product.description }}</p>
        <div class="price">¥{{ product.price }}</div>
        <div class="actions">
          <el-input-number v-model="quantity" :min="1" :max="product.stock" />
          <el-button type="primary" @click="addToCart">加入购物车</el-button>
          <el-button @click="addFavorite">收藏</el-button>
        </div>
      </div>
    </div>
    <el-card v-if="userStore.token">
      <h3>写评价</h3>
      <el-rate v-model="reviewForm.rating" />
      <el-input v-model="reviewForm.content" type="textarea" placeholder="评价内容" />
      <el-button type="primary" @click="submitReview">提交</el-button>
    </el-card>
    <el-card>
      <h3>评价列表</h3>
      <div v-for="review in reviews" :key="review.id" class="review">
        <el-rate :model-value="review.rating" disabled />
        <p>{{ review.content }}</p>
      </div>
    </el-card>
  </div>
</template>

<style scoped>
.main {
  display: grid;
  grid-template-columns: 360px 1fr;
  gap: 24px;
}
.main img {
  width: 100%;
  height: 360px;
  object-fit: cover;
  border-radius: 8px;
}
.price {
  color: #dc2626;
  font-size: 28px;
  font-weight: 700;
}
.actions {
  display: flex;
  gap: 12px;
  align-items: center;
}
.review {
  border-bottom: 1px solid #e5e7eb;
  padding: 12px 0;
}
</style>

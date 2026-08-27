<script setup lang="ts">
import { onMounted, ref } from 'vue'
import { categoryApi, productApi } from '@/api/product'
import ProductCard from '@/components/ProductCard.vue'
import Pagination from '@/components/Pagination.vue'
import type { Category, Product } from '@/types'

const keyword = ref('')
const categoryId = ref<number | undefined>()
const page = ref(1)
const pageSize = ref(20)
const total = ref(0)
const products = ref<Product[]>([])
const categories = ref<Category[]>([])
const loading = ref(false)

async function fetchProducts() {
  loading.value = true
  try {
    const res = await productApi.list({
      keyword: keyword.value,
      categoryId: categoryId.value,
      page: page.value,
      pageSize: pageSize.value,
    })
    products.value = res.records
    total.value = res.total
  } finally {
    loading.value = false
  }
}

async function fetchCategories() {
  categories.value = await categoryApi.tree()
}

function search() {
  page.value = 1
  fetchProducts()
}

function selectCategory(id: number) {
  categoryId.value = id === 0 ? undefined : id
  page.value = 1
  fetchProducts()
}

onMounted(() => {
  fetchProducts()
  fetchCategories()
})
</script>

<template>
  <div class="home">
    <section class="hero">
      <h1>云商城 · 好物集结</h1>
      <p>精选好物，一站式购物体验</p>
      <div class="search-bar">
        <el-input
          v-model="keyword"
          size="large"
          placeholder="搜索商品关键词"
          clearable
          @keyup.enter="search"
        />
        <el-button type="primary" size="large" @click="search">搜索</el-button>
      </div>
    </section>

    <div class="categories">
      <button class="cat" :class="{ active: !categoryId }" @click="selectCategory(0)">全部</button>
      <button
        v-for="cat in categories"
        :key="cat.id"
        class="cat"
        :class="{ active: categoryId === cat.id }"
        @click="selectCategory(cat.id)"
      >
        {{ cat.name }}
      </button>
    </div>

    <div v-loading="loading" class="grid">
      <ProductCard v-for="product in products" :key="product.id" :product="product" />
    </div>

    <el-empty v-if="!loading && products.length === 0" description="没有找到相关商品" />

    <Pagination
      v-if="total > 0"
      :page="page"
      :total="total"
      :page-size="pageSize"
      @change="page = $event; fetchProducts()"
    />
  </div>
</template>

<style scoped>
.hero {
  background: linear-gradient(135deg, #2563eb 0%, #7c3aed 100%);
  color: #fff;
  border-radius: var(--radius);
  padding: 40px 32px;
  margin-bottom: 20px;
}

.hero h1 {
  color: #fff;
  margin: 0 0 6px;
}

.hero p {
  color: rgba(255, 255, 255, 0.85);
  margin: 0 0 20px;
}

.search-bar {
  display: flex;
  gap: 12px;
  max-width: 520px;
}

.categories {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}

.cat {
  border: 1px solid var(--border);
  background: #fff;
  color: var(--text-2);
  padding: 6px 14px;
  border-radius: 999px;
  cursor: pointer;
  font-size: 14px;
  transition: all 0.2s;
}

.cat:hover {
  color: var(--brand);
  border-color: var(--brand);
}

.cat.active {
  background: var(--brand);
  color: #fff;
  border-color: var(--brand);
  font-weight: 600;
}

.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  min-height: 120px;
}

@media (max-width: 768px) {
  .hero {
    padding: 28px 20px;
  }
}
</style>

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

async function fetchProducts() {
  const res = await productApi.list({
    keyword: keyword.value,
    categoryId: categoryId.value,
    page: page.value,
    pageSize: pageSize.value,
  })
  products.value = res.records
  total.value = res.total
}

async function fetchCategories() {
  categories.value = await categoryApi.tree()
}

function search() {
  page.value = 1
  fetchProducts()
}

function selectCategory(id: number) {
  categoryId.value = id
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
    <div class="toolbar">
      <el-input v-model="keyword" placeholder="搜索商品" @keyup.enter="search" />
      <el-button type="primary" @click="search">搜索</el-button>
    </div>
    <div class="categories">
      <el-tag
        v-for="cat in categories"
        :key="cat.id"
        class="category-tag"
        @click="selectCategory(cat.id)"
      >
        {{ cat.name }}
      </el-tag>
    </div>
    <div class="grid">
      <ProductCard v-for="product in products" :key="product.id" :product="product" />
    </div>
    <Pagination
      :page="page"
      :total="total"
      :page-size="pageSize"
      @change="page = $event; fetchProducts()"
    />
  </div>
</template>

<style scoped>
.toolbar {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
}
.categories {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 16px;
}
.category-tag {
  cursor: pointer;
}
.grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 24px;
}
</style>

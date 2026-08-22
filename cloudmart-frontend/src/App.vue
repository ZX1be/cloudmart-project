<template>
  <Header />
  <main class="page">
    <router-view />
  </main>
</template>

<script setup lang="ts">
import Header from '@/components/Header.vue'
</script>

<style>
body {
  margin: 0;
  background: #f5f7fa;
  color: #1f2937;
}
.page {
  max-width: 1200px;
  margin: 0 auto;
  padding: 24px 16px 48px;
}
</style>

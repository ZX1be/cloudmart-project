<script setup lang="ts">
defineProps<{ page: number; total: number; pageSize: number }>()
const emit = defineEmits<{ change: [page: number] }>()
</script>

<template>
  <el-pagination
    background
    layout="prev, pager, next"
    :current-page="page"
    :page-size="pageSize"
    :total="total"
    @current-change="emit('change', $event)"
  />
</template>

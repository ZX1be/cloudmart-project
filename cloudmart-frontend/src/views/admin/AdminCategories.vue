<script setup lang="ts">
import { onMounted, reactive, ref } from 'vue'
import { adminApi } from '@/api/admin'
import { categoryApi } from '@/api/product'
import type { Category } from '@/types'

const categories = ref<Category[]>([])
const dialogVisible = ref(false)
const editingId = ref<number | null>(null)
const form = reactive({
  parentId: 0,
  name: '',
  icon: '',
  sortOrder: 0,
  status: 1,
})

function normalize(tree: Category[]): Category[] {
  return tree.map((category) => ({
    ...category,
    children: category.children ? normalize(category.children) : [],
  }))
}

async function load() {
  categories.value = normalize(await categoryApi.tree())
}

function openCreate(parentId = 0) {
  editingId.value = null
  Object.assign(form, {
    parentId,
    name: '',
    icon: '',
    sortOrder: 0,
    status: 1,
  })
  dialogVisible.value = true
}

function openEdit(category: Category) {
  editingId.value = category.id
  Object.assign(form, {
    parentId: category.parentId,
    name: category.name,
    icon: category.icon || '',
    sortOrder: category.sortOrder || 0,
    status: category.status ?? 1,
  })
  dialogVisible.value = true
}

async function submit() {
  if (editingId.value) {
    await adminApi.updateCategory(editingId.value, form)
  } else {
    await adminApi.createCategory(form)
  }
  dialogVisible.value = false
  load()
}

async function remove(id: number) {
  await adminApi.deleteCategory(id)
  load()
}

onMounted(load)
</script>

<template>
  <div>
    <div class="toolbar">
      <h2>分类管理</h2>
      <el-button type="primary" @click="openCreate(0)">添加顶级分类</el-button>
    </div>

    <el-table
      :data="categories"
      row-key="id"
      border
      default-expand-all
      :tree-props="{ children: 'children' }"
    >
      <el-table-column prop="name" label="分类名称" min-width="200" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column prop="status" label="状态" width="90">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : 'danger'">
            {{ row.status === 1 ? '正常' : '禁用' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="240">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="openCreate(row.id)">
            添加子分类
          </el-button>
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-popconfirm title="确认删除该分类？" @confirm="remove(row.id)">
            <template #reference>
              <el-button size="small" type="danger">删除</el-button>
            </template>
          </el-popconfirm>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑分类' : '新增分类'" width="460px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="分类名">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="图标URL">
          <el-input v-model="form.icon" />
        </el-form-item>
        <el-form-item label="排序">
          <el-input-number v-model="form.sortOrder" :min="0" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio :value="1">正常</el-radio>
            <el-radio :value="0">禁用</el-radio>
          </el-radio-group>
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
</style>

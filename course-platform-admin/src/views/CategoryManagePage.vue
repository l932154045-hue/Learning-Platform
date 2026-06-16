<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { adminCourseApi } from '@/api/modules/admin-course'
import { adminCategoryApi } from '@/api/modules/admin-category'
import type { CourseCategoryVO } from '@shared/types'

const categories = ref<CourseCategoryVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加分类')
const editingId = ref<number | null>(null)
const parentId = ref(0)
const formRef = ref<FormInstance>()

const form = reactive({ name: '', sortOrder: 0 })

onMounted(() => { fetch() })

async function fetch() {
  loading.value = true
  try {
    const res = await adminCourseApi.getCategories()
    categories.value = res.data.data ?? []
  } finally { loading.value = false }
}

function openAdd(pId = 0) {
  dialogTitle.value = pId ? '添加子分类' : '添加顶级分类'
  editingId.value = null; parentId.value = pId
  form.name = ''; form.sortOrder = 0
  dialogVisible.value = true
}

function openEdit(row: CourseCategoryVO) {
  dialogTitle.value = '编辑分类'; editingId.value = row.id
  form.name = row.name; form.sortOrder = row.sortOrder
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await adminCategoryApi.update(editingId.value, form.name, form.sortOrder)
      ElMessage.success('已更新')
    } else {
      await adminCategoryApi.create(form.name, parentId.value, form.sortOrder)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    fetch()
  } catch { /* handled */ }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await adminCategoryApi.delete(id)
    ElMessage.success('已删除')
    fetch()
  } catch { /* cancelled */ }
}

function getIndent(level: number) {
  return { paddingLeft: `${level * 24}px` }
}
</script>

<template>
  <div class="page">
    <div class="page-head">
      <h1>分类管理</h1>
      <el-button type="primary" @click="openAdd(0)">添加顶级分类</el-button>
    </div>
    <el-table :data="categories" v-loading="loading" border stripe row-key="id" :tree-props="{ children: 'children', hasChildren: 'children' }" default-expand-all>
      <el-table-column prop="name" label="名称" min-width="200" />
      <el-table-column prop="sortOrder" label="排序" width="80" />
      <el-table-column label="操作" width="220" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openAdd(row.id)">添加子分类</el-button>
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="400px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="名称" required><el-input v-model="form.name" /></el-form-item>
        <el-form-item label="排序"><el-input-number v-model="form.sortOrder" :min="0" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<style scoped>
.page { padding: var(--space-lg); }
.page-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: var(--space-lg); }
.page-head h1 { font-size: var(--font-size-xl); font-weight: 700; }
</style>

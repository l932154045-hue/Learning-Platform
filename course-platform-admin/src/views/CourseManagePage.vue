<script setup lang="ts">
import { ref, reactive, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox, type FormInstance } from 'element-plus'
import { adminCourseApi } from '@/api/modules/admin-course'
import type { CourseListItemVO, CourseCategoryVO, CourseSaveReq } from '@shared/types'

const router = useRouter()
const courses = ref<CourseListItemVO[]>([])
const categories = ref<CourseCategoryVO[]>([])
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('创建课程')
const editingId = ref<number | null>(null)
const formRef = ref<FormInstance>()

const form = reactive<CourseSaveReq>({
  title: '', description: '', coverUrl: '', categoryId: 0, teacherName: '', price: 0,
})

// Flatten category tree for dropdown with hierarchy display
interface FlatCategory { id: number; label: string }
const flatCategories = computed<FlatCategory[]>(() => {
  const result: FlatCategory[] = []
  function walk(list: CourseCategoryVO[], depth: number) {
    for (const cat of list) {
      const prefix = depth > 0 ? '　'.repeat(depth) + '└ ' : ''
      result.push({ id: cat.id, label: prefix + cat.name })
      if (cat.children) walk(cat.children, depth + 1)
    }
  }
  walk(categories.value, 0)
  return result
})

onMounted(() => { fetch() })

async function fetch() {
  loading.value = true
  try {
    const [cRes, catRes] = await Promise.all([
      adminCourseApi.getCourses(),
      adminCourseApi.getCategories(),
    ])
    courses.value = cRes.data.data?.records ?? []
    categories.value = catRes.data.data ?? []
  } finally { loading.value = false }
}

function openCreate() {
  dialogTitle.value = '创建课程'
  editingId.value = null
  Object.assign(form, { title: '', description: '', coverUrl: '', categoryId: 0, teacherName: '', price: 0 })
  dialogVisible.value = true
}

function openEdit(row: CourseListItemVO) {
  dialogTitle.value = '编辑课程'
  editingId.value = row.id
  Object.assign(form, { title: row.title, description: '', coverUrl: row.coverUrl || '', categoryId: row.categoryId ?? 0, teacherName: row.teacherName || '', price: row.price })
  dialogVisible.value = true
}

async function handleSave() {
  const valid = await formRef.value?.validate().catch(() => false)
  if (!valid) return
  try {
    if (editingId.value) {
      await adminCourseApi.update(editingId.value, form)
      ElMessage.success('已更新')
    } else {
      await adminCourseApi.create(form)
      ElMessage.success('已创建')
    }
    dialogVisible.value = false
    fetch()
  } catch { /* handled */ }
}

async function handleDelete(id: number) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await adminCourseApi.delete(id)
    ElMessage.success('已删除')
    fetch()
  } catch { /* cancelled */ }
}

async function handleToggleStatus(row: CourseListItemVO) {
  const newStatus = row.status === 1 ? 0 : 1
  await adminCourseApi.updateStatus(row.id, newStatus)
  ElMessage.success(newStatus === 1 ? '已上架' : '已下架')
}

function goVideos(id: number) { router.push(`/course/${id}/videos`) }
</script>

<template>
  <div class="page">
    <div class="page-head">
      <h1>课程管理</h1>
      <el-button type="primary" @click="openCreate">创建课程</el-button>
    </div>
    <el-table :data="courses" v-loading="loading" border stripe>
      <el-table-column prop="id" label="ID" width="60" />
      <el-table-column prop="title" label="标题" min-width="200" />
      <el-table-column prop="categoryName" label="分类" width="100" />
      <el-table-column prop="teacherName" label="讲师" width="100" />
      <el-table-column prop="price" label="价格" width="100">
        <template #default="{ row }">&yen;{{ row.price }}</template>
      </el-table-column>
      <el-table-column prop="saleCount" label="销量" width="80" />
      <el-table-column label="操作" width="260" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" @click="handleToggleStatus(row)">下架</el-button>
          <el-button size="small" @click="goVideos(row.id)">视频</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form ref="formRef" :model="form" label-width="80px">
        <el-form-item label="标题" required><el-input v-model="form.title" /></el-form-item>
        <el-form-item label="简介"><el-input v-model="form.description" type="textarea" /></el-form-item>
        <el-form-item label="价格" required><el-input-number v-model="form.price" :min="0" :precision="2" /></el-form-item>
        <el-form-item label="讲师"><el-input v-model="form.teacherName" /></el-form-item>
        <el-form-item label="封面"><el-input v-model="form.coverUrl" placeholder="URL" /></el-form-item>
        <el-form-item label="分类">
          <el-select v-model="form.categoryId" placeholder="选择分类">
            <el-option v-for="c in flatCategories" :key="c.id" :label="c.label" :value="c.id" />
          </el-select>
        </el-form-item>
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

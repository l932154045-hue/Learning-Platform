<script setup lang="ts">
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { adminCourseApi } from '@/api/modules/admin-course'
import { courseApi } from '@/api/modules/course'
import type { CourseDetailVO, VideoSaveReq } from '@shared/types'

const route = useRoute()
const router = useRouter()
const courseId = Number(route.params.id)
const course = ref<CourseDetailVO | null>(null)
const loading = ref(false)
const dialogVisible = ref(false)
const dialogTitle = ref('添加视频')
const editingVideoId = ref<number | null>(null)

const form = reactive<VideoSaveReq>({
  courseId, chapterTitle: '', videoTitle: '', videoUrl: '', duration: 0, sortOrder: 0,
})

onMounted(() => { fetchCourse() })

async function fetchCourse() {
  loading.value = true
  try {
    const res = await courseApi.getDetail(courseId)
    course.value = res.data.data ?? null
  } finally { loading.value = false }
}

function openAdd() {
  dialogTitle.value = '添加视频'
  editingVideoId.value = null
  Object.assign(form, { courseId, chapterTitle: '', videoTitle: '', videoUrl: '', duration: 0, sortOrder: 0 })
  dialogVisible.value = true
}

function openEdit(video: any) {
  dialogTitle.value = '编辑视频'
  editingVideoId.value = video.id
  Object.assign(form, {
    courseId, chapterTitle: video.chapterTitle, videoTitle: video.videoTitle,
    videoUrl: video.videoUrl, duration: video.duration, sortOrder: video.sortOrder,
  })
  dialogVisible.value = true
}

async function handleSave() {
  try {
    if (editingVideoId.value) {
      await adminCourseApi.updateVideo(editingVideoId.value, { ...form })
      ElMessage.success('已更新')
    } else {
      await adminCourseApi.addVideo({ ...form })
      ElMessage.success('已添加')
    }
    dialogVisible.value = false
    fetchCourse()
  } catch { /* handled */ }
}

async function handleDelete(videoId: number) {
  try {
    await ElMessageBox.confirm('确定删除？', '提示', { type: 'warning' })
    await adminCourseApi.deleteVideo(videoId)
    ElMessage.success('已删除')
    fetchCourse()
  } catch { /* cancelled */ }
}
</script>

<template>
  <div class="page">
    <div class="page-head">
      <div>
        <el-button text @click="router.push('/courses')">← 返回课程列表</el-button>
        <h1>{{ course?.title || '加载中...' }} — 视频管理</h1>
      </div>
      <el-button type="primary" @click="openAdd">添加视频</el-button>
    </div>
    <el-table :data="course?.chapters || []" v-loading="loading" border stripe row-key="id">
      <el-table-column prop="sortOrder" label="序号" width="60" />
      <el-table-column prop="chapterTitle" label="章节标题" width="180" />
      <el-table-column prop="videoTitle" label="视频标题" min-width="200" />
      <el-table-column prop="duration" label="时长(秒)" width="100" />
      <el-table-column label="操作" width="160" fixed="right">
        <template #default="{ row }">
          <el-button size="small" @click="openEdit(row)">编辑</el-button>
          <el-button size="small" type="danger" @click="handleDelete(row.id)">删除</el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="500px">
      <el-form :model="form" label-width="80px">
        <el-form-item label="章节标题"><el-input v-model="form.chapterTitle" /></el-form-item>
        <el-form-item label="视频标题"><el-input v-model="form.videoTitle" /></el-form-item>
        <el-form-item label="视频URL"><el-input v-model="form.videoUrl" /></el-form-item>
        <el-form-item label="时长(秒)"><el-input-number v-model="form.duration" :min="0" /></el-form-item>
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
.page-head { display: flex; justify-content: space-between; align-items: flex-start; margin-bottom: var(--space-lg); }
.page-head h1 { font-size: var(--font-size-lg); font-weight: 700; margin-top: var(--space-sm); }
</style>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import RatingStars from '@/components/common/RatingStars.vue'
import { reviewApi } from '@/api/modules/review'
import { courseApi } from '@/api/modules/course'
import type { CourseDetailVO } from '@shared/types'

const route = useRoute()
const router = useRouter()
const courseId = Number(route.params.courseId)
const course = ref<CourseDetailVO | null>(null)
const rating = ref(0)
const content = ref('')
const loading = ref(true)
const submitting = ref(false)

onMounted(async () => {
  try {
    const [cRes, rRes] = await Promise.all([
      courseApi.getDetail(courseId),
      reviewApi.getMyReview(courseId).catch(() => ({ data: { data: null } })),
    ])
    course.value = cRes.data.data ?? null
    const myReview = rRes?.data?.data
    if (myReview) {
      rating.value = myReview.rating
      content.value = myReview.content
    }
  } finally { loading.value = false }
})

async function submit() {
  if (!rating.value) { ElMessage.warning('请评分'); return }
  if (!content.value.trim()) { ElMessage.warning('请输入评价内容'); return }
  submitting.value = true
  try {
    await reviewApi.submit({ courseId, rating: rating.value, content: content.value })
    ElMessage.success('评价已提交')
    router.push(`/course/${courseId}`)
  } catch { /* handled */ } finally { submitting.value = false }
}
</script>

<template>
  <div class="review-page">
    <AppHeader />
    <div class="container page-section">
      <el-button text @click="router.push(`/course/${courseId}`)">← 返回课程详情</el-button>
      <div v-if="loading" class="mt"><el-skeleton :rows="4" animated /></div>
      <template v-else-if="course">
        <div class="review-card">
          <div class="course-summary">
            <img :src="course.coverUrl || 'https://placehold.co/200x120/E8E6DD/1A1A2E?text=Course'" />
            <div>
              <h3>{{ course.title }}</h3>
              <p>{{ course.teacherName }}</p>
            </div>
          </div>
          <div class="review-form">
            <label>评分</label>
            <RatingStars v-model="rating" size="large" class="mb" />
            <label>评价内容</label>
            <el-input v-model="content" type="textarea" :rows="5" maxlength="500" show-word-limit placeholder="分享你的学习体验..." />
            <el-button type="primary" size="large" :loading="submitting" class="submit-btn" @click="submit">提交评价</el-button>
          </div>
        </div>
      </template>
    </div>
    <AppFooter />
  </div>
</template>

<style scoped>
.mt { margin-top: var(--space-lg); }
.review-card { background: white; border: 1px solid var(--color-border); border-radius: var(--radius-md); overflow: hidden; margin-top: var(--space-md); }
.course-summary { display: flex; gap: var(--space-lg); padding: var(--space-lg); border-bottom: 1px solid var(--color-border); background: var(--color-surface-alt); }
.course-summary img { width: 160px; height: 96px; object-fit: cover; border-radius: var(--radius-sm); }
.course-summary h3 { font-size: var(--font-size-base); font-weight: 600; }
.course-summary p { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: 4px; }
.review-form { padding: var(--space-lg); }
.review-form label { display: block; font-size: var(--font-size-sm); font-weight: 600; margin-bottom: var(--space-sm); margin-top: var(--space-md); }
.mb { margin-bottom: var(--space-lg); }
.submit-btn { margin-top: var(--space-lg); }
</style>

<script setup lang="ts">
import { onMounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import CourseCard from '@/components/common/CourseCard.vue'
import EmptyState from '@/components/common/EmptyState.vue'
import { useLearningStore } from '@/stores/learning'

const router = useRouter()
const store = useLearningStore()

onMounted(() => { store.fetchMyCourses() })

function goLearn(courseId: number) {
  router.push(`/learning/${courseId}`)
}
</script>

<template>
  <div class="my-learning-page">
    <AppHeader />
    <div class="container page-section">
      <h1 class="page-title">我的学习</h1>
      <div v-if="store.loading"><el-skeleton :rows="6" animated /></div>
      <template v-else-if="store.myCourses.length > 0">
        <div class="course-grid">
          <div
            v-for="course in store.myCourses"
            :key="course.enrollmentId"
            class="course-wrap"
            @click="goLearn(course.courseId)"
          >
            <CourseCard
              :course="{ ...course, title: course.courseTitle, coverUrl: course.courseCover } as any"
              :show-progress="true"
              :progress="course.totalProgress"
            />
          </div>
        </div>
      </template>
      <EmptyState v-else description="还没有报名的课程，去逛逛吧" />
    </div>
    <AppFooter />
  </div>
</template>

<style scoped>
.page-title { font-size: var(--font-size-2xl); font-weight: 700; margin-bottom: var(--space-xl); }
.course-grid { display: grid; grid-template-columns: repeat(4, 1fr); gap: var(--space-lg); }
@media (max-width: 1024px) { .course-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px)  { .course-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px)  { .course-grid { grid-template-columns: 1fr; } }
.course-wrap { cursor: pointer; }
</style>

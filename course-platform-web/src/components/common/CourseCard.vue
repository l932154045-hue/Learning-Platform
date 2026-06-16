<script setup lang="ts">
import type { CourseListItemVO, MyCourseVO } from '@shared/types'
import { useRouter } from 'vue-router'

const props = defineProps<{
  course: CourseListItemVO | MyCourseVO
  showProgress?: boolean
  progress?: number
}>()

const router = useRouter()

const title = 'title' in props.course ? props.course.title : props.course.courseTitle
const cover = 'coverUrl' in props.course ? props.course.coverUrl : props.course.courseCover
const teacher = 'teacherName' in props.course ? props.course.teacherName : props.course.teacherName
const price = props.course.price

function goDetail() {
  const id = 'courseId' in props.course ? props.course.courseId : props.course.id
  router.push(`/course/${id}`)
}
</script>

<template>
  <div class="course-card" @click="goDetail">
    <div class="card-cover">
      <img
        :src="cover || 'https://placehold.co/600x400/E8E6DD/1A1A2E?text=Course'"
        :alt="title"
        loading="lazy"
      />
      <div v-if="showProgress && progress !== undefined" class="progress-overlay">
        <div class="progress-bar">
          <div class="progress-fill" :style="{ width: progress + '%' }"></div>
        </div>
        <span class="progress-text">{{ progress }}%</span>
      </div>
    </div>
    <div class="card-body">
      <h3 class="card-title">{{ title }}</h3>
      <p class="card-teacher">{{ teacher || '知名讲师' }}</p>
      <div class="card-footer">
        <span class="card-price">&yen;{{ price }}</span>
        <span v-if="'saleCount' in course" class="card-sales">{{ (course as CourseListItemVO).saleCount }} 人已学</span>
      </div>
    </div>
  </div>
</template>

<style scoped>
.course-card {
  border-radius: var(--radius-md);
  overflow: hidden;
  background: white;
  border: 1px solid var(--color-border);
  cursor: pointer;
  transition: transform 0.2s, box-shadow 0.2s;
}
.course-card:hover {
  transform: translateY(-2px);
  box-shadow: var(--shadow-card-hover);
}

.card-cover {
  position: relative;
  aspect-ratio: 16 / 10;
  background: var(--color-surface-alt);
  overflow: hidden;
}
.card-cover img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.progress-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(transparent, rgba(0,0,0,0.7));
  padding: var(--space-lg) var(--space-md) var(--space-sm);
  display: flex;
  align-items: center;
  gap: var(--space-sm);
}
.progress-bar {
  flex: 1;
  height: 4px;
  background: rgba(255,255,255,0.3);
  border-radius: 2px;
  overflow: hidden;
}
.progress-fill {
  height: 100%;
  background: var(--color-accent);
  border-radius: 2px;
  transition: width 0.3s;
}
.progress-text {
  font-size: var(--font-size-xs);
  color: white;
  font-weight: 600;
}

.card-body {
  padding: var(--space-md);
}
.card-title {
  font-size: var(--font-size-base);
  font-weight: 600;
  color: var(--color-text);
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  line-height: 1.4;
}
.card-teacher {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
  margin-top: var(--space-xs);
}
.card-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: var(--space-md);
}
.card-price {
  font-size: var(--font-size-lg);
  font-weight: 700;
  color: var(--color-accent);
}
.card-sales {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
}
</style>

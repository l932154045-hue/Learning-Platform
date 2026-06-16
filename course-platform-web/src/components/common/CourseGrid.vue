<script setup lang="ts">
import { computed } from 'vue'
import type { CourseListItemVO } from '@shared/types'
import CourseCard from './CourseCard.vue'
import { ElPagination } from 'element-plus'

const props = defineProps<{
  courses: CourseListItemVO[]
  loading: boolean
  total: number
  pageNum: number
  pageSize: number
}>()

const emit = defineEmits<{
  'page-change': [page: number]
}>()

const totalPages = computed(() => Math.ceil(props.total / props.pageSize) || 1)
</script>

<template>
  <div>
    <div v-if="loading" class="course-grid">
      <div v-for="i in 8" :key="i" class="skeleton-card">
        <el-skeleton animated>
          <template #template>
            <div class="sk-image"></div>
            <div class="sk-body">
              <el-skeleton-item variant="text" style="width: 80%; height: 20px" />
              <el-skeleton-item variant="text" style="width: 50%; height: 14px" />
              <el-skeleton-item variant="text" style="width: 40%; height: 18px" />
            </div>
          </template>
        </el-skeleton>
      </div>
    </div>

    <div v-else-if="courses.length === 0" class="empty-wrap">
      <el-empty description="暂无课程" />
    </div>

    <div v-else class="course-grid">
      <CourseCard
        v-for="course in courses"
        :key="course.id"
        :course="course"
      />
    </div>

    <div v-if="total > props.pageSize" class="pagination-wrap">
      <el-pagination
        :current-page="pageNum"
        :page-size="pageSize"
        :total="total"
        :page-sizes="[8, 16, 24]"
        layout="total, sizes, prev, pager, next"
        background
        @current-change="(p: number) => emit('page-change', p)"
        @update:page-size="(s: number) => emit('page-change', 1)"
      />
    </div>
  </div>
</template>

<style scoped>
.course-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-lg);
}
@media (max-width: 1024px) { .course-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px)  { .course-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px)  { .course-grid { grid-template-columns: 1fr; } }

.skeleton-card {
  background: white;
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--color-border);
}
.sk-image {
  aspect-ratio: 16/10;
  background: var(--color-surface-alt);
}
.sk-body {
  padding: var(--space-md);
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.empty-wrap {
  padding: var(--space-3xl) 0;
}

.pagination-wrap {
  display: flex;
  justify-content: center;
  margin-top: var(--space-2xl);
}
</style>

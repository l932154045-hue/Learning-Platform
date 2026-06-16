<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import CourseCard from '@/components/common/CourseCard.vue'
import { courseApi } from '@/api/modules/course'
import type { CourseListItemVO, CourseCategoryVO } from '@shared/types'

const router = useRouter()
const hotCourses = ref<CourseListItemVO[]>([])
const categories = ref<CourseCategoryVO[]>([])
const loading = ref(true)
const glowX = ref(50)
const glowY = ref(50)

function onMouseMove(e: MouseEvent) {
  const hero = (e.currentTarget as HTMLElement)
  const rect = hero.getBoundingClientRect()
  glowX.value = ((e.clientX - rect.left) / rect.width) * 100
  glowY.value = ((e.clientY - rect.top) / rect.height) * 100
}

onMounted(async () => {
  try {
    const [hotRes, catRes] = await Promise.all([
      courseApi.getHot(),
      courseApi.getCategoryTree(),
    ])
    hotCourses.value = hotRes.data.data ?? []
    categories.value = (catRes.data.data ?? []).slice(0, 8)
  } finally {
    loading.value = false
  }
})

function goToList(categoryId?: number) {
  const query = categoryId ? `?categoryId=${categoryId}` : ''
  router.push(`/courses${query}`)
}

</script>

<template>
  <div class="home-page">
    <AppHeader />

    <!-- Hero -->
    <section class="hero" @mousemove="onMouseMove">
      <div class="hero-glow" :style="{ left: glowX + '%', top: glowY + '%' }"></div>
      <div class="container hero-content">
        <h1 class="hero-title">学一门课<span class="hero-accent">，换一个世界</span></h1>
        <p class="hero-sub">200+ 实战课程，每一门都来自一线开发者</p>
        <div class="hero-actions">
          <el-button type="primary" size="large" round @click="goToList()">探索课程</el-button>
          <el-button size="large" round class="btn-outline" @click="goToList()">了解详情</el-button>
        </div>
        <div class="hero-stats">
          <div class="stat"><span class="stat-num">200+</span><span class="stat-label">实战课程</span></div>
          <div class="stat"><span class="stat-num">50+</span><span class="stat-label">一线讲师</span></div>
          <div class="stat"><span class="stat-num">月更新</span><span class="stat-label">保持同步</span></div>
        </div>
      </div>
    </section>

    <!-- Categories -->
    <section class="page-section container">
      <div class="section-header">
        <h2>课程分类</h2>
        <a class="section-link" @click="goToList()">查看全部 →</a>
      </div>
      <div v-if="loading" class="cat-skeleton">
        <div v-for="i in 8" :key="i" class="cat-chip-skel"></div>
      </div>
      <div v-else class="cat-list">
        <span
          v-for="cat in categories"
          :key="cat.id"
          class="cat-chip"
          @click="goToList(cat.id)"
        >
          {{ cat.name }}
        </span>
      </div>
    </section>

    <!-- Hot Courses -->
    <section class="page-section container">
      <div class="section-header">
        <h2>热门课程</h2>
        <a class="section-link" @click="goToList()">查看全部 →</a>
      </div>
      <div v-if="loading" class="course-grid">
        <div v-for="i in 8" :key="i" class="sk-card">
          <el-skeleton animated><template #template><el-skeleton-item variant="image" style="aspect-ratio:16/10" /></template></el-skeleton>
        </div>
      </div>
      <div v-else class="course-grid">
        <CourseCard
          v-for="course in hotCourses"
          :key="course.id"
          :course="course"
          @click="goToDetail(course.id)"
        />
      </div>
    </section>

    <AppFooter />
  </div>
</template>

<style scoped>
/* Hero */
.hero {
  position: relative;
  background: linear-gradient(135deg, var(--color-primary) 0%, #2D1B69 100%);
  padding: 100px 0 80px;
  overflow: hidden;
}
.hero-glow {
  position: absolute;
  width: 500px;
  height: 500px;
  border-radius: 50%;
  background: radial-gradient(circle, rgba(232, 153, 45, 0.12) 0%, transparent 70%);
  pointer-events: none;
  transform: translate(-50%, -50%);
  transition: left 0.4s ease-out, top 0.4s ease-out;
}
.hero-content {
  position: relative;
  z-index: 1;
  text-align: center;
}
.hero-title {
  font-size: var(--font-size-3xl);
  font-weight: 700;
  color: white;
  line-height: 1.3;
}
.hero-accent {
  color: var(--color-accent);
}
.hero-sub {
  margin-top: var(--space-md);
  font-size: var(--font-size-lg);
  color: rgba(255,255,255,0.7);
  max-width: 500px;
  margin-left: auto;
  margin-right: auto;
}
.hero-actions {
  margin-top: var(--space-xl);
  display: flex;
  gap: var(--space-md);
  justify-content: center;
}
.btn-outline {
  border-color: rgba(255,255,255,0.5) !important;
  color: white !important;
  background: transparent !important;
}
.btn-outline:hover {
  border-color: white !important;
  background: rgba(255,255,255,0.1) !important;
}
.hero-stats {
  display: flex;
  justify-content: center;
  gap: var(--space-3xl);
  margin-top: var(--space-2xl);
}
.stat {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: var(--space-xs);
}
.stat-num {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  color: var(--color-accent);
}
.stat-label {
  font-size: var(--font-size-sm);
  color: rgba(255,255,255,0.6);
}

/* Section header */
.section-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: var(--space-xl);
}
.section-header h2 {
  font-size: var(--font-size-xl);
  font-weight: 700;
}
.section-link {
  font-size: var(--font-size-sm);
  font-weight: 500;
  color: var(--color-primary-hover);
  cursor: pointer;
}

/* Categories */
.cat-list {
  display: flex;
  flex-wrap: wrap;
  gap: var(--space-md);
}
.cat-chip {
  display: inline-block;
  padding: var(--space-sm) var(--space-lg);
  border-radius: 20px;
  background: white;
  border: 1px solid var(--color-border);
  font-size: var(--font-size-sm);
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}
.cat-chip:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
  background: rgba(30,30,74,0.04);
}
.cat-skeleton {
  display: flex;
  gap: var(--space-md);
}
.cat-chip-skel {
  width: 100px;
  height: 36px;
  border-radius: 20px;
  background: var(--color-surface-alt);
  animation: pulse 1.5s ease-in-out infinite;
}

/* Course Grid */
.course-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: var(--space-lg);
}
@media (max-width: 1024px) { .course-grid { grid-template-columns: repeat(3, 1fr); } }
@media (max-width: 768px)  { .course-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 480px)  { .course-grid { grid-template-columns: 1fr; } }

.sk-card {
  border-radius: var(--radius-md);
  overflow: hidden;
  border: 1px solid var(--color-border);
}

@keyframes pulse {
  0%, 100% { opacity: 1; }
  50% { opacity: 0.5; }
}
</style>

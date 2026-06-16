<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import RatingStars from '@/components/common/RatingStars.vue'
import { courseApi } from '@/api/modules/course'
import { cartApi } from '@/api/modules/cart'
import { orderApi } from '@/api/modules/order'
import { reviewApi } from '@/api/modules/review'
import { useAuthStore } from '@/stores/auth'
import type { CourseDetailVO, ReviewVO } from '@shared/types'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()
const course = ref<CourseDetailVO | null>(null)
const reviews = ref<ReviewVO[]>([])
const reviewPage = ref(1)
const reviewTotal = ref(0)
const loading = ref(true)

const courseId = Number(route.params.id)

onMounted(async () => {
  try {
    const [cRes, rRes] = await Promise.all([
      courseApi.getDetail(courseId),
      reviewApi.getCourseReviews(courseId),
    ])
    course.value = cRes.data.data ?? null
    const rData = rRes.data.data
    reviews.value = rData ?? []
    reviewTotal.value = rData?.length ?? 0
  } finally {
    loading.value = false
  }
})

async function addToCart() {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  try {
    await cartApi.add(courseId)
    ElMessage.success('已加入购物车')
  } catch { /* error handled by interceptor */ }
}

async function buyNow() {
  if (!auth.isLoggedIn) { router.push('/login'); return }
  try {
    const res = await orderApi.create(courseId)
    const orderId = res.data.data
    if (orderId) {
      router.push(`/order/${orderId}`)
    }
  } catch { /* error handled by interceptor */ }
}
</script>

<template>
  <div class="detail-page">
    <AppHeader />
    <div v-if="loading" class="container page-section">
      <el-skeleton :rows="12" animated />
    </div>
    <template v-else-if="course">
      <div class="detail-hero">
        <div class="container hero-grid">
          <div class="hero-cover">
            <img :src="course.coverUrl || 'https://placehold.co/600x400/E8E6DD/1A1A2E?text=Course'" :alt="course.title" />
          </div>
          <div class="hero-info">
            <h1>{{ course.title }}</h1>
            <p class="meta">
              <span>{{ course.teacherName }}</span>
              <span class="dot">·</span>
              <span>{{ course.categoryName }}</span>
              <span class="dot">·</span>
              <span>{{ course.saleCount }} 人已学</span>
            </p>
            <div class="price-block">
              <span class="price">&yen;{{ course.price }}</span>
            </div>
            <div class="hero-actions">
              <el-button type="primary" size="large" @click="buyNow">立即购买</el-button>
              <el-button size="large" @click="addToCart">加入购物车</el-button>
            </div>
          </div>
        </div>
      </div>

      <div class="container page-section">
        <div class="detail-grid">
          <div class="detail-main">
            <h2 class="sec-title">课程介绍</h2>
            <p class="desc">{{ course.description || '暂无简介' }}</p>

            <h2 class="sec-title">课程大纲</h2>
            <div v-if="course.chapters?.length" class="chapter-list">
              <el-collapse>
                <el-collapse-item
                  v-for="(ch, idx) in course.chapters"
                  :key="ch.id"
                  :title="`第${idx + 1}章 ${ch.chapterTitle}（${ch.videoTitle ? 1 : 0} 个视频）`"
                >
                  <div class="video-item">
                    <span class="video-title">{{ ch.videoTitle }}</span>
                    <span class="video-dur">{{ ch.duration ? Math.floor(ch.duration / 60) + ' 分钟' : '' }}</span>
                  </div>
                </el-collapse-item>
              </el-collapse>
            </div>
            <div v-else class="empty-wrap"><el-empty description="暂无章节" /></div>

            <h2 class="sec-title">课程评价（{{ reviewTotal }}）</h2>
            <div v-if="reviews.length" class="review-list">
              <div v-for="r in reviews" :key="r.id" class="review-item">
                <div class="review-head">
                  <span class="review-name">{{ r.nickname }}</span>
                  <RatingStars :model-value="r.rating" readonly size="small" />
                  <span class="review-date">{{ r.createdAt?.slice(0, 10) }}</span>
                </div>
                <p class="review-content">{{ r.content }}</p>
              </div>
            </div>
            <div v-else class="empty-wrap"><el-empty description="暂无评价" /></div>
          </div>

          <aside class="detail-sidebar">
            <div class="sidebar-card">
              <h4>课程信息</h4>
              <div class="info-row"><span>讲师</span><span>{{ course.teacherName }}</span></div>
              <div class="info-row"><span>分类</span><span>{{ course.categoryName }}</span></div>
              <div class="info-row"><span>章节</span><span>{{ course.chapters?.length || 0 }} 章</span></div>
              <div class="info-row"><span>已学</span><span>{{ course.saleCount }} 人</span></div>
            </div>
          </aside>
        </div>
      </div>
    </template>
    <AppFooter />
  </div>
</template>

<style scoped>
.detail-hero {
  background: linear-gradient(180deg, rgba(30,30,74,0.03) 0%, transparent 100%);
  padding: var(--space-2xl) 0;
}
.hero-grid {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: var(--space-2xl);
  align-items: center;
}
@media (max-width: 768px) { .hero-grid { grid-template-columns: 1fr; } }
.hero-cover {
  border-radius: var(--radius-lg);
  overflow: hidden;
  box-shadow: var(--shadow-card-hover);
}
.hero-cover img { width: 100%; aspect-ratio: 16/10; object-fit: cover; }
.hero-info h1 { font-size: var(--font-size-2xl); font-weight: 700; line-height: 1.3; }
.meta { margin-top: var(--space-md); font-size: var(--font-size-sm); color: var(--color-text-secondary); }
.dot { margin: 0 8px; }
.price-block { margin-top: var(--space-lg); }
.price { font-size: 32px; font-weight: 700; color: var(--color-accent); }
.hero-actions { margin-top: var(--space-lg); display: flex; gap: var(--space-md); }

.detail-grid {
  display: grid;
  grid-template-columns: 1fr 300px;
  gap: var(--space-2xl);
}
@media (max-width: 768px) { .detail-grid { grid-template-columns: 1fr; } }

.sec-title { font-size: var(--font-size-lg); font-weight: 700; margin: var(--space-xl) 0 var(--space-md); }
.desc { color: var(--color-text-secondary); line-height: 1.8; }
.chapter-list { margin-top: var(--space-md); }
.video-item { display: flex; justify-content: space-between; align-items: center; padding: var(--space-sm) 0; }
.video-title { font-size: var(--font-size-sm); }
.video-dur { font-size: var(--font-size-xs); color: var(--color-text-secondary); }

.review-item { padding: var(--space-md) 0; border-bottom: 1px solid var(--color-border); }
.review-head { display: flex; align-items: center; gap: var(--space-md); }
.review-name { font-weight: 600; font-size: var(--font-size-sm); }
.review-date { font-size: var(--font-size-xs); color: var(--color-text-secondary); }
.review-content { margin-top: var(--space-sm); font-size: var(--font-size-sm); color: var(--color-text-secondary); }

.sidebar-card {
  background: white;
  border: 1px solid var(--color-border);
  border-radius: var(--radius-md);
  padding: var(--space-lg);
  position: sticky;
  top: calc(var(--header-height) + 24px);
}
.sidebar-card h4 { font-size: var(--font-size-base); font-weight: 700; margin-bottom: var(--space-md); }
.info-row { display: flex; justify-content: space-between; padding: var(--space-sm) 0; font-size: var(--font-size-sm); }
.info-row span:first-child { color: var(--color-text-secondary); }
.empty-wrap { padding: var(--space-lg) 0; }
</style>

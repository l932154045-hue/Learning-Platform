<script setup lang="ts">
import { ref, onMounted, onUnmounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import { learningApi } from '@/api/modules/learning'
import { courseApi } from '@/api/modules/course'
import type { CourseDetailVO, ProgressVO } from '@shared/types'

const route = useRoute()
const router = useRouter()
const courseId = Number(route.params.courseId)
const course = ref<CourseDetailVO | null>(null)
const progressMap = ref<Record<number, ProgressVO>>({})
const currentVideoId = ref<number>(0)
const currentVideo = ref<any>(null)
const loading = ref(true)
const videoRef = ref<HTMLVideoElement>()
let progressTimer: ReturnType<typeof setInterval> | null = null

onMounted(async () => {
  try {
    const [cRes, pRes] = await Promise.all([
      courseApi.getDetail(courseId),
      learningApi.getProgress(courseId),
    ])
    course.value = cRes.data.data ?? null
    const map: Record<number, ProgressVO> = {}
    for (const p of pRes.data.data ?? []) { map[p.videoId] = p }
    progressMap.value = map

    // Select first video
    const firstVideo = course.value?.chapters?.[0]
    if (firstVideo) {
      currentVideoId.value = firstVideo.id
      currentVideo.value = firstVideo
    }
  } finally { loading.value = false }
  startProgressReport()
})

onUnmounted(() => { stopProgressReport() })

function selectVideo(video: any) {
  currentVideoId.value = video.id
  currentVideo.value = video
  const p = progressMap.value[video.id]
  if (p && videoRef.value) {
    videoRef.value.currentTime = p.progressSeconds || 0
  }
  videoRef.value?.play()
}

function startProgressReport() {
  progressTimer = setInterval(() => {
    if (!videoRef.value || videoRef.value.paused) return
    const secs = Math.floor(videoRef.value.currentTime)
    const vid = currentVideoId.value
    if (!vid) return
    const prev = progressMap.value[vid]
    if (!prev || secs > prev.progressSeconds) {
      learningApi.reportProgress({
        videoId: vid,
        courseId,
        progressSeconds: secs,
        duration: currentVideo.value?.duration || 0,
      }).catch(() => {})
      progressMap.value[vid] = { ...prev, progressSeconds: secs } as ProgressVO
    }
  }, 10000)
}

function stopProgressReport() {
  if (progressTimer) { clearInterval(progressTimer); progressTimer = null }
}

function handleVideoEnded() {
  const vid = currentVideoId.value
  learningApi.reportProgress({
    videoId: vid,
    courseId,
    progressSeconds: currentVideo.value?.duration || 0,
    duration: currentVideo.value?.duration || 0,
  }).catch(() => {})
}
</script>

<template>
  <div class="learning-page">
    <AppHeader />
    <div v-if="loading" class="container page-section"><el-skeleton :rows="10" animated /></div>
    <template v-else-if="course">
      <div class="learning-layout">
        <div class="player-area">
          <div class="video-container">
            <video
              v-if="currentVideo"
              ref="videoRef"
              :src="currentVideo.videoUrl || 'https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4'"
              controls
              class="video-player"
              @ended="handleVideoEnded"
            />
            <div v-else class="no-video">
              <p>请选择一个视频开始学习</p>
            </div>
          </div>
          <div class="video-info">
            <h3>{{ currentVideo?.videoTitle || '选择视频' }}</h3>
            <span class="back-btn" @click="router.push('/my-learning')">← 返回我的课程</span>
          </div>
        </div>
        <aside class="sidebar">
          <h3>课程目录</h3>
          <div class="chapter-list">
            <div
              v-for="(ch, idx) in course.chapters"
              :key="ch.id"
              :class="['chapter-item', { active: currentVideoId === ch.id }]"
              @click="selectVideo(ch)"
            >
              <span class="ch-index">{{ idx + 1 }}</span>
              <div class="ch-info">
                <span class="ch-title">{{ ch.chapterTitle }}</span>
                <span class="ch-video">{{ ch.videoTitle }}</span>
                <span class="ch-dur" v-if="ch.duration">{{ Math.floor(ch.duration / 60) }}分钟</span>
              </div>
              <span v-if="progressMap[ch.id]?.finished" class="ch-check">✅</span>
              <span v-else-if="progressMap[ch.id]" class="ch-progress">{{ Math.round((progressMap[ch.id].progressSeconds / ch.duration) * 100) }}%</span>
            </div>
          </div>
        </aside>
      </div>
    </template>
    <AppFooter />
  </div>
</template>

<style scoped>
.learning-layout {
  display: flex;
  min-height: calc(100vh - var(--header-height));
  max-width: 1400px;
  margin: 0 auto;
}
.player-area { flex: 1; padding: var(--space-lg); }
.video-container { background: #000; border-radius: var(--radius-md); overflow: hidden; aspect-ratio: 16/9; }
.video-player { width: 100%; height: 100%; }
.no-video { display: flex; align-items: center; justify-content: center; height: 100%; color: rgba(255,255,255,0.5); }
.video-info { display: flex; justify-content: space-between; align-items: center; margin-top: var(--space-md); }
.video-info h3 { font-size: var(--font-size-lg); font-weight: 600; }
.back-btn { font-size: var(--font-size-sm); color: var(--color-primary-hover); cursor: pointer; }

.sidebar {
  width: 340px;
  background: white;
  border-left: 1px solid var(--color-border);
  padding: var(--space-lg);
  overflow-y: auto;
}
.sidebar h3 { font-size: var(--font-size-base); font-weight: 700; margin-bottom: var(--space-md); }
.chapter-item {
  display: flex;
  align-items: center;
  gap: var(--space-md);
  padding: var(--space-md);
  border-radius: var(--radius-sm);
  cursor: pointer;
  transition: background 0.15s;
}
.chapter-item:hover,
.chapter-item.active { background: rgba(30,30,74,0.04); }
.chapter-item.active { border-left: 3px solid var(--color-primary); }
.ch-index {
  width: 28px; height: 28px;
  border-radius: 50%;
  background: var(--color-surface-alt);
  display: flex; align-items: center; justify-content: center;
  font-size: var(--font-size-xs); font-weight: 600; flex-shrink: 0;
}
.ch-info { flex: 1; min-width: 0; }
.ch-title { font-size: var(--font-size-sm); font-weight: 600; display: block; }
.ch-video { font-size: var(--font-size-xs); color: var(--color-text-secondary); display: block; }
.ch-dur { font-size: 11px; color: var(--color-text-secondary); }
.ch-check, .ch-progress { font-size: var(--font-size-xs); flex-shrink: 0; }
.ch-progress { color: var(--color-primary-hover); font-weight: 500; }

@media (max-width: 768px) {
  .learning-layout { flex-direction: column; }
  .sidebar { width: 100%; border-left: none; border-top: 1px solid var(--color-border); }
}
</style>

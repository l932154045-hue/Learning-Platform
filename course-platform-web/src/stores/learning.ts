import { defineStore } from 'pinia'
import { ref } from 'vue'
import { learningApi } from '@/api/modules/learning'
import type { MyCourseVO, ProgressVO } from '@shared/types'

export const useLearningStore = defineStore('learning', () => {
  const myCourses = ref<MyCourseVO[]>([])
  const currentProgress = ref<ProgressVO[]>([])
  const loading = ref(false)

  async function fetchMyCourses() {
    loading.value = true
    try {
      const res = await learningApi.getMyCourses()
      myCourses.value = res.data.data ?? []
    } finally {
      loading.value = false
    }
  }

  async function fetchProgress(courseId: number) {
    const res = await learningApi.getProgress(courseId)
    currentProgress.value = res.data.data ?? []
  }

  return { myCourses, currentProgress, loading, fetchMyCourses, fetchProgress }
})

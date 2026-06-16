<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { adminCourseApi } from '@/api/modules/admin-course'

const totalCourses = ref(0)
const totalCategories = ref(0)

onMounted(async () => {
  try {
    const [cRes, catRes] = await Promise.all([
      adminCourseApi.getCourses(),
      adminCourseApi.getCategories(),
    ])
    totalCourses.value = cRes.data.data?.total ?? 0
    totalCategories.value = catRes.data.data?.length ?? 0
  } catch { /* handled */ }
})
</script>

<template>
  <div class="dashboard">
    <h1>仪表盘</h1>
    <div class="stat-cards">
      <div class="stat-card">
        <div class="stat-icon">📚</div>
        <div class="stat-val">{{ totalCourses }}</div>
        <div class="stat-label">课程总数</div>
      </div>
      <div class="stat-card">
        <div class="stat-icon">📂</div>
        <div class="stat-val">{{ totalCategories }}</div>
        <div class="stat-label">分类数量</div>
      </div>
    </div>
  </div>
</template>

<style scoped>
h1 { font-size: var(--font-size-xl); font-weight: 700; margin-bottom: var(--space-xl); }
.stat-cards { display: grid; grid-template-columns: repeat(auto-fill, minmax(220px, 1fr)); gap: var(--space-lg); }
.stat-card {
  background: white; border-radius: var(--radius-md); padding: var(--space-xl);
  border: 1px solid var(--color-border); text-align: center;
}
.stat-icon { font-size: 36px; margin-bottom: var(--space-sm); }
.stat-val { font-size: var(--font-size-2xl); font-weight: 700; color: var(--color-primary); }
.stat-label { font-size: var(--font-size-sm); color: var(--color-text-secondary); margin-top: 4px; }
</style>

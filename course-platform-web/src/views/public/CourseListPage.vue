<script setup lang="ts">
import { ref, watch, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import AppHeader from '@/components/layout/AppHeader.vue'
import AppFooter from '@/components/layout/AppFooter.vue'
import CourseGrid from '@/components/common/CourseGrid.vue'
import { courseApi } from '@/api/modules/course'
import type { CourseListItemVO, CourseCategoryVO, CourseSearchParams } from '@shared/types'

const route = useRoute()
const router = useRouter()
const courses = ref<CourseListItemVO[]>([])
const categories = ref<CourseCategoryVO[]>([])
const loading = ref(true)
const total = ref(0)

const params = ref<CourseSearchParams>({
  pageNum: Number(route.query.pageNum) || 1,
  pageSize: 8,
  keyword: (route.query.keyword as string) || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  sort: (route.query.sort as string) || 'saleCount_desc',
  priceMin: 0,
  priceMax: 99999,
})

// Flatten category tree for dropdown with hierarchy display
interface FlatCategory { id: number; label: string }
const flatCategories = computed<FlatCategory[]>(() => {
  const result: FlatCategory[] = []
  function walk(list: CourseCategoryVO[], depth: number) {
    for (const cat of list) {
      const prefix = depth > 0 ? '　'.repeat(depth) + '└ ' : ''
      result.push({ id: cat.id, label: prefix + cat.name })
      if (cat.children) walk(cat.children, depth + 1)
    }
  }
  walk(categories.value, 0)
  return result
})

onMounted(async () => {
  const catRes = await courseApi.getCategoryTree()
  categories.value = catRes.data.data ?? []
  await search()
})

async function search(pageNum = 1) {
  loading.value = true
  try {
    params.value.pageNum = pageNum
    const res = await courseApi.getList(params.value)
    const data = res.data.data
    courses.value = data?.records ?? []
    total.value = data?.total ?? 0
    // sync URL
    router.replace({ query: { ...params.value } })
  } finally {
    loading.value = false
  }
}

function onPageChange(page: number) { search(page) }
function onKeywordChange() { search(1) }
function onFilterChange() { search(1) }

let keywordTimer: ReturnType<typeof setTimeout> | null = null
watch(() => params.value.keyword, () => {
  if (keywordTimer) clearTimeout(keywordTimer)
  keywordTimer = setTimeout(() => search(1), 500)
})
</script>

<template>
  <div class="course-list-page">
    <AppHeader />
    <div class="container page-section">
      <h1 class="page-title">全部课程</h1>

      <!-- Filters -->
      <div class="filter-bar">
        <el-input
          v-model="params.keyword"
          placeholder="搜索课程名称"
          clearable
          class="search-input"
          @keyup.enter="onKeywordChange"
          @clear="onKeywordChange"
        >
          <template #prefix><el-icon><svg viewBox="0 0 24 24" width="16" height="16" fill="none" stroke="currentColor" stroke-width="2"><circle cx="11" cy="11" r="8"/><line x1="21" y1="21" x2="16.65" y2="16.65"/></svg></el-icon></template>
        </el-input>
        <el-select
          v-model="params.categoryId"
          placeholder="全部分类"
          clearable
          class="filter-select"
          @change="onFilterChange"
        >
          <el-option
            v-for="cat in flatCategories"
            :key="cat.id"
            :label="cat.label"
            :value="cat.id"
          />
        </el-select>
        <el-select
          v-model="params.sort"
          class="filter-select"
          @change="onFilterChange"
        >
          <el-option label="热门优先" value="saleCount_desc" />
          <el-option label="最新发布" value="newest" />
          <el-option label="价格从低到高" value="price_asc" />
          <el-option label="价格从高到低" value="price_desc" />
        </el-select>
      </div>

      <CourseGrid
        :courses="courses"
        :loading="loading"
        :total="total"
        :page-num="params.pageNum || 1"
        :page-size="params.pageSize || 8"
        @page-change="onPageChange"
      />
    </div>
    <AppFooter />
  </div>
</template>

<style scoped>
.page-title {
  font-size: var(--font-size-2xl);
  font-weight: 700;
  margin-bottom: var(--space-xl);
}
.filter-bar {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
  flex-wrap: wrap;
}
.search-input {
  width: 280px;
}
.filter-select {
  width: 180px;
}
</style>

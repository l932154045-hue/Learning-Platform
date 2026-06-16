<script setup lang="ts">
import { ref, watch, onMounted } from 'vue'
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
const clickedParent = ref<number | null>(null)

const params = ref<CourseSearchParams>({
  pageNum: Number(route.query.pageNum) || 1,
  pageSize: 16,
  keyword: (route.query.keyword as string) || '',
  categoryId: route.query.categoryId ? Number(route.query.categoryId) : undefined,
  sort: (route.query.sort as string) || 'saleCount_desc',
  priceMin: route.query.priceMin ? Number(route.query.priceMin) : 0,
  priceMax: route.query.priceMax ? Number(route.query.priceMax) : 99999,
})

// Find parent category ID for a given child category ID
function findParentId(childId: number): number | null {
  for (const p of categories.value) {
    if (p.children?.some(c => c.id === childId)) return p.id
  }
  return null
}

onMounted(async () => {
  const catRes = await courseApi.getCategoryTree()
  categories.value = catRes.data.data ?? []
  // Auto-expand parent if child category is selected via URL
  if (params.value.categoryId) {
    clickedParent.value = findParentId(params.value.categoryId)
  }
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
    // Clean URL: omit default values
    const q: Record<string, any> = { ...params.value }
    if (q.priceMin === 0) delete q.priceMin
    if (q.priceMax === 99999) delete q.priceMax
    router.replace({ query: q })
  } finally {
    loading.value = false
  }
}

function onPageChange(page: number) { search(page) }
function onKeywordChange() { search(1) }

function selectCategory(catId: number) {
  params.value.categoryId = catId
  clickedParent.value = null
  search(1)
}

function clearCategory() {
  params.value.categoryId = undefined
  search(1)
}

function clearPrice() {
  params.value.priceMin = 0
  params.value.priceMax = 99999
  search(1)
}

function toggleParent(id: number) {
  clickedParent.value = clickedParent.value === id ? null : id
}

function isChildSelected(parent: CourseCategoryVO): boolean {
  if (!parent.children) return false
  return parent.children.some(c => c.id === params.value.categoryId)
}

function isSubOpen(parentId: number): boolean {
  return clickedParent.value === parentId || isChildSelected(categories.value.find(c => c.id === parentId)!)
}

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

      <!-- Category Hover Menu -->
      <div class="category-section">
        <span class="category-label">分类：</span>
        <div class="category-menu">
          <span
            class="category-item"
            :class="{ active: !params.categoryId }"
            @click="clearCategory"
          >全部</span>
          <div
            v-for="parent in categories"
            :key="parent.id"
            class="category-parent"
          >
            <span
              class="category-item"
              :class="{ active: params.categoryId === parent.id || isChildSelected(parent) }"
              @click="parent.children?.length ? toggleParent(parent.id) : selectCategory(parent.id)"
            >
              {{ parent.name }}
              <span v-if="parent.children?.length" class="arrow">▾</span>
            </span>
            <div
              v-if="parent.children?.length"
              class="sub-dropdown"
              :class="{ open: isSubOpen(parent.id) }"
            >
              <div
                v-for="child in parent.children"
                :key="child.id"
                class="sub-item"
                :class="{ active: params.categoryId === child.id }"
                @click="selectCategory(child.id)"
              >
                {{ child.name }}
              </div>
            </div>
          </div>
        </div>
      </div>

      <!-- Search + Price + Sort Bar -->
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
        <div class="price-filter">
          <el-input-number
            v-model="params.priceMin"
            :min="0"
            :max="99999"
            :step="10"
            placeholder="最低价"
            controls-position="right"
            class="price-input"
          />
          <span class="price-sep">—</span>
          <el-input-number
            v-model="params.priceMax"
            :min="0"
            :max="99999"
            :step="10"
            placeholder="最高价"
            controls-position="right"
            class="price-input"
          />
          <el-button type="primary" size="small" @click="search(1)">筛选</el-button>
          <el-button size="small" text type="info" @click="clearPrice">不限</el-button>
        </div>
        <el-select
          v-model="params.sort"
          class="filter-select"
          @change="search(1)"
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
        :page-size="params.pageSize || 16"
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
  margin-bottom: var(--space-lg);
}
/* Category hover menu */
.category-section {
  display: flex;
  align-items: flex-start;
  margin-bottom: var(--space-lg);
}
.category-label {
  font-weight: 600;
  color: var(--el-text-color-regular);
  white-space: nowrap;
  padding-top: 8px;
  margin-right: var(--space-sm);
}
.category-menu {
  display: flex;
  flex-wrap: wrap;
  gap: 0;
}
.category-parent {
  position: relative;
}
.category-item {
  display: inline-block;
  padding: 6px 14px;
  cursor: pointer;
  border-radius: 6px;
  font-size: 14px;
  color: var(--el-text-color-regular);
  background: var(--el-fill-color-light);
  margin: 2px;
  transition: all .2s;
  user-select: none;
}
.category-item:hover,
.category-item.active {
  color: #fff;
  background: var(--el-color-primary);
}
.category-item .arrow {
  margin-left: 4px;
  font-size: 11px;
}
/* Subcategory dropdown — CSS-based hover + click class */
.sub-dropdown {
  position: absolute;
  top: 100%;
  left: 0;
  z-index: 100;
  min-width: 140px;
  padding: 6px 0;
  margin-top: 4px;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 4px 16px rgba(0,0,0,.12);
  border: 1px solid var(--el-border-color-light);
  opacity: 0;
  visibility: hidden;
  transition: opacity .15s, visibility .15s;
}
.category-parent:hover .sub-dropdown,
.sub-dropdown.open {
  opacity: 1;
  visibility: visible;
}
.sub-item {
  padding: 8px 16px;
  cursor: pointer;
  font-size: 13px;
  color: var(--el-text-color-regular);
  white-space: nowrap;
  transition: all .15s;
}
.sub-item:hover {
  color: var(--el-color-primary);
  background: var(--el-fill-color-light);
}
.sub-item.active {
  color: var(--el-color-primary);
  font-weight: 600;
  background: var(--el-color-primary-light-9);
}
.filter-bar {
  display: flex;
  gap: var(--space-md);
  margin-bottom: var(--space-xl);
  flex-wrap: wrap;
}
.search-input {
  width: 260px;
}
.price-filter {
  display: flex;
  align-items: center;
  gap: 6px;
}
.price-input {
  width: 120px;
}
.price-sep {
  color: var(--el-text-color-placeholder);
}
.filter-select {
  width: 180px;
}
</style>

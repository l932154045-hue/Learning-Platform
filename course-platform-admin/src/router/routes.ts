import type { RouteRecordRaw } from 'vue-router'

export const adminRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'AdminLogin',
    component: () => import('@/views/AdminLoginPage.vue'),
    meta: { public: true },
  },
  {
    path: '/',
    component: () => import('@/components/layout/AdminLayout.vue'),
    meta: { requiresAuth: true, requiresAdmin: true },
    children: [
      { path: '', name: 'Dashboard', component: () => import('@/views/DashboardPage.vue') },
      { path: 'courses', name: 'CourseManage', component: () => import('@/views/CourseManagePage.vue') },
      { path: 'course/:id/videos', name: 'ChapterVideo', component: () => import('@/views/ChapterVideoPage.vue') },
      { path: 'categories', name: 'CategoryManage', component: () => import('@/views/CategoryManagePage.vue') },
      { path: 'users', name: 'UserManage', component: () => import('@/views/UserManagePage.vue') },
    ],
  },
  { path: '/:pathMatch(.*)*', redirect: '/' },
]

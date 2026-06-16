import type { RouteRecordRaw } from 'vue-router'

export const routes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/views/public/HomePage.vue'),
    meta: { public: true },
  },
  {
    path: '/courses',
    name: 'CourseList',
    component: () => import('@/views/public/CourseListPage.vue'),
    meta: { public: true },
  },
  {
    path: '/course/:id',
    name: 'CourseDetail',
    component: () => import('@/views/public/CourseDetailPage.vue'),
    meta: { public: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/public/LoginPage.vue'),
    meta: { public: true, guestOnly: true },
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/public/RegisterPage.vue'),
    meta: { public: true, guestOnly: true },
  },
  {
    path: '/cart',
    name: 'Cart',
    component: () => import('@/views/user/ShoppingCartPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/orders',
    name: 'OrderList',
    component: () => import('@/views/user/OrderListPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/order/:id',
    name: 'OrderDetail',
    component: () => import('@/views/user/OrderDetailPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/pay-result/:orderId',
    name: 'PayResult',
    component: () => import('@/views/user/PayResultPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/my-learning',
    name: 'MyLearning',
    component: () => import('@/views/user/MyLearningPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/learning/:courseId',
    name: 'LearningDetail',
    component: () => import('@/views/user/LearningDetailPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/review/:courseId',
    name: 'WriteReview',
    component: () => import('@/views/user/MyReviewPage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/profile',
    name: 'UserProfile',
    component: () => import('@/views/user/UserProfilePage.vue'),
    meta: { requiresAuth: true },
  },
  {
    path: '/:pathMatch(.*)*',
    name: 'NotFound',
    component: () => import('@/views/NotFoundPage.vue'),
    meta: { public: true },
  },
]

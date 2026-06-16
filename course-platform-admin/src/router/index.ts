import { createRouter, createWebHistory } from 'vue-router'
import { adminRoutes } from './routes'
import { useAdminAuthStore } from '@/stores/auth'

const router = createRouter({
  history: createWebHistory(),
  routes: adminRoutes,
})

router.beforeEach(async (to, _from, next) => {
  const auth = useAdminAuthStore()

  if (!auth.initialized) await auth.restore()

  if (to.meta.public) {
    if (auth.isLoggedIn && auth.isAdmin) return next({ name: 'Dashboard' })
    return next()
  }

  if (!auth.isLoggedIn) return next({ name: 'AdminLogin' })

  if (to.meta.requiresAdmin && !auth.isAdmin) {
    auth.logout()
    return next({ name: 'AdminLogin' })
  }

  next()
})

export default router

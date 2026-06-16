import http from '../request'
import type { R, DashboardStats } from '@shared/types'

export const adminDashboardApi = {
  getStats: () => http.get<R<DashboardStats>>('/api/admin/dashboard/stats'),
}

import http from '../request'
import type { R, PageResp, OrderListItem } from '@shared/types'

export const adminOrderApi = {
  list: (params?: { pageNum?: number; pageSize?: number }) =>
    http.get<R<PageResp<OrderListItem>>>('/api/admin/order/list', { params }),
  updateStatus: (id: number, status: number) =>
    http.put<R<null>>(`/api/admin/order/${id}/status?status=${status}`),
}

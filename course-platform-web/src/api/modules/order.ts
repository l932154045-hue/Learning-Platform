import http from '../request'
import type { R } from '@shared/types'
import type { CreateOrderReq, OrderDetailVO } from '@shared/types'

export const orderApi = {
  create: (courseId: number) => http.post<R<number>>('/api/order/create', { courseId } as CreateOrderReq),
  getDetail: (id: number) => http.get<R<OrderDetailVO>>(`/api/order/detail/${id}`),
  getList: () => http.get<R<OrderDetailVO[]>>('/api/order/list'),
  cancel: (id: number) => http.put<R<null>>(`/api/order/cancel/${id}`),
}

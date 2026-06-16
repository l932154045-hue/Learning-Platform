import http from '../request'
import type { R } from '@shared/types'
import type { CartItemVO, CartAddReq } from '@shared/types'

export const cartApi = {
  getList: () => http.get<R<CartItemVO[]>>('/api/cart/list'),
  add: (courseId: number) => http.post<R<null>>('/api/cart/add', { courseId } as CartAddReq),
  remove: (courseId: number) => http.delete<R<null>>(`/api/cart/remove/${courseId}`),
  clear: () => http.delete<R<null>>('/api/cart/clear'),
}

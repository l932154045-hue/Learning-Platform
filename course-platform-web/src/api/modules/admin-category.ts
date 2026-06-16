import http from '@/api/request'
import type { R } from '@shared/types'

export const adminCategoryApi = {
  create: (name: string, parentId = 0, sortOrder = 0) =>
    http.post<R<null>>('/api/admin/category', null, { params: { name, parentId, sortOrder } }),
  update: (id: number, name: string, sortOrder: number) =>
    http.put<R<null>>(`/api/admin/category/${id}`, null, { params: { name, sortOrder } }),
  delete: (id: number) => http.delete<R<null>>(`/api/admin/category/${id}`),
}

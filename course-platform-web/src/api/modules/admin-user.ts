import http from '@/api/request'
import type { R, PageResp, UserListItem, UserStatusReq } from '@shared/types'

export const adminUserApi = {
  list: (params?: { pageNum?: number; pageSize?: number }) =>
    http.get<R<PageResp<UserListItem>>>('/api/admin/user/list', { params }),
  updateStatus: (id: number, status: number) =>
    http.put<R<null>>(`/api/admin/user/${id}/status`, { status } as UserStatusReq),
}

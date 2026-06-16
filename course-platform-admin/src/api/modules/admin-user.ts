import http from '../request'
import type { R } from '@shared/types'
import type { UserStatusReq } from '@shared/types'

export const adminUserApi = {
  updateStatus: (id: number, status: number) =>
    http.put<R<null>>(`/api/admin/user/${id}/status`, { status } as UserStatusReq),
}

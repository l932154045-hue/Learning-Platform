import http from '../request'
import type { R } from '@shared/types'
import type { LoginReq, RegisterReq, LoginResp, UserInfoResp, UpdateUserReq } from '@shared/types'

export const userApi = {
  register: (data: RegisterReq) => http.post<R<null>>('/api/user/register', data),
  login: (data: LoginReq) => http.post<R<LoginResp>>('/api/user/login', data),
  getInfo: () => http.get<R<UserInfoResp>>('/api/user/info'),
  updateInfo: (data: UpdateUserReq) => http.put<R<null>>('/api/user/info', data),
}

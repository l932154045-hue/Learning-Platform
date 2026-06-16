import http from '../request'
import type { R } from '@shared/types'
import type { CourseDetailVO } from '@shared/types'

export const courseApi = {
  getDetail: (id: number) => http.get<R<CourseDetailVO>>(`/api/course/detail/${id}`),
}

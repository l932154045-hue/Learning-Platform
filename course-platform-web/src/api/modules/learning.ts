import http from '../request'
import type { R } from '@shared/types'
import type { MyCourseVO, ProgressVO, ProgressReportReq } from '@shared/types'

export const learningApi = {
  getMyCourses: () => http.get<R<MyCourseVO[]>>('/api/learning/my-courses'),
  getProgress: (courseId: number) => http.get<R<ProgressVO[]>>(`/api/learning/progress/${courseId}`),
  reportProgress: (data: ProgressReportReq) => http.put<R<null>>('/api/learning/progress/report', data),
}

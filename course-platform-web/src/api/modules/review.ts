import http from '../request'
import type { R } from '@shared/types'
import type { ReviewVO, ReviewReq } from '@shared/types'

export const reviewApi = {
  getMyReview: (courseId: number) => http.get<R<ReviewVO>>(`/api/learning/review/${courseId}`),
  submit: (data: ReviewReq) => http.post<R<null>>('/api/learning/review', data),
  getCourseReviews: (courseId: number) => http.get<R<ReviewVO[]>>(`/api/learning/course/${courseId}/reviews`),
}

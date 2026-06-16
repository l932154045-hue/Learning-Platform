import http from '../request'
import type { R } from '@shared/types'
import type { CourseSaveReq, VideoSaveReq, CourseListItemVO, CourseCategoryVO } from '@shared/types'

export const adminCourseApi = {
  create: (data: CourseSaveReq) => http.post<R<null>>('/api/admin/course', data),
  update: (id: number, data: CourseSaveReq) => http.put<R<null>>(`/api/admin/course/${id}`, data),
  delete: (id: number) => http.delete<R<null>>(`/api/admin/course/${id}`),
  updateStatus: (id: number, status: number) => http.put<R<null>>(`/api/admin/course/${id}/status?status=${status}`),
  addVideo: (data: VideoSaveReq) => http.post<R<null>>(`/api/admin/course/${data.courseId}/video`, data),
  updateVideo: (id: number, data: VideoSaveReq) => http.put<R<null>>(`/api/admin/course/video/${id}`, data),
  deleteVideo: (id: number) => http.delete<R<null>>(`/api/admin/course/video/${id}`),
  // Use public endpoints to list courses
  getCourses: () => http.get<R<{ records: CourseListItemVO[]; total: number }>>('/api/course/list', { params: { pageSize: 100 } }),
  getCategories: () => http.get<R<CourseCategoryVO[]>>('/api/course/category/tree'),
  refreshCache: () => http.post<R<string>>('/api/course/cache/refresh'),
}

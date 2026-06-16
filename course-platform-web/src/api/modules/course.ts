import http from '../request'
import type { R, PageResp } from '@shared/types'
import type { CourseCategoryVO, CourseDetailVO, CourseListItemVO, CourseSearchParams } from '@shared/types'

export const courseApi = {
  getCategoryTree: () => http.get<R<CourseCategoryVO[]>>('/api/course/category/tree'),
  getList: (params: CourseSearchParams) => http.get<R<PageResp<CourseListItemVO>>>('/api/course/list', { params }),
  getDetail: (id: number) => http.get<R<CourseDetailVO>>(`/api/course/detail/${id}`),
  getHot: () => http.get<R<CourseListItemVO[]>>('/api/course/hot'),
}

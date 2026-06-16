// ===== Response Wrappers =====
export interface R<T> {
  code: number
  message: string
  data: T
  timestamp: number
}

export interface PageResp<T> {
  records: T[]
  total: number
  pageNum: number
  pageSize: number
  totalPages: number
}

// ===== User =====
export interface LoginReq {
  phone: string
  password: string
}

export interface RegisterReq {
  username: string
  password: string
  phone: string
}

export interface LoginResp {
  token: string
  userId: number
  nickname: string
  role: number
}

export interface UserInfoResp {
  id: number
  username: string
  nickname: string
  phone: string
  email: string | null
  avatarUrl: string | null
  role: number
}

export interface UpdateUserReq {
  nickname?: string
  email?: string
  avatarUrl?: string
}

// ===== Course =====
export interface CourseCategoryVO {
  id: number
  name: string
  parentId: number
  sortOrder: number
  children: CourseCategoryVO[] | null
}

export interface CourseListItemVO {
  id: number
  title: string
  coverUrl: string
  teacherName: string
  price: number
  saleCount: number
  categoryId: number
  categoryName: string
  status: number
}

export interface ChapterVideoVO {
  id: number
  chapterTitle: string
  videoTitle: string
  videoUrl: string
  duration: number
  sortOrder: number
}

export interface CourseDetailVO {
  id: number
  title: string
  description: string
  coverUrl: string
  categoryName: string
  teacherName: string
  price: number
  saleCount: number
  status: number
  chapters: ChapterVideoVO[]
}

export interface CourseSearchParams {
  categoryId?: number
  keyword?: string
  sort?: string
  priceMin?: number
  priceMax?: number
  pageNum?: number
  pageSize?: number
}

// ===== Cart =====
export interface CartAddReq {
  courseId: number
}

export interface CartItemVO {
  cartId: number
  courseId: number
  courseTitle: string
  coverUrl: string
  teacherName: string
  price: number
}

// ===== Order =====
export interface CreateOrderReq {
  courseId: number
}

export interface OrderItem {
  id: number
  orderId: number
  courseId: number
  courseTitle: string
  price: number
}

export interface OrderDetailVO {
  id: number
  orderNo: string
  userId: number
  totalAmount: number
  status: number
  statusDesc: string
  paidAt: string | null
  createdAt: string
  orderItems: OrderItem[]
}

// ===== Payment =====
export interface PayResultVO {
  paymentNo: string
  orderNo: string
  amount: number
  status: number
  statusDesc: string
  paidAt: string | null
}

// ===== Learning =====
export interface MyCourseVO {
  enrollmentId: number
  courseId: number
  courseTitle: string
  courseCover: string
  teacherName: string
  price: number
  totalProgress: number
  videoCount: number
  finishedVideoCount: number
  enrolledAt: string
  lastLearnedAt: string | null
}

export interface ProgressVO {
  videoId: number
  courseId: number
  progressSeconds: number
  duration: number
  finished: boolean
  updatedAt: string
}

export interface ProgressReportReq {
  videoId: number
  courseId: number
  progressSeconds: number
  duration: number
}

// ===== Review =====
export interface ReviewVO {
  id: number
  userId: number
  nickname: string
  avatarUrl: string | null
  rating: number
  content: string
  createdAt: string
}

export interface ReviewReq {
  courseId: number
  rating: number
  content: string
}

// ===== Admin =====
export interface CourseSaveReq {
  title: string
  description?: string
  coverUrl?: string
  categoryId: number
  teacherName?: string
  price: number
}

export interface VideoSaveReq {
  courseId: number
  chapterTitle: string
  videoTitle: string
  videoUrl: string
  duration: number
  sortOrder: number
}

export interface UserStatusReq {
  status: number
}

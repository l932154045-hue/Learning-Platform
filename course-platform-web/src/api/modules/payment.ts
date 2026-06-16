import http from '../request'
import type { R } from '@shared/types'
import type { PayResultVO } from '@shared/types'

export const paymentApi = {
  pay: (orderId: number) => http.post<R<PayResultVO>>(`/api/payment/pay/${orderId}`),
  getResult: (orderId: number) => http.get<R<PayResultVO>>(`/api/payment/result/${orderId}`),
}

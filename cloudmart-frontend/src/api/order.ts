import request from './request'
import type { Order, OrderVO, Page } from '@/types'

export interface CreateOrderForm {
  addressId: number
  cartFingerprint: string
  addressSnapshot?: string
  userCouponId?: number | null
}

export const orderApi = {
  create: (data: CreateOrderForm, idempotencyKey: string) =>
    request.post<any, Order>('/orders', data, {
      headers: {
        'Idempotency-Key': idempotencyKey,
      },
    }),
  list: (params: object) => request.get<any, Page<Order>>('/orders', { params }),
  detail: (id: number) => request.get<any, OrderVO>(`/orders/${id}`),
  pay: (id: number) => request.post<any, void>(`/orders/${id}/pay`),
  cancel: (id: number) => request.post<any, void>(`/orders/${id}/cancel`),
  receive: (id: number) => request.post<any, void>(`/orders/${id}/receive`),
  ship: (id: number) => request.put<any, void>(`/orders/${id}/ship`),
}

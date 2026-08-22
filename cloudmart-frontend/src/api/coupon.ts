import request from './request'
import type { Coupon, UserCoupon } from '@/types'

export const couponApi = {
  list: () => request.get<any, Coupon[]>('/coupons'),
  claim: (id: number) => request.post<any, void>(`/coupons/${id}/claim`),
  mine: () => request.get<any, UserCoupon[]>('/coupons/mine'),
}

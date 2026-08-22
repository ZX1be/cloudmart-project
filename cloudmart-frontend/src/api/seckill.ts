import request from './request'
import type { SeckillActivity } from '@/types'

export const seckillApi = {
  activities: () => request.get<any, SeckillActivity[]>('/seckill/activities'),
  buy: (activityId: number, addressId: number) =>
    request.post<any, number>(`/seckill/${activityId}/buy`, null, {
      params: { addressId },
    }),
}

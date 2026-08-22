import request from './request'
import type { Address } from '@/types'

export interface AddressForm {
  receiverName: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault?: number
}

export const addressApi = {
  list: () => request.get<any, Address[]>('/auth/address/info'),
  save: (data: AddressForm) => request.post<any, void>('/auth/address/info', data),
  update: (id: number, data: AddressForm) =>
    request.put<any, void>(`/auth/address/${id}`, data),
  remove: (id: number) => request.delete<any, void>(`/auth/address/${id}`),
}

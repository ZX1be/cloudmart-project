import request from './request'
import type { CartItem } from '@/types'

export const cartApi = {
  list: () => request.get<any, CartItem[]>('/cart'),
  add: (productId: number, quantity = 1) =>
    request.post<any, void>('/cart', null, { params: { productId, quantity } }),
  updateQuantity: (id: number, quantity: number) =>
    request.put<any, void>(`/cart/${id}`, null, { params: { quantity } }),
  updateSelected: (id: number, selected: number) =>
    request.put<any, void>(`/cart/${id}/selected`, null, { params: { selected } }),
  remove: (id: number) => request.delete<any, void>(`/cart/${id}`),
}

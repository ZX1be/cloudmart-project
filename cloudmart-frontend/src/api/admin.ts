import request from './request'
import type { Category, Coupon, Order, Page, Product, SeckillActivity } from '@/types'

export const adminApi = {
  products: (params: object) => request.get<any, Page<Product>>('/admin/products', { params }),
  createProduct: (data: object) => request.post<any, void>('/admin/products', data),
  updateProduct: (id: number, data: object) =>
    request.put<any, void>(`/admin/products/${id}`, data),
  deleteProduct: (id: number) => request.delete<any, void>(`/admin/products/${id}`),
  categories: () => request.get<any, Category[]>('/admin/categories'),
  createCategory: (data: object) => request.post<any, void>('/admin/categories', data),
  updateCategory: (id: number, data: object) =>
    request.put<any, void>(`/admin/categories/${id}`, data),
  deleteCategory: (id: number) => request.delete<any, void>(`/admin/categories/${id}`),
  orders: (params: object) => request.get<any, Page<Order>>('/admin/orders', { params }),
  shipOrder: (id: number) => request.put<any, void>(`/orders/${id}/ship`),
  coupons: () => request.get<any, Coupon[]>('/admin/coupons'),
  createCoupon: (data: object) => request.post<any, void>('/admin/coupons', data),
  seckillActivities: () => request.get<any, SeckillActivity[]>('/admin/seckill'),
  createSeckill: (data: object) => request.post<any, void>('/admin/seckill', data),
  warmUpSeckill: (id: number) => request.post<any, void>(`/admin/seckill/${id}/warmup`),
}

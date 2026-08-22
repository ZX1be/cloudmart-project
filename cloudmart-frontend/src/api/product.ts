import request from './request'
import type { Category, Favorite, Page, Product, Review } from '@/types'

export const productApi = {
  list: (params: object) => request.get<any, Page<Product>>('/products', { params }),
  detail: (id: number) => request.get<any, Product>(`/products/${id}`),
  reviews: (productId: number, page = 1, size = 10) =>
    request.get<any, Page<Review>>(`/products/${productId}/reviews`, {
      params: { page, size },
    }),
  addReview: (productId: number, data: object) =>
    request.post<any, void>(`/products/${productId}/reviews`, data),
}

export const categoryApi = {
  tree: () => request.get<any, Category[]>('/categories/tree'),
}

export const favoriteApi = {
  list: () => request.get<any, Favorite[]>('/favorites'),
  add: (productId: number) => request.post<any, void>(`/favorites/${productId}`),
  remove: (productId: number) => request.delete<any, void>(`/favorites/${productId}`),
}

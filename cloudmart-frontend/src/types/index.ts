export interface Page<T> {
  records: T[]
  total: number
  current: number
  size: number
  pages: number
}

export interface LoginResult {
  token: string
  userId: number
  username: string
  avatar?: string
  role: string
}

export interface UserInfo {
  id: number
  username: string
  phone?: string
  email?: string
  avatar?: string
  role: string
  status: number
}

export interface Address {
  id: number
  receiverName: string
  phone: string
  province: string
  city: string
  district: string
  detail: string
  isDefault: number
}

export interface Category {
  id: number
  parentId: number
  name: string
  icon?: string
  sortOrder?: number
  status?: number
  children?: Category[]
}

export interface Product {
  id: number
  name: string
  description?: string
  price: number
  stock: number
  categoryId: number
  images?: string
  status: number
  sales: number
}

export interface Review {
  id: number
  userId: number
  productId: number
  orderId?: number
  rating: number
  content?: string
  createdAt?: string
}

export interface Favorite {
  id: number
  userId: number
  productId: number
  createdAt?: string
}

export interface CartItem {
  id: number
  userId: number
  productId: number
  quantity: number
  selected: number
  createdAt?: string
}

export interface Order {
  id: number
  orderNo: string
  userId: number
  addressId?: number
  totalAmount: number
  discountAmount: number
  payAmount: number
  status: string
  couponId?: number
  createdAt?: string
}

export interface OrderItem {
  id: number
  orderId: number
  productId: number
  productName: string
  productImage?: string
  price: number
  quantity: number
  amount: number
}

export interface OrderVO extends Order {
  items: OrderItem[]
}

export interface Coupon {
  id: number
  name: string
  type: string
  discountValue: number
  minAmount: number
  totalCount: number
  usedCount: number
  startTime: string
  endTime: string
  status: number
}

export interface UserCoupon {
  id: number
  userId: number
  couponId: number
  status: string
  createdAt?: string
}

export interface SeckillActivity {
  id: number
  productId: number
  seckillPrice: number
  stock: number
  realStock?: number
  startTime: string
  endTime: string
  status: number
}

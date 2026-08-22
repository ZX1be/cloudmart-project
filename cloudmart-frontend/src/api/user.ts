import request from './request'
import type { UserInfo } from '@/types'

export const userApi = {
  info: () => request.get<any, UserInfo>('/auth/user/info'),
  update: (data: Partial<UserInfo>) => request.put<any, void>('/auth/user/info', data),
}

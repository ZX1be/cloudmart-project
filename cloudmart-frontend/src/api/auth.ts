import request from './request'
import type { LoginResult } from '@/types'

export const authApi = {
  login: (data: { username: string; password: string }) =>
    request.post<any, LoginResult>('/auth/login', data),
  register: (data: { username: string; password: string; phone?: string }) =>
    request.post<any, void>('/auth/register', data),
}

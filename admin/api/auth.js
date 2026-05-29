import { post } from './request'

export const login = data => post('/api/admin/auth/login', data)

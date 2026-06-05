import { get, put } from './request'

export const listUsers = params => get('/api/admin/users', params)
export const getUserDetail = id => get(`/api/admin/users/${id}`)
export const updateUserStatus = (id, data) => put(`/api/admin/users/${id}/status`, data)
export const updateUserRole = (id, data) => put(`/api/admin/users/${id}/role`, data)

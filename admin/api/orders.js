import { get, put } from './request'

export const listOrders = params => get('/api/admin/orders', params)
export const getOrderDetail = id => get(`/api/admin/orders/${id}`)
export const listOrderLogs = id => get(`/api/admin/orders/${id}/logs`)
export const cancelOrder = (id, data) => put(`/api/admin/orders/${id}/cancel`, data)

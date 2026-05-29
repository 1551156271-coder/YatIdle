import { get, put, del } from './request'

export const listItems = params => get('/api/admin/items', params)
export const getItemDetail = id => get(`/api/admin/items/${id}`)
export const updateItemStatus = (id, data) => put(`/api/admin/items/${id}/status`, data)
export const deleteItem = (id, data) => del(`/api/admin/items/${id}`, data)

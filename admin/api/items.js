import { get, put } from './request'

export const listItems = params => get('/api/admin/items', params)
export const getItemDetail = id => get(`/api/admin/items/${id}`)
export const updateItemStatus = (id, data) => put(`/api/admin/items/${id}/status`, data)
export const deleteItem = (id, data) => put(`/api/admin/items/${id}/delete`, data)

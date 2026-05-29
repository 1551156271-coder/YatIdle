import { get, put, del } from './request'

export const listWanted = params => get('/api/admin/wanted', params)
export const getWantedDetail = id => get(`/api/admin/wanted/${id}`)
export const updateWantedStatus = (id, data) => put(`/api/admin/wanted/${id}/status`, data)
export const deleteWanted = (id, data) => del(`/api/admin/wanted/${id}`, data)

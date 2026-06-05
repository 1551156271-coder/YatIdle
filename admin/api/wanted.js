import { get, put } from './request'

export const listWanted = params => get('/api/admin/wanted', params)
export const getWantedDetail = id => get(`/api/admin/wanted/${id}`)
export const updateWantedStatus = (id, data) => put(`/api/admin/wanted/${id}/status`, data)
export const deleteWanted = (id, data) => put(`/api/admin/wanted/${id}/delete`, data)

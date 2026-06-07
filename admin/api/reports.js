import { get, put } from './request'

export const listReports = params => get('/api/admin/reports', params)
export const getReportDetail = id => get(`/api/admin/reports/${id}`)
export const handleReport = (id, data) => put(`/api/admin/reports/${id}/handle`, data)
export const restoreReportAction = (id, data) => put(`/api/admin/reports/${id}/restore`, data)

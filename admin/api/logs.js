import { get } from './request'

export const listLogs = params => get('/api/admin/logs', params)

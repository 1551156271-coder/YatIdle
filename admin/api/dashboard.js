import { get } from './request'

export const overview = () => get('/api/admin/stats/overview')

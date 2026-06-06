import { get } from './request'

export const listSessions = params => get('/api/admin/chats/sessions', params)
export const listMessages = id => get(`/api/admin/chats/sessions/${id}/messages`)

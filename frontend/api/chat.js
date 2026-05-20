import { get, post, put } from './index'

export function createSession(userId, data) {
  return post('/api/chat/sessions?userId=' + userId, data)
}

export function getMySessions(userId, pageNum = 1, pageSize = 10) {
  return get('/api/chat/sessions', { userId, pageNum, pageSize })
}

export function sendMessage(userId, data) {
  return post('/api/chat/messages?userId=' + userId, data)
}

export function getMessages(sessionId, userId, pageNum = 1, pageSize = 20) {
  return get('/api/chat/sessions/' + sessionId + '/messages', { userId, pageNum, pageSize })
}

export function markRead(sessionId, userId) {
  return put('/api/chat/sessions/' + sessionId + '/read?userId=' + userId)
}

import { get, post, put } from './index'

export function createSession(userId, data) {
  return post('/api/chat/sessions?userId=' + userId, data)
}

export function getMySessions(userId) {
  return get('/api/chat/sessions', { userId })
}

export function sendMessage(userId, data) {
  return post('/api/chat/messages?userId=' + userId, data)
}

export function getMessages(sessionId, userId) {
  return get('/api/chat/sessions/' + sessionId + '/messages', { userId })
}

export function markRead(sessionId, userId) {
  return put('/api/chat/sessions/' + sessionId + '/read?userId=' + userId)
}

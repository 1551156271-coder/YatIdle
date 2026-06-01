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

export function uploadChatImage(filePath) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: 'http://127.0.0.1:8080/api/chat/images/upload',
      filePath: filePath,
      name: 'file',
      success(res) {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 200) {
            resolve(body.data.url)
          } else {
            uni.showToast({ title: body.message || '上传失败', icon: 'none' })
            reject(body)
          }
        } catch (e) {
          reject(e)
        }
      },
      fail(err) {
        uni.showToast({ title: '图片上传失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

import { get, post, put, MOCK } from './index'

export function getCategories() {
  return get('/api/categories')
}

export function publishItem(data) {
  return post('/api/items/publish', data)
}

export function getItemDetail(id) {
  return get('/api/items/' + id)
}

export function searchItems(params) {
  return get('/api/items/search', params)
}

export function getUserItems(userId, params) {
  return get('/api/items/user/' + userId, params)
}

export function updateItem(id, data) {
  return put('/api/items/' + id, data)
}

export function offlineItem(id, userId) {
  return put('/api/items/' + id + '/offline?userId=' + userId)
}

export function onlineItem(id, userId) {
  return put('/api/items/' + id + '/online?userId=' + userId)
}

export function uploadImage(filePath) {
  if (MOCK) {
    return Promise.resolve('/uploads/items/mock_' + Date.now() + '.jpg')
  }
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: 'http://localhost:8080/api/items/images/upload',
      filePath,
      name: 'file',
      success(res) {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 200) {
            resolve(body.data.url)
          } else {
            reject(body)
          }
        } catch (e) {
          reject(res)
        }
      },
      fail: reject
    })
  })
}

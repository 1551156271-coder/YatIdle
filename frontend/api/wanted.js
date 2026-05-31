import { get, post, put, del } from './index'

export function publishWanted(data) {
  return post('/api/wanted', data)
}

export function getWantedList(params) {
  return get('/api/wanted/list', params)
}

export function getWantedDetail(id) {
  return get('/api/wanted/' + id)
}

export function updateWanted(id, data) {
  return put('/api/wanted/' + id, data)
}

export function deleteWanted(id) {
  return del('/api/wanted/' + id)
}

export function deleteAllMyWanted(userId) {
  return del('/api/wanted/user/' + userId)
}

export function getMyWanted(userId) {
  return get('/api/wanted/my', { userId })
}

export function closeWanted(id, userId) {
  return put('/api/wanted/' + id + '/close?userId=' + userId)
}

import { get, post, put } from './index'

export function createOrder(userId, data) {
  return post('/api/orders?userId=' + userId, data)
}

export function getMyBuyOrders(userId, pageNum = 1, pageSize = 10) {
  return get('/api/orders/my-buy', { userId, pageNum, pageSize })
}

export function getMySellOrders(userId, pageNum = 1, pageSize = 10) {
  return get('/api/orders/my-sell', { userId, pageNum, pageSize })
}

export function cancelOrder(id, userId, data) {
  return put('/api/orders/' + id + '/cancel?userId=' + userId, data || {})
}

export function completeOrder(id, userId) {
  return put('/api/orders/' + id + '/complete?userId=' + userId)
}

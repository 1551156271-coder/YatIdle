import { get, post, put } from './index'

export function createOrder(userId, data) {
  return post('/api/orders?userId=' + userId, data)
}

export function getMyBuyOrders(userId) {
  return get('/api/orders/my-buy', { userId })
}

export function getMySellOrders(userId) {
  return get('/api/orders/my-sell', { userId })
}

export function cancelOrder(id, userId, data) {
  return put('/api/orders/' + id + '/cancel?userId=' + userId, data || {})
}

export function completeOrder(id, userId) {
  return put('/api/orders/' + id + '/complete?userId=' + userId)
}

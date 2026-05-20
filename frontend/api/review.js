import { get } from './index'

export function getMyReviews(userId) {
  return get('/api/reviews', { userId })
}

export function getMyCredit(userId) {
  return get('/api/user-credit', { userId })
}

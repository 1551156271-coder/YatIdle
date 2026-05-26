import { get, post, put, del } from './index'

export function submitReview(userId, data) {
  return post('/api/reviews?userId=' + userId, data)
}

export function updateReview(id, userId, data) {
  return put('/api/reviews/' + id + '?userId=' + userId, data)
}

export function deleteReview(id, userId) {
  return del('/api/reviews/' + id + '?userId=' + userId)
}

export function getUserReviews(userId) {
  return get('/api/reviews/user/' + userId)
}

export function getMyReviews(userId) {
  return get('/api/reviews/my', { userId })
}

export function checkReviewed(orderId, userId) {
  return get('/api/reviews/check', { orderId, userId })
}

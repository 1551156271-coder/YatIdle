import { get, del } from './request'

export const listReviews = params => get('/api/admin/reviews', params)
export const getReviewDetail = id => get(`/api/admin/reviews/${id}`)
export const deleteReview = (id, data) => del(`/api/admin/reviews/${id}`, data)

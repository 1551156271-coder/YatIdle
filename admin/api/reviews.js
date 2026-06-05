import { get, put } from './request'

export const listReviews = params => get('/api/admin/reviews', params)
export const getReviewDetail = id => get(`/api/admin/reviews/${id}`)
export const deleteReview = (id, data) => put(`/api/admin/reviews/${id}/delete`, data)

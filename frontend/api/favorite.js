import { get, post, del } from './index'

export function addFavorite(itemId, userId) {
  return post('/api/favorites/' + itemId + '?userId=' + userId)
}

export function removeFavorite(itemId, userId) {
  return del('/api/favorites/' + itemId + '?userId=' + userId)
}

export function getMyFavorites(userId, pageNum = 1, pageSize = 10) {
  return get('/api/favorites', { userId, pageNum, pageSize })
}

export function addWantedFavorite(wantedId, userId) {
  return post('/api/favorites/wanted/' + wantedId + '?userId=' + userId)
}

export function removeWantedFavorite(wantedId, userId) {
  return del('/api/favorites/wanted/' + wantedId + '?userId=' + userId)
}

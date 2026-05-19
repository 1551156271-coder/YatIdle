import { get, post, del } from './index'

export function addFavorite(itemId, userId) {
  return post('/api/favorites/' + itemId + '?userId=' + userId)
}

export function removeFavorite(itemId, userId) {
  return del('/api/favorites/' + itemId + '?userId=' + userId)
}

export function getMyFavorites(userId) {
  return get('/api/favorites', { userId })
}

import { post, get, put } from './index'

export function register(username, password) {
	return post('/api/user/register', { username, password })
}

export function login(username, password) {
	return post('/api/user/login', { username, password })
}

export function getUserInfo(id) {
	return get('/api/user/' + id)
}

export function updateProfile(data) {
	return put('/api/user/profile', data)
}

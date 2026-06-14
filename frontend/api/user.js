import { post, get, put, del, resolveUploadStorageUrl, API_BASE_URL } from './index'

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

export function deleteUser(id) {
	return del('/api/user/' + id)
}

export function uploadAvatar(filePath) {
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: API_BASE_URL + '/api/user/avatar/upload',
			filePath,
			name: 'file',
			success(res) {
				try {
					const body = JSON.parse(res.data)
					if (body.code === 200) {
						resolve(resolveUploadStorageUrl(body.data.url))
					} else {
						reject(body)
					}
				} catch (e) {
					reject(res)
				}
			},
			fail: reject
		})
	})
}

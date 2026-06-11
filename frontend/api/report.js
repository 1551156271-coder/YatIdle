import { API_BASE_URL, post, resolveImageUrl } from './index'

export function createReport(data) {
	return post('/api/reports', data)
}

export function uploadReportImage(filePath) {
	return new Promise((resolve, reject) => {
		uni.uploadFile({
			url: API_BASE_URL + '/api/reports/images/upload',
			filePath,
			name: 'file',
			header: {
				'Authorization': 'Bearer ' + (uni.getStorageSync('token') || '')
			},
			success(res) {
				try {
					const body = typeof res.data === 'string' ? JSON.parse(res.data) : res.data
					if (res.statusCode === 200 && body.code === 200) {
						resolve(resolveImageUrl(body.data.url))
					} else {
						reject(body)
					}
				} catch (e) {
					reject(e)
				}
			},
			fail: reject
		})
	})
}

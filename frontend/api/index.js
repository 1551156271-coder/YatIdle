const BASE_URL = 'http://localhost:8080'
const TIMEOUT = 10000

function request(method, url, data) {
	return new Promise((resolve, reject) => {
		uni.request({
			url: BASE_URL + url,
			method,
			data,
			timeout: TIMEOUT,
			header: {
				'Content-Type': 'application/json'
			},
			success(res) {
				if (res.statusCode === 200) {
					const body = res.data
					if (body.code === 200) {
						resolve(body.data)
					} else {
						uni.showToast({ title: body.message || '请求失败', icon: 'none' })
						reject(body)
					}
				} else {
					uni.showToast({ title: '服务器错误 ' + res.statusCode, icon: 'none' })
					reject(res)
				}
			},
			fail(err) {
				uni.showToast({ title: '网络请求失败', icon: 'none' })
				reject(err)
			}
		})
	})
}

export function get(url, params) {
	return request('GET', url, params)
}

export function post(url, data) {
	return request('POST', url, data)
}

export function put(url, data) {
	return request('PUT', url, data)
}

export function del(url, data) {
	return request('DELETE', url, data)
}

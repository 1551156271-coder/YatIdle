const BASE_URL = 'http://127.0.0.1:8080'
const TIMEOUT = 10000

function request(method, url, data) {
  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      timeout: TIMEOUT,
      header: {
        'Content-Type': 'application/json',
        Authorization: 'Bearer ' + (uni.getStorageSync('adminToken') || '')
      },
      success(res) {
        const body = res.data || {}
        if (res.statusCode === 200 && body.code === 200) {
          resolve(body.data)
          return
        }
        if (body.message === '未登录' || body.message === '登录已失效' || body.message === '无管理员权限') {
          uni.removeStorageSync('adminToken')
          uni.removeStorageSync('adminUser')
          uni.reLaunch({ url: '/pages/login/login' })
        }
        uni.showToast({ title: body.message || '请求失败', icon: 'none' })
        reject(body)
      },
      fail(err) {
        uni.showToast({ title: '网络请求失败', icon: 'none' })
        reject(err)
      }
    })
  })
}

export const get = (url, params) => request('GET', url, params)
export const post = (url, data) => request('POST', url, data)
export const put = (url, data) => request('PUT', url, data)
export const del = (url, data) => request('DELETE', url, data)

const DEFAULT_API_BASE_URL = 'http://127.0.0.1:8080'
const LOCAL_HOSTS = new Set(['127.0.0.1', 'localhost'])

export function getRuntimeApiBaseUrl(locationLike) {
  const location = locationLike || getBrowserLocation()
  if (location && location.hostname) {
    const protocol = location.protocol === 'https:' ? 'https:' : 'http:'
    return `${protocol}//${location.hostname}:8080`
  }
  return DEFAULT_API_BASE_URL
}

export function resolveImageUrlWithBase(url, baseUrl) {
  if (!url) return ''
  const text = String(url)
  const apiBase = baseUrl || DEFAULT_API_BASE_URL

  try {
    const parsed = new URL(text)
    if (LOCAL_HOSTS.has(parsed.hostname)) {
      const runtimeBase = new URL(apiBase)
      parsed.protocol = runtimeBase.protocol
      parsed.hostname = runtimeBase.hostname
      parsed.port = runtimeBase.port
      return parsed.toString()
    }
    return text
  } catch (e) {
    if (text.startsWith('/')) return apiBase + text
    return apiBase + '/' + text
  }
}

export function createNetworkError(err, requestUrl) {
  return {
    ...(err || {}),
    requestUrl,
    friendlyMessage: '网络请求失败，请确认手机和电脑在同一 Wi-Fi，且后端 8080 端口可访问'
  }
}

function getBrowserLocation() {
  // #ifdef H5
  if (typeof window !== 'undefined' && window.location) {
    return window.location
  }
  // #endif
  return null
}

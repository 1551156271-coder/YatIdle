import test from 'node:test'
import assert from 'node:assert/strict'

import {
  getRuntimeApiBaseUrl,
  resolveImageUrlWithBase,
  createNetworkError
} from './runtime.js'

test('H5 runtime API base follows the current browser host on port 8080', () => {
  const location = { protocol: 'http:', hostname: '172.18.156.250' }

  assert.equal(getRuntimeApiBaseUrl(location), 'http://172.18.156.250:8080')
})

test('local browser host resolves to localhost backend', () => {
  const location = { protocol: 'http:', hostname: 'localhost' }

  assert.equal(getRuntimeApiBaseUrl(location), 'http://localhost:8080')
})

test('image URLs returned from local backend are rewritten to runtime API base', () => {
  const baseUrl = 'http://172.18.156.250:8080'

  assert.equal(
    resolveImageUrlWithBase('http://127.0.0.1:8080/uploads/items/a.jpg', baseUrl),
    'http://172.18.156.250:8080/uploads/items/a.jpg'
  )
  assert.equal(
    resolveImageUrlWithBase('http://localhost:8080/uploads/items/a.jpg', baseUrl),
    'http://172.18.156.250:8080/uploads/items/a.jpg'
  )
  assert.equal(
    resolveImageUrlWithBase('/uploads/items/a.jpg', baseUrl),
    'http://172.18.156.250:8080/uploads/items/a.jpg'
  )
})

test('network error wrapper carries request URL and friendly H5 hint', () => {
  const error = createNetworkError({ errMsg: 'request:fail' }, 'http://172.18.156.250:8080/api/categories')

  assert.equal(error.requestUrl, 'http://172.18.156.250:8080/api/categories')
  assert.match(error.friendlyMessage, /8080/)
})

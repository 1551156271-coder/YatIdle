import test from 'node:test'
import assert from 'node:assert/strict'

import {
  getRuntimeApiBaseUrl,
  resolveImageUrlWithBase,
  resolveUploadStorageUrlWithBase,
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
  assert.equal(
    resolveImageUrlWithBase('http://192.168.1.20:8080/uploads/items/a.jpg', baseUrl),
    'http://172.18.156.250:8080/uploads/items/a.jpg'
  )
  assert.equal(
    resolveImageUrlWithBase('http://172.18.156.250:8080/uploads/items/a.jpg', 'http://localhost:8080'),
    'http://localhost:8080/uploads/items/a.jpg'
  )
  assert.equal(
    resolveImageUrlWithBase('blob:http://172.18.156.250:5173/temp-image', baseUrl),
    'blob:http://172.18.156.250:5173/temp-image'
  )
  assert.equal(
    resolveImageUrlWithBase('_doc/uniapp_temp_1.jpg', baseUrl),
    '_doc/uniapp_temp_1.jpg'
  )
})

test('network error wrapper carries request URL and friendly H5 hint', () => {
  const error = createNetworkError({ errMsg: 'request:fail' }, 'http://172.18.156.250:8080/api/categories')

  assert.equal(error.requestUrl, 'http://172.18.156.250:8080/api/categories')
  assert.match(error.friendlyMessage, /8080/)
})

test('uploaded image storage URL stays relative before sending to backend', () => {
  const baseUrl = 'http://172.18.156.250:8080'

  assert.equal(
    resolveUploadStorageUrlWithBase('http://172.18.156.250:8080/uploads/chat/a.jpg', baseUrl),
    '/uploads/chat/a.jpg'
  )
  assert.equal(
    resolveUploadStorageUrlWithBase('http://localhost:8080/uploads/chat/a.jpg', baseUrl),
    '/uploads/chat/a.jpg'
  )
  assert.equal(
    resolveUploadStorageUrlWithBase('http://192.168.1.20:8080/uploads/chat/a.jpg', baseUrl),
    '/uploads/chat/a.jpg'
  )
  assert.equal(
    resolveUploadStorageUrlWithBase('/uploads/chat/a.jpg', baseUrl),
    '/uploads/chat/a.jpg'
  )
})

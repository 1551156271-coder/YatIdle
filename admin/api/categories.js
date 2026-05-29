import { get, post, put, del } from './request'

export const listCategories = () => get('/api/admin/categories')
export const createCategory = data => post('/api/admin/categories', data)
export const updateCategory = (id, data) => put(`/api/admin/categories/${id}`, data)
export const deleteCategory = id => del(`/api/admin/categories/${id}`)

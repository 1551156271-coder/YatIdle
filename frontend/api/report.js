import { post } from './index'

export function createReport(data) {
	return post('/api/reports', data)
}

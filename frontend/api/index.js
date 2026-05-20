const BASE_URL = 'http://localhost:8080'
const TIMEOUT = 10000

// ========== Mock 开关：true=用假数据，false=调真实接口 ==========
export const MOCK = true
// ================================================================

// Mock 模式下自动写入登录态
if (MOCK && !uni.getStorageSync('user')) {
	uni.setStorageSync('user', { id: 1, username: 'testuser', avatar: '', campus: '东校园' })
}

// ========== Mock 数据 ==========
const mockData = {}

// 用户
mockData.user = {
	id: 1, username: 'testuser', phone: '13800138000',
	avatar: '', role: 0, status: 'active', verified: true,
	createTime: '2026-05-14T11:30:00'
}

// 分类
mockData.categories = [
	{ id: 1, name: '数码电子', sortOrder: 1 },
	{ id: 2, name: '书籍教材', sortOrder: 2 },
	{ id: 3, name: '生活用品', sortOrder: 3 },
	{ id: 4, name: '服饰鞋包', sortOrder: 4 },
	{ id: 5, name: '运动户外', sortOrder: 5 },
	{ id: 6, name: '其他', sortOrder: 6 }
]

// 商品
mockData.items = [
	{ id: 1, userId: 1, title: 'iPad Pro 2021 128G 几乎全新', campus: '东校园', conditionLevel: '99新', description: '去年买的，没用几次，电池健康98%，带原装充电器和保护壳。', price: 4200, categoryId: 1, status: 'ON_SALE', viewCount: 256, favoriteCount: 12, imageUrl: '', imageUrls: [], createTime: '2026-05-15T10:00:00' },
	{ id: 2, userId: 2, title: '高等数学第七版 同济大学', campus: '南校园', conditionLevel: '9成新', description: '上学期用完，只有少量笔记。', price: 15, categoryId: 2, status: 'ON_SALE', viewCount: 89, favoriteCount: 3, imageUrl: '', imageUrls: [], createTime: '2026-05-16T14:00:00' },
	{ id: 3, userId: 1, title: '宿舍用小台灯 LED 护眼', campus: '东校园', conditionLevel: '95新', description: '可调亮度，暖光白光切换。', price: 35, categoryId: 3, status: 'REMOVED', viewCount: 134, favoriteCount: 5, imageUrl: '', imageUrls: [], createTime: '2026-05-17T09:00:00' },
	{ id: 4, userId: 2, title: 'Nike Air Force 1 白色 42码', campus: '北校园', conditionLevel: '8成新', description: '穿了一个学期，鞋底有正常磨损。', price: 200, categoryId: 4, status: 'ON_SALE', viewCount: 312, favoriteCount: 18, imageUrl: '', imageUrls: [], createTime: '2026-05-18T16:00:00' },
	{ id: 5, userId: 1, title: '羽毛球拍 Yonex 单拍', campus: '南校园', conditionLevel: '9成新', description: '买多了出一支，已拉线。', price: 150, categoryId: 5, status: 'ON_SALE', viewCount: 78, favoriteCount: 2, imageUrl: '', imageUrls: [], createTime: '2026-05-19T08:00:00' },
	{ id: 6, userId: 1, title: '闲置键盘 Cherry MX 红轴', campus: '东校园', conditionLevel: '9成新', description: '用了两个月，手感很好，换新键盘了所以出。', price: 280, categoryId: 1, status: 'ON_SALE', viewCount: 45, favoriteCount: 1, imageUrl: '', imageUrls: [], createTime: '2026-05-19T12:00:00' }
]

// 订单
mockData.orders = {
	sell: [
		{ id: 101, orderNo: 'YI202605151030001', itemId: 1, buyerId: 3, sellerId: 1, price: 4200, status: 'PENDING', tradeLocation: '东校园图书馆门口', remark: '', createTime: '2026-05-15T10:30:00' },
		{ id: 102, orderNo: 'YI202605161400002', itemId: 2, buyerId: 4, sellerId: 1, price: 15, status: 'COMPLETED', tradeLocation: '南校园食堂', remark: '当面交易', createTime: '2026-05-16T14:00:00', completeTime: '2026-05-16T15:00:00' },
		{ id: 103, orderNo: 'YI202605170900003', itemId: 3, buyerId: 2, sellerId: 1, price: 35, status: 'CANCELLED', tradeLocation: '东校园', remark: '', createTime: '2026-05-17T09:00:00', cancelTime: '2026-05-17T10:00:00', cancelReason: '买家改变主意' }
	],
	buy: [
		{ id: 201, orderNo: 'YI202605181600004', itemId: 4, buyerId: 1, sellerId: 4, price: 200, status: 'PENDING', tradeLocation: '北校园快递站', remark: '', createTime: '2026-05-18T16:00:00' },
		{ id: 202, orderNo: 'YI202605190800005', itemId: 5, buyerId: 1, sellerId: 3, price: 150, status: 'COMPLETED', tradeLocation: '南校园体育馆', remark: '晚上来拿', createTime: '2026-05-19T08:00:00', completeTime: '2026-05-19T09:00:00' }
	]
}

// 收藏
mockData.favorites = [
	{ id: 1, itemId: 1, itemTitle: 'iPad Pro 2021 128G 几乎全新', price: 4200, itemStatus: 'ON_SALE', favoriteTime: '2026-05-16T12:00:00' },
	{ id: 2, itemId: 4, itemTitle: 'Nike Air Force 1 白色 42码', price: 200, itemStatus: 'ON_SALE', favoriteTime: '2026-05-18T20:00:00' }
]

// 聊天会话
mockData.chatSessions = [
	{ id: 1, itemId: 4, itemTitle: 'Nike Air Force 1 白色 42码', buyerId: 1, sellerId: 4, lastMessage: '鞋子还在吗？多大码的？', lastSenderId: 1, lastMessageTime: '2026-05-18T20:15:00', unreadCount: 0 },
	{ id: 2, itemId: 5, itemTitle: '羽毛球拍 Yonex 单拍', buyerId: 1, sellerId: 3, lastMessage: '好的，晚上体育馆见', lastSenderId: 3, lastMessageTime: '2026-05-19T07:30:00', unreadCount: 1 },
	{ id: 3, itemId: 1, itemTitle: 'iPad Pro 2021 128G 几乎全新', buyerId: 5, sellerId: 1, lastMessage: '还能便宜点吗？', lastSenderId: 5, lastMessageTime: '2026-05-19T10:00:00', unreadCount: 2 },
	{ id: 4, itemId: 2, itemTitle: '高等数学第七版 同济大学', buyerId: 1, sellerId: 2, lastMessage: '书还有吗？明天方便面交吗', lastSenderId: 1, lastMessageTime: '2026-05-17T19:00:00', unreadCount: 0 },
	{ id: 5, itemId: 6, itemTitle: '闲置键盘 Cherry MX 红轴', buyerId: 3, sellerId: 1, lastMessage: '键盘什么轴', lastSenderId: 3, lastMessageTime: '2026-05-20T09:00:00', unreadCount: 1 },
	{ id: 6, itemId: 3, itemTitle: '宿舍用小台灯 LED 护眼', buyerId: 4, sellerId: 1, lastMessage: '台灯还在吗？', lastSenderId: 4, lastMessageTime: '2026-05-18T10:00:00', unreadCount: 0 },
	{ id: 7, itemId: 4, itemTitle: 'Nike Air Force 1 白色 42码', buyerId: 6, sellerId: 2, lastMessage: '能小刀吗', lastSenderId: 6, lastMessageTime: '2026-05-19T15:00:00', unreadCount: 1 }
]

// 评价（收到的评价，userId=1 即当前用户）
mockData.reviews = [
	{ id: 1, reviewerName: '李四', reviewerAvatar: '', rating: 5, content: '卖家很爽快，车况和描述完全一致，还送了挡泥板！', createTime: '2026-05-17T10:00:00' },
	{ id: 2, reviewerName: '王五', reviewerAvatar: '', rating: 5, content: '交易顺利，当面验货很放心', createTime: '2026-05-14T14:00:00' },
	{ id: 3, reviewerName: '赵六', reviewerAvatar: '', rating: 4, content: '台灯好用，就是有点小划痕，整体不错', createTime: '2026-05-10T09:00:00' },
	{ id: 4, reviewerName: '孙七', reviewerAvatar: '', rating: 5, content: '非常好的卖家，书保护得很好', createTime: '2026-05-06T16:00:00' },
	{ id: 5, reviewerName: '周八', reviewerAvatar: '', rating: 4, content: '交易顺利', createTime: '2026-04-28T11:00:00' },
	{ id: 6, reviewerName: '吴九', reviewerAvatar: '', rating: 5, content: '很满意的一次交易，卖家回复很快', createTime: '2026-04-20T15:00:00' },
	{ id: 7, reviewerName: '郑十', reviewerAvatar: '', rating: 3, content: '东西还行，但比描述稍微旧了一点', createTime: '2026-04-12T08:00:00' },
	{ id: 8, reviewerName: '钱二', reviewerAvatar: '', rating: 5, content: '第二次交易了，一如既往靠谱', createTime: '2026-04-05T10:00:00' }
]

// 用户信用统计（userId=1 即当前用户）
mockData.userCredit = {
	creditScore: 92,
	dealCount: 23,
	goodsCount: 3,
	reviewCount: 8
}

// 钱包
mockData.wallet = {
	balance: 386.50,
	frozenAmount: 0,
	totalIncome: 1280.00,
	totalExpense: 893.50
}
mockData.walletTransactions = [
	{ id: 1, type: 'INCOME', title: '出售 iPad Pro 2021', amount: 4200, balanceAfter: 4200, createTime: '2026-05-15T10:40:00' },
	{ id: 2, type: 'EXPENSE', title: '购买 高等数学第七版', amount: -15, balanceAfter: 4185, createTime: '2026-05-16T14:10:00' },
	{ id: 3, type: 'INCOME', title: '出售 小台灯', amount: 35, balanceAfter: 4220, createTime: '2026-05-17T09:20:00' },
	{ id: 4, type: 'EXPENSE', title: '购买 Nike Air Force 1', amount: -200, balanceAfter: 4020, createTime: '2026-05-18T16:30:00' },
	{ id: 5, type: 'INCOME', title: '出售 羽毛球拍', amount: 150, balanceAfter: 4170, createTime: '2026-05-19T08:15:00' },
	{ id: 6, type: 'EXPENSE', title: '提现', amount: -3800, balanceAfter: 370, createTime: '2026-05-19T12:00:00' },
	{ id: 7, type: 'INCOME', title: '退款 买家取消订单', amount: 35, balanceAfter: 405, createTime: '2026-05-20T09:00:00' },
	{ id: 8, type: 'EXPENSE', title: '购买 键盘 Cherry MX 红轴', amount: -18.50, balanceAfter: 386.50, createTime: '2026-05-20T14:00:00' }
]

// 聊天消息
mockData.chatMessages = {}
mockData.chatMessages[1] = [
	{ id: 1, sessionId: 1, senderId: 1, receiverId: 4, messageType: 'TEXT', content: '你好，鞋子还在吗？', readFlag: 1, createTime: '2026-05-18T20:10:00' },
	{ id: 2, sessionId: 1, senderId: 4, receiverId: 1, messageType: 'TEXT', content: '在的，42码，成色如图', readFlag: 1, createTime: '2026-05-18T20:12:00' },
	{ id: 3, sessionId: 1, senderId: 1, receiverId: 4, messageType: 'TEXT', content: '鞋子还在吗？多大码的？', readFlag: 1, createTime: '2026-05-18T20:15:00' },
	{ id: 4, sessionId: 1, senderId: 4, receiverId: 1, messageType: 'VOICE', content: '', duration: 12, readFlag: 0, createTime: '2026-05-18T20:16:00' }
]
mockData.chatMessages[2] = [
	{ id: 5, sessionId: 2, senderId: 1, receiverId: 3, messageType: 'TEXT', content: '请问羽毛球拍还在吗？', readFlag: 1, createTime: '2026-05-19T07:00:00' },
	{ id: 6, sessionId: 2, senderId: 3, receiverId: 1, messageType: 'TEXT', content: '还在', readFlag: 1, createTime: '2026-05-19T07:25:00' },
	{ id: 7, sessionId: 2, senderId: 1, receiverId: 3, messageType: 'TEXT', content: '什么时候方便拿？', readFlag: 1, createTime: '2026-05-19T07:28:00' },
	{ id: 8, sessionId: 2, senderId: 3, receiverId: 1, messageType: 'TEXT', content: '好的，晚上体育馆见', readFlag: 0, createTime: '2026-05-19T07:30:00' }
]
mockData.chatMessages[3] = [
	{ id: 9, sessionId: 3, senderId: 5, receiverId: 1, messageType: 'TEXT', content: 'iPad还在吗？', readFlag: 1, createTime: '2026-05-19T09:30:00' },
	{ id: 10, sessionId: 3, senderId: 1, receiverId: 5, messageType: 'TEXT', content: '在的', readFlag: 1, createTime: '2026-05-19T09:35:00' },
	{ id: 11, sessionId: 3, senderId: 5, receiverId: 1, messageType: 'TEXT', content: '还能便宜点吗？', readFlag: 0, createTime: '2026-05-19T10:00:00' }
]
mockData.chatMessages[4] = [
	{ id: 12, sessionId: 4, senderId: 1, receiverId: 2, messageType: 'TEXT', content: '学姐你好，书还有吗？明天方便面交吗', readFlag: 1, createTime: '2026-05-17T19:00:00' }
]
mockData.chatMessages[5] = [
	{ id: 13, sessionId: 5, senderId: 3, receiverId: 1, messageType: 'TEXT', content: '键盘什么轴', readFlag: 0, createTime: '2026-05-20T09:00:00' }
]
mockData.chatMessages[6] = [
	{ id: 14, sessionId: 6, senderId: 4, receiverId: 1, messageType: 'TEXT', content: '台灯还在吗？', readFlag: 1, createTime: '2026-05-18T10:00:00' }
]
mockData.chatMessages[7] = [
	{ id: 15, sessionId: 7, senderId: 6, receiverId: 2, messageType: 'TEXT', content: '能小刀吗', readFlag: 0, createTime: '2026-05-19T15:00:00' }
]

// 模拟网络延迟
function fakeDelay() {
	return new Promise(resolve => setTimeout(resolve, 200 + Math.random() * 300))
}

// 分页辅助函数
function paginate(arr, pageNum, pageSize) {
	const total = arr.length
	const pn = parseInt(pageNum) || 1
	const ps = parseInt(pageSize) || 10
	const start = (pn - 1) * ps
	return {
		total,
		pageNum: pn,
		pageSize: ps,
		records: arr.slice(start, start + ps)
	}
}

// 根据 URL 匹配 mock 数据
function getMockResponse(method, url, data) {
	// 用户
	if (url === '/api/user/login' || url === '/api/user/register') {
		return { ...mockData.user }
	}
	if (url.match(/^\/api\/user\/\d+$/)) {
		return { ...mockData.user }
	}
	if (url === '/api/user/profile' && method === 'PUT') {
		return null
	}

	// 分类
	if (url === '/api/categories') {
		return [...mockData.categories]
	}

	// 商品搜索
	if (url === '/api/items/search') {
		return { records: [...mockData.items], total: mockData.items.length, size: 20, current: 1, pages: 1 }
	}
	// 商品详情
	const itemMatch = url.match(/^\/api\/items\/(\d+)$/)
	if (itemMatch && method === 'GET') {
		const item = mockData.items.find(i => i.id === parseInt(itemMatch[1]))
		return item ? { ...item } : null
	}
	// 发布商品
	if (url === '/api/items/publish' && method === 'POST') {
		const newItem = {
			id: mockData.items.length + 1,
			userId: data ? data.userId : 1,
			title: data ? data.title : '',
			price: data ? data.price : 0,
			categoryId: data ? data.categoryId : 6,
			status: 'ON_SALE',
			viewCount: 0, favoriteCount: 0,
			imageUrls: data && data.imageUrls ? data.imageUrls : [],
			createTime: new Date().toISOString()
		}
		return newItem
	}
	// 用户商品列表
	const userItemsMatch = url.match(/^\/api\/items\/user\/(\d+)$/)
	if (userItemsMatch) {
		const userId = parseInt(userItemsMatch[1])
		const status = data && data.status
		let items = mockData.items.filter(i => i.userId === userId)
		if (status) items = items.filter(i => i.status === status)
		return { records: items, total: items.length, size: 20, current: 1, pages: 1 }
	}
	// 编辑商品
	const itemEditMatch = url.match(/^\/api\/items\/(\d+)$/)
	if (itemEditMatch && method === 'PUT') {
		return null
	}
	// 下架商品
	const offlineMatch = url.match(/^\/api\/items\/(\d+)\/offline/)
	if (offlineMatch) {
		const itemId = parseInt(offlineMatch[1])
		const item = mockData.items.find(i => i.id === itemId)
		if (item) item.status = 'REMOVED'
		return null
	}
	// 重新上架
	const onlineMatch = url.match(/^\/api\/items\/(\d+)\/online/)
	if (onlineMatch) {
		const itemId = parseInt(onlineMatch[1])
		const item = mockData.items.find(i => i.id === itemId)
		if (item) item.status = 'ON_SALE'
		return null
	}

	// 订单
	if (url === '/api/orders/my-buy') {
		return paginate(mockData.orders.buy, data.pageNum, data.pageSize)
	}
	if (url === '/api/orders/my-sell') {
		return paginate(mockData.orders.sell, data.pageNum, data.pageSize)
	}
	if (url === '/api/orders' && method === 'POST') {
		return {
			id: 301,
			orderNo: 'YI' + Date.now(),
			itemId: data ? data.itemId : 1,
			buyerId: 1,
			sellerId: 2,
			price: 100,
			status: 'PENDING',
			tradeLocation: data ? data.tradeLocation : '',
			remark: data ? data.remark : '',
			createTime: new Date().toISOString()
		}
	}
	const orderCancelMatch = url.match(/^\/api\/orders\/(\d+)\/cancel$/)
	if (orderCancelMatch) {
		return { id: parseInt(orderCancelMatch[1]), status: 'CANCELLED', cancelTime: new Date().toISOString() }
	}
	const orderCompleteMatch = url.match(/^\/api\/orders\/(\d+)\/complete$/)
	if (orderCompleteMatch) {
		return { id: parseInt(orderCompleteMatch[1]), status: 'COMPLETED', completeTime: new Date().toISOString() }
	}

	// 收藏
	if (url === '/api/favorites' && method === 'GET') {
		return paginate(mockData.favorites, data.pageNum, data.pageSize)
	}
	const favAddMatch = url.match(/^\/api\/favorites\/(\d+)$/)
	if (favAddMatch && method === 'POST') {
		const itemId = parseInt(favAddMatch[1])
		const item = mockData.items.find(i => i.id === itemId)
		if (!item) return null
		const exists = mockData.favorites.find(f => f.itemId === itemId)
		if (!exists) {
			const newFav = {
				id: mockData.favorites.length + 1,
				itemId: item.id,
				itemTitle: item.title,
				price: item.price,
				itemStatus: item.status,
				favoriteTime: new Date().toISOString()
			}
			mockData.favorites.unshift(newFav)
		}
		return null
	}
	if (favAddMatch && method === 'DELETE') {
		const itemId = parseInt(favAddMatch[1])
		const idx = mockData.favorites.findIndex(f => f.itemId === itemId)
		if (idx >= 0) mockData.favorites.splice(idx, 1)
		return null
	}

	// 聊天会话
	if (url === '/api/chat/sessions' && method === 'GET') {
		return paginate(mockData.chatSessions, data.pageNum, data.pageSize)
	}
	if (url === '/api/chat/sessions' && method === 'POST') {
		const itemId = data ? data.itemId : null
		const item = mockData.items.find(i => i.id === itemId)
		if (!item) return null
		const existing = mockData.chatSessions.find(s => s.itemId === itemId && s.buyerId === 1)
		if (existing) return { ...existing }
		const newSess = {
			id: mockData.chatSessions.length + 1,
			itemId: item.id,
			itemTitle: item.title,
			buyerId: 1,
			sellerId: item.userId,
			lastMessage: null,
			lastSenderId: null,
			lastMessageTime: null,
			unreadCount: 0
		}
		mockData.chatSessions.push(newSess)
		mockData.chatMessages[newSess.id] = []
		return newSess
	}
	// 聊天消息
	const msgMatch = url.match(/^\/api\/chat\/sessions\/(\d+)\/messages$/)
	if (msgMatch) {
		const msgs = mockData.chatMessages[parseInt(msgMatch[1])] || []
		return method === 'GET' ? paginate(msgs, data.pageNum, data.pageSize || 20) : null
	}
	// 消息已读
	const readMatch = url.match(/^\/api\/chat\/sessions\/(\d+)\/read$/)
	if (readMatch) {
		const sid = parseInt(readMatch[1])
		const sess = mockData.chatSessions.find(s => s.id === sid)
		if (sess) sess.unreadCount = 0
		return null
	}
	// 发送消息
	if (url === '/api/chat/messages' && method === 'POST') {
		const sid = data ? data.sessionId : 1
		const newMsg = {
			id: Date.now(),
			sessionId: sid,
			senderId: 1,
			receiverId: 2,
			messageType: 'TEXT',
			content: data ? data.content : '',
			readFlag: 0,
			createTime: new Date().toISOString()
		}
		if (!mockData.chatMessages[sid]) mockData.chatMessages[sid] = []
		mockData.chatMessages[sid].push(newMsg)
		const sess = mockData.chatSessions.find(s => s.id === sid)
		if (sess) {
			sess.lastMessage = newMsg.content
			sess.lastSenderId = newMsg.senderId
			sess.lastMessageTime = newMsg.createTime
		}
		return newMsg
	}

	// 评价
	if (url.match(/^\/api\/reviews/)) {
		return [...mockData.reviews]
	}
	if (url.match(/^\/api\/user-credit/)) {
		return { ...mockData.userCredit }
	}
	// 钱包
	if (url.match(/^\/api\/wallet\/transactions/)) {
		return [...mockData.walletTransactions]
	}
	if (url.match(/^\/api\/wallet/)) {
		return { ...mockData.wallet }
	}

	// 其他更新类接口默认返回 null
	return null
}

function request(method, url, data) {
	// Mock 模式
	if (MOCK) {
		return fakeDelay().then(() => {
			const result = getMockResponse(method, url, data)
			if (result === undefined || result === null) {
				return result
			}
			return result
		})
	}

	// 真实请求
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

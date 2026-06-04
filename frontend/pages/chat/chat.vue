<template>
	<view class="chat-page">

		<!-- 消息区域 -->
		<scroll-view
			class="msg-scroll"
			scroll-y
			:scroll-into-view="scrollToId"
			:scroll-with-animation="scrollAnimated"
		>
			<view v-if="msgs.length === 0" class="empty-chat">
				<text class="empty-text">开始聊天吧~</text>
			</view>

			<block v-for="msg in msgs" :key="msg.id">
				<view :id="'msg-' + msg.id">
					<view v-if="msg.showTime" class="time-divider">
						<text class="time-text">{{ msg.timeLabel }}</text>
					</view>

					<view v-if="msg.type === 'system'" class="system-msg">
						<text class="system-text">{{ msg.content }}</text>
					</view>

					<view v-else class="msg-row" :class="{ 'msg-self': msg.fromMe }">
						<view v-if="!msg.fromMe" class="msg-avatar" @click="goPartnerProfile">
							<image v-if="contactInfo.avatar" class="msg-avatar-img" :src="contactInfo.avatar" mode="aspectFill"></image>
							<text v-else class="msg-avatar-emoji">{{ contactInfo.defaultAvatar }}</text>
						</view>

						<view class="msg-bubble-wrap">
							<!-- 文本 -->
							<view v-if="msg.type === 'text'" class="bubble bubble-text" :class="{ 'bubble-self': msg.fromMe }">
								<text class="msg-text">{{ msg.content }}</text>
							</view>

							<!-- 图片 -->
							<view v-else-if="msg.type === 'image'" class="bubble bubble-media" @click="previewImage(msg)">
								<image class="msg-image" :src="msg.content" mode="aspectFill" :style="msg.imgStyle"></image>
							</view>

							<!-- 商品卡片 -->
							<view v-else-if="msg.type === 'product'" class="bubble bubble-card" @click="openProductDetail(msg)">
								<image v-if="msg.product.image" class="card-img" :src="msg.product.image" mode="aspectFill"></image>
								<view class="card-info">
									<text class="card-title">{{ msg.product.title }}</text>
									<text class="card-price">¥{{ msg.product.price }}</text>
								</view>
							</view>
						</view>

						<view v-if="msg.fromMe" class="msg-avatar msg-avatar-self" @click="goMyProfile">
							<image v-if="myAvatar" class="msg-avatar-img" :src="myAvatar" mode="aspectFill"></image>
							<text v-else class="msg-avatar-emoji">{{ myDefaultAvatar }}</text>
						</view>
					</view>
				</view>
			</block>

			<view class="scroll-anchor" id="msg-bottom"></view>
		</scroll-view>

		<!-- 商品选择弹窗 -->
		<view v-if="pickerVisible" class="picker-overlay" @click="closeProductPicker">
			<view class="picker-panel" @click.stop>
				<view class="picker-header">
					<text class="picker-title">选择要发送的商品</text>
					<text class="picker-close" @click="closeProductPicker">✕</text>
				</view>
				<scroll-view class="picker-body" scroll-y>
					<view v-if="pickerLoading" class="picker-loading">加载中...</view>
					<view v-else-if="pickerItems.length === 0" class="picker-empty">暂无在售商品</view>
					<view v-for="item in pickerItems" :key="item.id" class="picker-card" @click="sendProduct(item)">
						<image v-if="item.imageUrl" class="picker-img" :src="item.imageUrl" mode="aspectFill"></image>
						<view class="picker-placeholder-img" v-else></view>
						<view class="picker-info">
							<text class="picker-name">{{ item.title }}</text>
							<text class="picker-price">¥{{ item.price }}</text>
						</view>
					</view>
				</scroll-view>
			</view>
		</view>

		<!-- 底部输入区 -->
		<view class="input-area">
			<view class="input-row">
				<view class="input-box">
					<input
						class="text-input"
						v-model="inputText"
						placeholder="说点什么..."
						confirm-type="send"
						@confirm="sendText"
					/>
				</view>

				<view class="plus-btn" :class="{ 'plus-active': toolbarOpen }" @click="toggleToolbar">
					<text class="plus-icon iconfont icon-fabu"></text>
				</view>
			</view>

			<!-- 扩展面板 -->
			<view v-if="toolbarOpen" class="toolbar-panel">
				<view class="tb-item" @click="pickImage">
					<view class="tb-icon" style="background:#e3f2fd;"><text class="tb-emoji">🖼</text></view>
					<text class="tb-label">相册</text>
				</view>
				<view class="tb-item" @click="takePhoto">
					<view class="tb-icon" style="background:#e8f5e9;"><text class="tb-emoji">📷</text></view>
					<text class="tb-label">拍摄</text>
				</view>
				<view class="tb-item" @click="openProductPicker">
					<view class="tb-icon" style="background:#fce4ec;"><text class="tb-emoji">📦</text></view>
					<text class="tb-label">商品</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getMessages, sendMessage, markRead, uploadChatImage } from '@/api/chat.js'
import { resolveImageUrl } from '@/api/index.js'
import { getUserItems } from '@/api/item.js'

function parseMessage(m, currentUserId) {
	const isImage = m.messageType === 'IMAGE'
	if (isImage) {
		return {
			id: m.id,
			fromMe: m.senderId === currentUserId,
			type: 'image',
			content: resolveImageUrl(m.content),
			time: new Date(m.createTime).getTime(),
			showTime: false
		}
	}
	// try parse product JSON
	try {
		const obj = JSON.parse(m.content)
		if (obj && obj.type === 'product') {
			return {
				id: m.id,
				fromMe: m.senderId === currentUserId,
				type: 'product',
				content: m.content,
				product: obj,
				time: new Date(m.createTime).getTime(),
				showTime: false
			}
		}
	} catch (e) {}
	return {
		id: m.id,
		fromMe: m.senderId === currentUserId,
		type: 'text',
		content: m.content,
		time: new Date(m.createTime).getTime(),
		showTime: false
	}
}

export default {
	data() {
		return {
			scrollAnimated: false,
			contactInfo: { id: null, avatar: '', defaultAvatar: '?', name: '聊天' },
				myAvatar: '',
				myDefaultAvatar: '?',
			msgs: [],
			inputText: '',
			toolbarOpen: false,
			scrollToId: '',
			sessionId: null,
			userId: null,
			partnerId: 0,
			pollTimer: null,
			// 商品选择器
			pickerVisible: false,
			pickerLoading: false,
			pickerItems: []
		}
	},
	onLoad(options) {

		const user = uni.getStorageSync('user')
		if (user && user.id) {
			this.userId = user.id
		}
		if (user) {
			this.myAvatar = user.avatar || ''
			this.myDefaultAvatar = (user.username || '?').charAt(0)
		}
		if (options.name) {
			this.contactInfo.name = decodeURIComponent(options.name)
		}
			if (options.avatar) {
				this.contactInfo.avatar = decodeURIComponent(options.avatar)
			}
			if (options.partnerId) {
				this.partnerId = parseInt(options.partnerId) || 0
			}
			this.contactInfo.defaultAvatar = this.contactInfo.name.charAt(0)
			uni.setNavigationBarTitle({ title: this.contactInfo.name })
		if (options.id) {
			this.sessionId = options.id
			this.loadMessages()
			this.startPolling()
		}
		this.$nextTick(() => this.scrollToBottom())
		setTimeout(() => { this.scrollAnimated = true }, 500)
	},
	onUnload() {
		this.stopPolling()
	},
	methods: {

		async loadMessages() {
			if (!this.sessionId || !this.userId) return
			try {
				await markRead(this.sessionId, this.userId).catch(() => {})
				const result = await getMessages(this.sessionId, this.userId); const list = (result && result.records) || result || []
				this.msgs = list.map(m => parseMessage(m, this.userId))
				this.addTimeDividers()
				this.$nextTick(() => this.scrollToBottom())
			} catch (e) {
				this.msgs = []
			}
		},

		addTimeDividers() {
			for (let i = 0; i < this.msgs.length; i++) {
				const msg = this.msgs[i]
				const prev = this.msgs[i - 1]
				if (!prev || (msg.time - prev.time > 5 * 60 * 1000)) {
					msg.showTime = true
					msg.timeLabel = this.formatTime(msg.time)
				}
			}
		},

		startPolling() {
			this.stopPolling()
			this.pollTimer = setInterval(() => {
				this.pollMessages()
			}, 3000)
		},

		stopPolling() {
			if (this.pollTimer) {
				clearInterval(this.pollTimer)
				this.pollTimer = null
			}
		},

		async pollMessages() {
			if (!this.sessionId || !this.userId) return
			try {
				const result = await getMessages(this.sessionId, this.userId)
				const list = (result && result.records) || result || []
				if (!list.length) return

				const isNew = list.length !== this.msgs.length ||
					(list.length > 0 && this.msgs.length > 0 && list[list.length - 1].id !== this.msgs[this.msgs.length - 1].id)

				if (!isNew) return

				const wasAtBottom = this.isScrolledToBottom()

				this.msgs = list.map(m => parseMessage(m, this.userId))
				this.addTimeDividers()

				markRead(this.sessionId, this.userId).catch(() => {})

				if (wasAtBottom) {
					this.$nextTick(() => this.scrollToBottom())
				}
			} catch (e) {
				// 轮询静默失败
			}
		},

		isScrolledToBottom() {
			return true
		},

		async sendText() {
			const t = this.inputText.trim()
			if (!t) return
			if (!this.userId || !this.sessionId) {
				uni.showToast({ title: '请先登录', icon: 'none' })
				return
			}
			this.inputText = ''
			this.toolbarOpen = false
			try {
				await sendMessage(this.userId, { sessionId: Number(this.sessionId), content: t })
				await this.loadMessages()
			} catch (e) {
				uni.showToast({ title: '发送失败', icon: 'none' })
			}
		},

		pickImage() {
			uni.chooseImage({ count: 9, sizeType: ['compressed'], sourceType: ['album'], success: (res) => {
				this.toolbarOpen = false
				res.tempFilePaths.forEach(filePath => this.sendImage(filePath))
			}})
		},
		takePhoto() {
			uni.chooseImage({ count: 1, sourceType: ['camera'], success: (res) => {
				this.toolbarOpen = false
				res.tempFilePaths.forEach(filePath => this.sendImage(filePath))
			}})
		},
		async sendImage(filePath) {
			if (!this.userId || !this.sessionId) {
				uni.showToast({ title: '请先登录', icon: 'none' })
				return
			}
			try {
				const url = await uploadChatImage(filePath)
				await sendMessage(this.userId, {
					sessionId: Number(this.sessionId),
					messageType: 'IMAGE',
					content: url
				})
				await this.loadMessages()
			} catch (e) {
				// 错误提示已在 api 层处理
			}
		},

		async openProductPicker() {
			this.toolbarOpen = false
			if (!this.userId) {
				uni.showToast({ title: '请先登录', icon: 'none' })
				return
			}
			this.pickerVisible = true
			this.pickerLoading = true
			this.pickerItems = []
			try {
				const result = await getUserItems(this.partnerId || this.userId, { status: 'ON_SALE' })
				const list = (result && result.records) || result || []
				this.pickerItems = list
			} catch (e) {
				this.pickerItems = []
			} finally {
				this.pickerLoading = false
			}
		},
		closeProductPicker() {
			this.pickerVisible = false
		},
		async sendProduct(item) {
			this.pickerVisible = false
			const payload = JSON.stringify({
				type: 'product',
				id: item.id,
				title: item.title,
				price: item.price,
				image: item.imageUrl || ''
			})
			try {
				await sendMessage(this.userId, {
					sessionId: Number(this.sessionId),
					content: payload
				})
				await this.loadMessages()
			} catch (e) {
				// 错误提示已在 api 层处理
			}
		},

		goPartnerProfile() {
				if (this.partnerId) {
					uni.navigateTo({ url: '/pages/profile/profile?id=' + this.partnerId })
				}
			},
			goMyProfile() {
				uni.navigateTo({ url: '/pages/my-edit/my-edit' })
			},
			openProductDetail(msg) {
			if (msg.product && msg.product.id) {
				uni.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + msg.product.id })
			}
		},

		previewImage(msg) {
			uni.previewImage({ current: msg.content, urls: this.msgs.filter(m => m.type === 'image').map(m => m.content) })
		},

		toggleToolbar() {
			this.toolbarOpen = !this.toolbarOpen
			if (this.toolbarOpen) this.$nextTick(() => this.scrollToBottom())
		},

		formatTime(ts) {
			const d = new Date(ts), now = new Date()
			const pad = n => String(n).padStart(2, '0')
			const hm = pad(d.getHours()) + ':' + pad(d.getMinutes())
			if (d.toDateString() === now.toDateString()) return '今天 ' + hm
			const y = new Date(now); y.setDate(now.getDate() - 1)
			if (d.toDateString() === y.toDateString()) return '昨天 ' + hm
			return (d.getMonth()+1) + '/' + d.getDate() + ' ' + hm
		},

		scrollToBottom() {
			this.scrollToId = ''
			this.$nextTick(() => { this.scrollToId = 'msg-bottom' })
		}
	}
}
</script>

<style>
/* ===== 页面整体 ===== */
.chat-page {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: #f0f0f0;
	box-sizing: border-box;
	overflow: hidden;
}


/* ===== 消息区 ===== */
.msg-scroll { flex: 1; padding: 20rpx 20rpx 0; overflow-y: auto; box-sizing: border-box; }
.scroll-anchor { height: 1rpx; }
.time-divider { text-align: center; padding: 24rpx 0 16rpx; }
.time-text { font-size: 22rpx; color: #bbb; background: #f0f0f0; padding: 4rpx 20rpx; border-radius: 4rpx; }
.system-msg { text-align: center; padding: 16rpx 0; }
.system-text { font-size: 22rpx; color: #ccc; }

/* ===== 消息行 ===== */
.msg-row { display: flex; align-items: flex-start; margin-bottom: 24rpx; box-sizing: border-box; }
.msg-self { justify-content: flex-end; }

.msg-avatar {
	width: 80rpx; height: 80rpx; background: #e8f5ee; border-radius: 50%;
	display: flex; align-items: center; justify-content: center; font-size: 36rpx; flex-shrink: 0; overflow: hidden;
}
.msg-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
.msg-avatar-emoji { font-size: 36rpx; }
.msg-row:not(.msg-self) .msg-avatar { margin-right: 16rpx; }
.msg-self .msg-avatar { margin-left: 16rpx; background: #e3f2fd; }

.msg-bubble-wrap { max-width: 72%; box-sizing: border-box; }

/* ===== 气泡 ===== */
.bubble { padding: 24rpx 28rpx; border-radius: 20rpx; word-break: break-all; position: relative; box-sizing: border-box; }
.msg-row:not(.msg-self) .bubble { background: #ffffff; border-top-left-radius: 6rpx; }
.bubble-self { background: #b3e5c8; border-top-right-radius: 6rpx; }
.msg-text { font-size: 32rpx; color: #333; line-height: 1.6; }

/* 图片 */
.bubble-media { padding: 0; overflow: hidden; position: relative; border-radius: 16rpx; line-height: 0; }
.msg-image { display: block; border-radius: 16rpx; max-width: 100%; }

/* 商品卡片 */
.bubble-card {
	padding: 0; border-radius: 16rpx; overflow: hidden; width: 420rpx;
	max-width: 100%; background: #ffffff; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05); box-sizing: border-box;
}
.card-img { width: 100%; height: 240rpx; display: block; background: #f0f0f0; }
.card-info { padding: 16rpx 20rpx; }
.card-title { font-size: 26rpx; color: #333; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.card-price { font-size: 30rpx; color: #e74c3c; font-weight: bold; margin-top: 4rpx; display: block; }

/* ===== 商品选择弹窗 ===== */
.picker-overlay {
	position: fixed; top: 0; left: 0; right: 0; bottom: 0;
	background: rgba(0,0,0,0.5); z-index: 200;
	display: flex; align-items: flex-end;
}
.picker-panel {
	width: 100%; max-height: 70vh; background: #fff;
	border-radius: 24rpx 24rpx 0 0; display: flex; flex-direction: column;
}
.picker-header {
	display: flex; align-items: center; justify-content: space-between;
	padding: 30rpx; border-bottom: 1rpx solid #f0f0f0;
}
.picker-title { font-size: 30rpx; font-weight: bold; color: #333; }
.picker-close { font-size: 36rpx; color: #999; padding: 10rpx; }
.picker-body { flex: 1; padding: 20rpx; }
.picker-loading, .picker-empty { text-align: center; padding: 80rpx 0; font-size: 26rpx; color: #999; }
.picker-card {
	display: flex; align-items: center; gap: 20rpx;
	padding: 20rpx; margin-bottom: 16rpx; background: #fafafa; border-radius: 12rpx;
}
.picker-img { width: 120rpx; height: 120rpx; border-radius: 8rpx; flex-shrink: 0; background: #eee; }
.picker-placeholder-img {
	width: 120rpx; height: 120rpx; border-radius: 8rpx; flex-shrink: 0;
	background: #eee;
}
.picker-info { flex: 1; overflow: hidden; }
.picker-name { font-size: 28rpx; color: #333; display: block; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.picker-price { font-size: 28rpx; color: #e74c3c; font-weight: bold; margin-top: 6rpx; display: block; }

/* ===== 空状态 ===== */
.empty-chat { display: flex; align-items: center; justify-content: center; padding-top: 300rpx; }
.empty-text { font-size: 28rpx; color: #ccc; }

/* ===== 底部输入区 ===== */
.input-area { background: #ffffff; border-top: 1rpx solid #eee; box-sizing: border-box; }
.input-row { display: flex; align-items: center; padding: 16rpx 16rpx; gap: 12rpx; box-sizing: border-box; }

.input-box { flex: 1; }
.text-input {
	width: 100%; height: 72rpx; background: #f5f5f5; border-radius: 8rpx;
	padding: 0 24rpx; font-size: 28rpx; box-sizing: border-box;
}

.plus-btn {
	width: 56rpx; height: 56rpx;
	display: flex; align-items: center; justify-content: center;
	flex-shrink: 0; transition: all 0.2s;
}
.plus-icon { font-size: 48rpx; color: #999; line-height: 1; }
.plus-active { background: #f5f5f5; border-color: #bbb; }

/* ===== 扩展面板 ===== */
.toolbar-panel {
	display: flex; flex-wrap: wrap; padding: 24rpx 20rpx 40rpx;
	border-top: 1rpx solid #f5f5f5; background: #fafafa; box-sizing: border-box;
}
.tb-item { width: 25%; display: flex; flex-direction: column; align-items: center; gap: 12rpx; margin-bottom: 30rpx; }
.tb-icon {
	width: 100rpx; height: 100rpx; border-radius: 24rpx;
	display: flex; align-items: center; justify-content: center;
}
.tb-emoji { font-size: 44rpx; }
.tb-label { font-size: 22rpx; color: #666; }
</style>

<template>
	<view class="messages-page">
		<view class="chat-list">
			<!-- 通知入口 -->
			<view class="chat-item notify-item" @click="goNotifications">
				<view class="chat-avatar notify-avatar">
					<text class="chat-avatar-emoji">🔔</text>
				</view>
				<view class="chat-content">
					<view class="chat-top">
						<text class="chat-name">通知</text>
					</view>
					<view class="chat-bottom">
						<text class="chat-msg">查看订单状态变更等系统通知</text>
						<view v-if="notifyUnread > 0" class="unread-badge">{{ notifyUnread > 99 ? '99+' : notifyUnread }}</view>
					</view>
				</view>
			</view>

			<view v-for="item in chatList" :key="item.id" class="chat-item" @click="openChat(item)">
				<view class="chat-avatar">
					<image v-if="item.avatar" class="chat-avatar-img" :src="item.avatar" mode="aspectFill"></image>
					<text v-else class="chat-avatar-emoji">{{ item.defaultAvatar }}</text>
				</view>
				<view class="chat-content">
					<view class="chat-top">
						<text class="chat-name">{{ item.name }}</text>
						<text class="chat-time">{{ item.time }}</text>
					</view>
					<view class="chat-bottom">
						<text class="chat-msg">{{ item.lastMsg }}</text>
						<view v-if="item.unread > 0" class="unread-badge">{{ item.unread > 99 ? '99+' : item.unread }}</view>
					</view>
				</view>
			</view>
		</view>

		<view v-if="chatList.length === 0 && !loading" class="empty-state">
			<text class="empty-icon iconfont icon-wuxiaoxi"></text>
			<text class="empty-text">暂无消息</text>
		</view>
	</view>
	<tab-bar />
</template>

<script>
	import TabBar from '@/components/tab-bar.vue'
	import { getMySessions } from '@/api/chat.js'
	import { getMySellOrders, getMyBuyOrders } from '@/api/order.js'
	export default {
		components: { TabBar },
		data() {
			return {
				loading: false,
				chatList: [],
				notifyUnread: 0,
				pollTimer: null
			}
		},
		onShow() {
			uni.hideTabBar()
			this.loadSessions()
			this.loadNotifyUnread()
			this.startPolling()
		},
		onHide() {
			this.stopPolling()
			uni.setStorageSync('msgUnread', 0)
		},
		methods: {
			startPolling() {
				this.stopPolling()
				this.pollTimer = setInterval(() => {
					this.loadSessions()
					this.loadNotifyUnread()
				}, 5000)
			},
			stopPolling() {
				if (this.pollTimer) {
					clearInterval(this.pollTimer)
					this.pollTimer = null
				}
			},
			async loadNotifyUnread() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					this.notifyUnread = 0
					return
				}
				try {
					let lastSeen = uni.getStorageSync('notifyLastSeen')
					if (!lastSeen || lastSeen.startsWith('1970')) {
						lastSeen = new Date().toISOString()
						uni.setStorageSync('notifyLastSeen', lastSeen)
					}
					const [sellResult, buyResult] = await Promise.all([
						getMySellOrders(user.id).catch(() => []),
						getMyBuyOrders(user.id).catch(() => [])
					])
					const allOrders = [...(sellResult || []), ...(buyResult || [])]
					const lastSeenMs = new Date(lastSeen).getTime()
					this.notifyUnread = allOrders.filter(o => {
						const t = o.completeTime || o.cancelTime || o.createTime || ''
						if (!t) return false
						return new Date(t).getTime() > lastSeenMs
					}).length
				} catch (e) {
					this.notifyUnread = 0
				}
			},
			async loadSessions() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					this.chatList = []
					uni.setStorageSync('msgUnread', 0)
					return
				}
				this.loading = true
				try {
					const result = await getMySessions(user.id); const list = (result && result.records) || result || []
					const totalUnread = list.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
					uni.setStorageSync('msgUnread', totalUnread)
					this.chatList = list.map(s => {
						const isBuyer = s.buyerId === user.id
						const otherName = isBuyer ? '卖家' : '买家'
						const partnerId = isBuyer ? s.sellerId : s.buyerId
						return {
							id: s.id,
							partnerId: partnerId || 0,
							avatar: s.partnerAvatar || '',
							defaultAvatar: (s.partnerName || otherName).charAt(0),
							name: s.partnerName || s.wantedTitle || s.itemTitle || otherName,
							lastMsg: s.lastMessage || '暂无消息',
							time: this.formatTime(s.lastMessageTime),
							unread: s.unreadCount || 0
						}
					})
				} catch (e) {
					this.chatList = []
				} finally {
					this.loading = false
				}
			},
			formatTime(ts) {
				if (!ts) return ''
				const d = new Date(ts), now = new Date()
				const pad = n => String(n).padStart(2, '0')
				const hm = pad(d.getHours()) + ':' + pad(d.getMinutes())
				if (d.toDateString() === now.toDateString()) return '今天 ' + hm
				const y = new Date(now); y.setDate(now.getDate() - 1)
				if (d.toDateString() === y.toDateString()) return '昨天 ' + hm
				return (d.getMonth() + 1) + '/' + d.getDate() + ' ' + hm
			},
			openChat(item) {
				uni.navigateTo({
					url: '/pages/chat/chat?id=' + item.id + '&partnerId=' + (item.partnerId || 0) + '&name=' + encodeURIComponent(item.name) + '&avatar=' + encodeURIComponent(item.avatar || '')
				})
			},
			goNotifications() {
				uni.navigateTo({
					url: '/pages/notifications/notifications'
				})
			}
		}
	}
</script>

<style>
	.messages-page { min-height: 100vh; width: 100%; background: #f5f5f5; overflow: hidden; box-sizing: border-box; }

	.chat-list { background: #ffffff; margin: 20rpx; border-radius: 16rpx; overflow: hidden; }

	.chat-item {
		display: flex; align-items: center;
		padding: 28rpx 24rpx; border-bottom: 1rpx solid #f5f5f5;
	}
	.chat-item:last-child { border-bottom: none; }

	/* 通知入口 */
	.notify-item { background: #f9faf9; }
	.notify-avatar { background: #e8f5ee; }

	.chat-avatar {
		width: 96rpx; height: 96rpx;
		background: #e8f5ee; border-radius: 50%;
		display: flex; align-items: center; justify-content: center;
		font-size: 44rpx; margin-right: 20rpx; flex-shrink: 0; overflow: hidden;
	}
	.chat-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
	.chat-avatar-emoji { font-size: 44rpx; }

	.chat-content { flex: 1; overflow: hidden; }
	.chat-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
	.chat-name { font-size: 30rpx; color: #333; font-weight: 500; }
	.chat-time { font-size: 22rpx; color: #ccc; }
	.chat-bottom { display: flex; justify-content: space-between; align-items: center; }
	.chat-msg { font-size: 26rpx; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }

	.unread-badge {
		min-width: 36rpx; height: 36rpx; line-height: 36rpx;
		background: #e74c3c; color: #ffffff; font-size: 20rpx;
		border-radius: 18rpx; padding: 0 8rpx; text-align: center;
	}

	.empty-state { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx; }
	.empty-icon { font-size: 100rpx; margin-bottom: 20rpx; color: #b6b6b6}
	.empty-text { font-size: 28rpx; color: #999; }
</style>

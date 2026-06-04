<template>
	<view class="my-tab-bar">
		<view
			v-for="(item, index) in list"
			:key="index"
			class="tb-item"
			@click="onTabClick(item, index)"
		>
			<view v-if="current === index" class="tb-indicator"></view>
			<view class="tb-icon-wrap">
				<text
					class="tb-icon iconfont"
					:class="current === index && item.selectedIcon ? item.selectedIcon : item.icon"
					:style="{ color: current === index ? selectedColor : color }"
				></text>
				<view v-if="item.badge > 0" class="tb-badge">{{ item.badge > 99 ? '99+' : item.badge }}</view>
			</view>
			<text
				class="tb-text"
				:class="{ 'tb-text-active': current === index }"
				:style="{ color: current === index ? selectedColor : color }"
			>{{ item.text }}</text>
		</view>
	</view>
</template>

<script>
	const BASE_URL = 'http://127.0.0.1:8080'

	import { getMySellOrders, getMyBuyOrders } from '@/api/order.js'

	export default {
		data() {
			return {
				color: '#676767',
				selectedColor: '#3A6341',
				current: 0,
				badgeTimer: null,
				list: [
					{ pagePath: '/pages/index/index', text: '首页', icon: 'icon-shouye1', selectedIcon: 'icon-shouye', badge: 0 },
					{ pagePath: '/pages/wanted/wanted', text: '求购', icon: 'icon-remenqiugou', badge: 0 },
					{ pagePath: '/pages/publish/publish', text: '发布', icon: 'icon-fabu-', selectedIcon: 'icon-fabudianjizhuangtai-', badge: 0 },
					{ pagePath: '/pages/messages/messages', text: '消息', icon: 'icon-xiaoxi', selectedIcon: 'icon-xiaoxi2', badge: 0 },
					{ pagePath: '/pages/my/my', text: '我的', icon: 'icon-wode-copy', selectedIcon: 'icon-wode', badge: 0 }
				]
			}
		},
		created() {
			const pages = getCurrentPages()
			const page = pages[pages.length - 1]
			if (page) {
				const idx = this.list.findIndex(t => t.pagePath === '/' + page.route)
				if (idx !== -1) this.current = idx
			}
			this.fetchUnread()
			this.badgeTimer = setInterval(() => this.fetchUnread(), 5000)
		},
		beforeUnmount() {
			if (this.badgeTimer) clearInterval(this.badgeTimer)
		},
		methods: {
			onTabClick(item, index) {
				if (this.current === index) return
				uni.switchTab({ url: item.pagePath })
			},
			async fetchUnread() {
				try {
					const user = uni.getStorageSync('user')
					if (!user || !user.id) return

					// 聊天未读
					let chatUnread = 0
					try {
						const res = await uni.request({
							url: BASE_URL + '/api/chat/sessions?userId=' + user.id + '&pageSize=50',
							method: 'GET',
							timeout: 5000
						})
						if (res.statusCode === 200 && res.data && res.data.code === 200) {
							const list = (res.data.data && res.data.data.records) || res.data.data || []
							chatUnread = list.reduce((sum, s) => sum + (s.unreadCount || 0), 0)
						}
					} catch (e) {}

					// 通知未读
					let notifyUnread = 0
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
						notifyUnread = allOrders.filter(o => {
							const t = o.completeTime || o.cancelTime || o.createTime || ''
							if (!t) return false
							return new Date(t).getTime() > lastSeenMs
						}).length
					} catch (e) {}

					const msgIdx = 3
					if (this.list[msgIdx]) {
						this.list[msgIdx].badge = chatUnread + notifyUnread
					}
				} catch (e) {}
			}
		}
	}
</script>

<style>
	.my-tab-bar {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		display: flex;
		align-items: center;
		justify-content: space-around;
		height: 100rpx;
		background: #ffffff;
		border-top: 1rpx solid #f0f0f0;
		padding-bottom: env(safe-area-inset-bottom);
		z-index: 999;
	}

	.tb-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		flex: 1;
		height: 100%;
		gap: 4rpx;
		position: relative;
	}

	.tb-indicator {
		position: absolute;
		top: 0;
		left: 50%;
		transform: translateX(-50%);
		width: 48rpx;
		height: 6rpx;
		background: #3A6341;
		border-radius: 0 0 6rpx 6rpx;
	}

	.tb-icon-wrap {
		position: relative;
	}

	.tb-icon {
		font-size: 50rpx;
		line-height: 1;
	}

	.tb-badge {
		position: absolute;
		top: -10rpx;
		right: -16rpx;
		min-width: 32rpx;
		height: 32rpx;
		line-height: 32rpx;
		text-align: center;
		background: #e74c3c;
		color: #ffffff;
		font-size: 20rpx;
		border-radius: 16rpx;
		padding: 0 8rpx;
	}

	.tb-text {
		font-size: 20rpx;
		line-height: 1;
	}

	.tb-text-active {
		font-weight: bold;
	}
</style>

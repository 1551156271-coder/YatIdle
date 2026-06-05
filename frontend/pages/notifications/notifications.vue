<template>
	<view class="nf-page">
		<view v-if="list.length > 0" class="nf-list">
			<view v-for="item in list" :key="item.id" class="nf-card" @click="onItemClick(item)">
				<view class="nf-icon-wrap" :class="item.iconBg">
					<text class="nf-icon">{{ item.icon }}</text>
				</view>
				<view class="nf-content">
					<view class="nf-top">
						<text class="nf-title">{{ item.title }}</text>
						<text class="nf-time">{{ item.time }}</text>
					</view>
					<text class="nf-desc">{{ item.desc }}</text>
					<view class="nf-order" v-if="item.orderInfo">
						<text class="nf-order-text">{{ item.orderInfo }}</text>
					</view>
				</view>
				<text class="nf-arrow" v-if="item.link">›</text>
			</view>
		</view>

		<view v-else-if="!loading" class="empty-wrap">
			<text class="empty-icon">🔔</text>
			<text class="empty-text">暂无通知</text>
			<text class="empty-hint">订单状态变更会在这里提醒你</text>
		</view>

		<view v-if="loading" class="loading-tip">加载中...</view>
	</view>
</template>

<script>
	import { getMySellOrders, getMyBuyOrders } from '@/api/order.js'

	export default {
		data() {
			return {
				list: [],
				loading: false
			}
		},
		onShow() {
			uni.setStorageSync('notifyLastSeen', new Date().toISOString())
			this.loadNotifications()
		},
		methods: {
			async loadNotifications() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					this.list = []
					return
				}
				this.loading = true
				try {
					const [sellResult, buyResult] = await Promise.all([
						getMySellOrders(user.id).catch(() => ({ records: [] })),
						getMyBuyOrders(user.id).catch(() => ({ records: [] }))
					])
					const sellOrders = (sellResult && sellResult.records) || sellResult || []
					const buyOrders = (buyResult && buyResult.records) || buyResult || []

					const notifications = []

					sellOrders.forEach(o => {
						notifications.push({
							id: 'sell-' + o.id,
							type: 'sell',
							icon: o.status === 'COMPLETED' ? '✅' : o.status === 'CANCELLED' ? '❌' : '📦',
							iconBg: o.status === 'COMPLETED' ? 'bg-green' : o.status === 'CANCELLED' ? 'bg-gray' : 'bg-orange',
							title: o.status === 'COMPLETED' ? '订单已完成' : o.status === 'CANCELLED' ? '订单已取消' : '新订单',
							desc: o.status === 'PENDING' ? '买家已下单，等待交易' : '',
							orderInfo: (o.itemTitle || '商品 #' + o.itemId) + '  ¥' + o.price,
							time: o.createTime ? o.createTime.slice(0, 16).replace('T', ' ') : '',
							order: o,
							link: true
						})
					})

					buyOrders.forEach(o => {
						notifications.push({
							id: 'buy-' + o.id,
							type: 'buy',
							icon: o.status === 'COMPLETED' ? '✅' : o.status === 'CANCELLED' ? '❌' : '🛒',
							iconBg: o.status === 'COMPLETED' ? 'bg-green' : o.status === 'CANCELLED' ? 'bg-gray' : 'bg-blue',
							title: o.status === 'COMPLETED' ? '交易完成' : o.status === 'CANCELLED' ? '订单已取消' : '已下单',
							desc: o.status === 'PENDING' ? '等待双方确认交易' : '',
							orderInfo: (o.itemTitle || '商品 #' + o.itemId) + '  ¥' + o.price,
							time: o.createTime ? o.createTime.slice(0, 16).replace('T', ' ') : '',
							order: o,
							link: true
						})
					})

					notifications.sort((a, b) => {
						const ta = a.order && a.order.createTime ? new Date(a.order.createTime).getTime() : 0
						const tb = b.order && b.order.createTime ? new Date(b.order.createTime).getTime() : 0
						return tb - ta
					})

					this.list = notifications
				} catch (e) {
					this.list = []
				} finally {
					this.loading = false
				}
			},
			onItemClick(item) {
				if (item.link && item.order) {
					const orderType = item.type === 'sell' ? 'sold' : 'purchased'
					uni.setStorageSync('currentOrder', {
						id: item.order.id,
						orderNo: item.order.orderNo,
						itemId: item.order.itemId,
						buyerId: item.order.buyerId,
						sellerId: item.order.sellerId,
						image: item.order.itemImageUrl || '',
						buyerName: item.order.buyerName || '',
						sellerName: item.order.sellerName || '',
						sellerAvatar: item.order.sellerAvatar || '',
						hasReviewed: item.order.hasReviewed || false,
						title: item.order.itemTitle || ('商品 #' + item.order.itemId),
						price: item.order.price,
						status: item.order.status,
						tradeLocation: item.order.tradeLocation,
						remark: item.order.remark,
						createTime: item.order.createTime,
						completeTime: item.order.completeTime,
						cancelTime: item.order.cancelTime,
						cancelReason: item.order.cancelReason,
						time: item.time
					})
					uni.navigateTo({ url: '/pages/order-detail/order-detail?id=' + item.order.id + '&type=' + orderType })
				}
			}
		}
	}
</script>

<style>
	.nf-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding: 20rpx;
		box-sizing: border-box;
	}

	.nf-list { display: flex; flex-direction: column; gap: 16rpx; }

	.nf-card {
		background: #ffffff;
		border-radius: 16rpx;
		padding: 24rpx;
		display: flex;
		align-items: flex-start;
		gap: 20rpx;
		box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.03);
	}

	.nf-icon-wrap {
		width: 80rpx; height: 80rpx;
		border-radius: 50%;
		display: flex; align-items: center; justify-content: center;
		flex-shrink: 0;
	}
	.nf-icon { font-size: 36rpx; }
	.bg-green { background: #e8f5e9; }
	.bg-orange { background: #fef5e7; }
	.bg-blue { background: #e3f2fd; }
	.bg-gray { background: #f0f0f0; }

	.nf-content { flex: 1; overflow: hidden; }
	.nf-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
	.nf-title { font-size: 28rpx; color: #333; font-weight: 500; }
	.nf-time { font-size: 22rpx; color: #ccc; flex-shrink: 0; margin-left: 12rpx; }
	.nf-desc { font-size: 24rpx; color: #999; line-height: 1.5; display: block; }
	.nf-order {
		margin-top: 12rpx;
		background: #f5f5f5;
		padding: 12rpx 16rpx;
		border-radius: 8rpx;
	}
	.nf-order-text { font-size: 24rpx; color: #666; }

	.nf-arrow { font-size: 32rpx; color: #ccc; flex-shrink: 0; margin-top: 24rpx; }

	/* 空态 */
	.empty-wrap {
		display: flex; flex-direction: column; align-items: center;
		padding-top: 200rpx;
	}
	.empty-icon { font-size: 80rpx; margin-bottom: 24rpx; }
	.empty-text { font-size: 30rpx; color: #999; margin-bottom: 12rpx; }
	.empty-hint { font-size: 24rpx; color: #ccc; }

	.loading-tip { text-align: center; padding: 60rpx; font-size: 24rpx; color: #ccc; }
</style>

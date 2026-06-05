<template>
	<view class="od-page">
		<!-- 订单状态 -->
		<view class="od-status-card" :class="statusBgClass">
			<view class="od-status-icon">{{ statusIcon }}</view>
			<text class="od-status-text">{{ statusLabel }}</text>
			<text class="od-status-hint" v-if="order.status === 'PENDING'">等待双方确认交易</text>
			<text class="od-status-hint" v-else-if="order.status === 'COMPLETED'">交易已完成</text>
			<text class="od-status-hint" v-else-if="order.status === 'CANCELLED'">订单已取消</text>
		</view>

		<!-- 商品信息 -->
		<view class="section-card">
			<view class="section-title">商品信息</view>
			<view class="goods-row" @click="goGoods">
				<image class="g-img" :src="order.image" mode="aspectFill"></image>
				<view class="g-info">
					<text class="g-title">{{ order.title }}</text>
					<text class="g-price">¥{{ order.price }}</text>
				</view>
				<text class="g-arrow">›</text>
			</view>
		</view>

		<!-- 订单信息 -->
		<view class="section-card">
			<view class="section-title">订单信息</view>
			<view class="info-row">
				<text class="info-label">订单编号</text>
				<text class="info-value selectable">{{ order.orderNo || order.id }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">订单状态</text>
				<text class="info-value" :class="statusTextClass">{{ statusLabel }}</text>
			</view>
			<view class="info-row" v-if="order.tradeLocation">
				<text class="info-label">交易地点</text>
				<text class="info-value">{{ order.tradeLocation }}</text>
			</view>
			<view class="info-row" v-if="order.remark">
				<text class="info-label">买家留言</text>
				<text class="info-value">{{ order.remark }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">创建时间</text>
				<text class="info-value">{{ order.createTime || order.time }}</text>
			</view>
			<view class="info-row" v-if="order.completeTime">
				<text class="info-label">完成时间</text>
				<text class="info-value">{{ order.completeTime }}</text>
			</view>
			<view class="info-row" v-if="order.cancelTime">
				<text class="info-label">取消时间</text>
				<text class="info-value">{{ order.cancelTime }}</text>
			</view>
			<view class="info-row" v-if="order.cancelReason">
				<text class="info-label">取消原因</text>
				<text class="info-value">{{ order.cancelReason }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">交易金额</text>
				<text class="info-value info-price">¥{{ order.price }}</text>
			</view>
		</view>

		<!-- 买卖双方信息 -->
		<view class="section-card">
			<view class="section-title">交易双方</view>
			<view class="party-row">
				<view class="party-item" @click="goProfile(order.buyerId)">
					<view class="party-avatar">
						<image v-if="order.buyerAvatar" class="party-avatar-img" :src="order.buyerAvatar" mode="aspectFill"></image>
						<text v-else class="party-avatar-emoji">{{ (order.buyerName || '?').charAt(0) }}</text>
					</view>
					<text class="party-label">买家</text>
					<text class="party-name">{{ order.buyerName || '用户 #' + order.buyerId }}</text>
				</view>
				<view class="party-arrow">⇄</view>
				<view class="party-item" @click="goProfile(order.sellerId)">
					<view class="party-avatar">
						<image v-if="order.sellerAvatar" class="party-avatar-img" :src="order.sellerAvatar" mode="aspectFill"></image>
						<text v-else class="party-avatar-emoji">{{ (order.sellerName || '?').charAt(0) }}</text>
					</view>
					<text class="party-label">卖家</text>
					<text class="party-name">{{ order.sellerName || '用户 #' + order.sellerId }}</text>
				</view>
			</view>
		</view>

		<!-- 底部操作：待交易状态 - 买家可见确认完成 -->
		<view class="od-bottom" v-if="order.status === 'PENDING' && orderType === 'purchased'">
			<button class="od-btn od-btn-outline" @click="onCancel">取消订单</button>
			<button class="od-btn od-btn-primary" @click="onComplete">确认完成</button>
		</view>
		<!-- 底部操作：待交易状态 - 卖家只能取消 -->
		<view class="od-bottom" v-if="order.status === 'PENDING' && orderType === 'sold'">
			<button class="od-btn od-btn-outline" style="flex: 1;" @click="onCancel">取消订单</button>
		</view>

		<!-- 底部操作：已完成 + 已购订单可评价 -->
		<view class="od-bottom" v-if="order.status === 'COMPLETED' && orderType === 'purchased' && !order.hasReviewed">
			<button class="od-btn od-btn-review" @click="goReview">去评价</button>
		</view>

		<!-- 底部操作：已完成 + 已购订单已评价 -->
		<view class="od-bottom" v-if="order.status === 'COMPLETED' && orderType === 'purchased' && order.hasReviewed">
			<button class="od-btn od-btn-done">已评价</button>
		</view>

		<!-- 底部操作：已完成 + 已售订单 -->
		<view class="od-bottom" v-if="order.status === 'COMPLETED' && orderType === 'sold'">
			<view class="od-done-text">交易已完成</view>
		</view>

		<!-- 已取消 -->
		<view class="od-bottom" v-if="order.status === 'CANCELLED'">
			<view class="od-done-text">订单已取消</view>
		</view>
	</view>
</template>

<script>
	import { cancelOrder, completeOrder } from '@/api/order.js'

	export default {
		data() {
			return {
				orderType: 'sold',
				order: {
					id: '',
					orderNo: '',
					itemId: null,
					buyerId: null,
					sellerId: null,
					buyerName: '',
					sellerName: '',
					sellerAvatar: '',
					buyerAvatar: '',
					image: '',
					title: '加载中...',
					price: '',
					status: '',
					tradeLocation: '',
					remark: '',
					counterparty: '',
					time: '',
					createTime: '',
					completeTime: '',
					cancelTime: '',
					cancelReason: '',
					hasReviewed: false
				}
			}
		},
		computed: {
			statusIcon() {
				if (this.order.status === 'COMPLETED') return '✅'
				if (this.order.status === 'CANCELLED') return '❌'
				return '📦'
			},
			statusLabel() {
				const map = { PENDING: '待交易', COMPLETED: '已完成', CANCELLED: '已取消' }
				return map[this.order.status] || this.order.status
			},
			statusBgClass() {
				if (this.order.status === 'COMPLETED') return 'status-done'
				if (this.order.status === 'CANCELLED') return 'status-cancel'
				return ''
			},
			statusTextClass() {
				if (this.order.status === 'COMPLETED') return 'text-done'
				if (this.order.status === 'CANCELLED') return 'text-cancel'
				return 'text-pending'
			}
		},
		onLoad(options) {
			this.orderType = options.type || 'sold'
			uni.setNavigationBarTitle({ title: '订单详情' })
			const stored = uni.getStorageSync('currentOrder')
			if (stored) {
				this.order = {
					id: stored.id || '',
					orderNo: stored.orderNo || '',
					itemId: stored.itemId || null,
					buyerId: stored.buyerId || null,
					sellerId: stored.sellerId || null,
				buyerName: stored.buyerName || '',
				sellerName: stored.sellerName || '',
				sellerAvatar: stored.sellerAvatar || '',
				buyerAvatar: stored.buyerAvatar || '',
					image: stored.image || '',
					title: stored.title || '商品 #' + (stored.itemId || ''),
					price: stored.price || '',
					status: stored.status || '',
					tradeLocation: stored.tradeLocation || '',
					remark: stored.remark || '',
					counterparty: stored.counterparty || '',
					time: stored.time || '',
					createTime: stored.createTime || stored.time || '',
					completeTime: stored.completeTime || '',
					cancelTime: stored.cancelTime || '',
					cancelReason: stored.cancelReason || '',
					hasReviewed: stored.hasReviewed || false
				}
				uni.removeStorageSync('currentOrder')
			}
		},
		methods: {
			goGoods() {
				if (this.order.itemId) {
					uni.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + this.order.itemId })
				}
			},
			goProfile(userId) {
				if (userId) {
					uni.navigateTo({ url: '/pages/profile/profile?id=' + userId })
				}
			},
			onCancel() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return
				}
				uni.showModal({
					title: '取消订单',
					content: '确认取消该订单？',
					success: async (res) => {
						if (res.confirm) {
							try {
								await cancelOrder(this.order.id, user.id, { cancelReason: '用户主动取消' })
								this.order.status = 'CANCELLED'
								this.order.cancelTime = new Date().toISOString()
								uni.showToast({ title: '已取消', icon: 'success' })
							} catch (e) {}
						}
					}
				})
			},
			onComplete() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return
				}
				uni.showModal({
					title: '确认完成',
					content: '确认交易完成？请在当面验货确认无误后再操作。',
					success: async (res) => {
						if (res.confirm) {
							try {
								await completeOrder(this.order.id, user.id)
								this.order.status = 'COMPLETED'
								this.order.completeTime = new Date().toISOString()
								this.order.hasReviewed = false
								uni.showToast({ title: '交易完成', icon: 'success' })
							} catch (e) {}
						}
					}
				})
			},
			goReview() {
				uni.navigateTo({
					url: '/pages/my-review/my-review?orderId=' + this.order.id + '&sellerId=' + (this.order.sellerId || '') + '&seller=' + encodeURIComponent(this.order.sellerName || '') + '&sellerAvatar=' + encodeURIComponent(this.order.sellerAvatar || '')
				})
			}
		}
	}
</script>

<style>
	.od-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding: 20rpx;
		box-sizing: border-box;
		padding-bottom: 160rpx;
	}

	/* 状态卡片 */
	.od-status-card {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		border-radius: 20rpx;
		padding: 48rpx 40rpx;
		display: flex; flex-direction: column; align-items: center;
		margin-bottom: 20rpx;
	}
	.od-status-card.status-done {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
	}
	.od-status-card.status-cancel {
		background: linear-gradient(135deg, #999, #b0b0b0);
	}
	.od-status-icon { font-size: 64rpx; margin-bottom: 16rpx; }
	.od-status-text { font-size: 36rpx; color: #ffffff; font-weight: bold; }
	.od-status-hint { font-size: 24rpx; color: rgba(255,255,255,0.8); margin-top: 8rpx; }

	/* 信息卡片 */
	.section-card {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 24rpx;
		margin-bottom: 20rpx;
	}
	.section-title {
		font-size: 28rpx; color: #999;
		padding-bottom: 20rpx; border-bottom: 1rpx solid #f5f5f5;
		margin-bottom: 16rpx;
	}

	/* 商品行 */
	.goods-row {
		display: flex; align-items: center; gap: 16rpx;
	}
	.g-img {
		width: 120rpx; height: 120rpx;
		border-radius: 12rpx; background: #eee;
		flex-shrink: 0;
	}
	.g-info { flex: 1; display: flex; flex-direction: column; gap: 10rpx; }
	.g-title {
		font-size: 28rpx; color: #333;
		display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2;
		overflow: hidden;
	}
	.g-price { font-size: 30rpx; color: #e74c3c; font-weight: bold; }
	.g-arrow { font-size: 32rpx; color: #ccc; }

	/* 信息行 */
	.info-row {
		display: flex; justify-content: space-between; align-items: flex-start;
		padding: 18rpx 0;
		border-bottom: 1rpx solid #f5f5f5;
	}
	.info-row:last-child { border-bottom: none; }
	.info-label { font-size: 26rpx; color: #999; flex-shrink: 0; margin-right: 20rpx; }
	.info-value { font-size: 26rpx; color: #333; text-align: right; word-break: break-all; flex: 1; }
	.info-price { color: #e74c3c; font-weight: bold; font-size: 30rpx; }
	.selectable { user-select: text; }
	.text-done { color: #2E7D32; }
	.text-cancel { color: #999; }
	.text-pending { color: #f0ad4e; }

	/* 双方信息 */
	.party-row {
		display: flex; align-items: center; justify-content: space-between;
		padding: 8rpx 0;
	}
	.party-item {
		display: flex; flex-direction: column; align-items: center;
		flex: 1; cursor: pointer;
	}
	.party-avatar {
		width: 88rpx; height: 88rpx;
		background: #e8f5ee; border-radius: 50%;
		display: flex; align-items: center; justify-content: center;
		font-size: 36rpx; margin-bottom: 12rpx; overflow: hidden;
	}
	.party-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
	.party-avatar-emoji { font-size: 36rpx; }
	.party-label { font-size: 24rpx; color: #999; margin-bottom: 8rpx; }
	.party-name { font-size: 28rpx; color: #333; font-weight: 500; }
	.party-arrow { font-size: 32rpx; color: #ccc; margin: 0 20rpx; }

	/* 底部操作 */
	.od-bottom {
		position: fixed; bottom: 0; left: 0; right: 0;
		background: #ffffff; padding: 16rpx 30rpx;
		display: flex; gap: 20rpx;
		border-top: 1rpx solid #f0f0f0;
		box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.04);
		box-sizing: border-box;
		padding-bottom: calc(16rpx + env(safe-area-inset-bottom));
	}
	.od-btn { flex: 1; height: 80rpx; line-height: 80rpx; text-align: center; border-radius: 40rpx; font-size: 28rpx; border: none; margin: 0; }
	.od-btn-outline { background: #f5f5f5; color: #333; }
	.od-btn-primary { background: linear-gradient(135deg, #3A6341, #4E7D56); color: #ffffff; }
	.od-btn-review { background: linear-gradient(135deg, #f0ad4e, #f5c26b); color: #ffffff; flex: 1; }
	.od-btn-done { background: #e0e0e0; color: #999; flex: 1; }
	.od-btn::after { border: none; }
	.od-done-text { flex: 1; text-align: center; font-size: 28rpx; color: #999; padding: 24rpx 0; }
</style>

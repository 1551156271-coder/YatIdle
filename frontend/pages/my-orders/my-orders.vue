<template>
	<view class="orders-page">
		<view v-if="orderList.length > 0" class="order-list">
			<view v-for="o in orderList" :key="o.id" class="o-card" @click="goDetail(o)">
				<view class="o-top">
					<image class="o-img" :src="o.image" mode="aspectFill"></image>
					<view class="o-info">
						<text class="o-title">{{ o.title }}</text>
						<text class="o-price">¥{{ o.price }}</text>
					</view>
					<text class="o-status" :class="statusClass(o.status)">{{ o.statusLabel }}</text>
				</view>
				<view class="o-bottom">
					<text class="o-people">订单号：{{ o.orderNo }}</text>
					<text class="o-time">{{ o.time }}</text>
				</view>
			</view>
		</view>

		<view v-else-if="!loading" class="empty-wrap">
			<text class="empty-icon">{{ orderType === 'sold' ? '💰' : '🛒' }}</text>
			<text class="empty-text">暂无{{ orderType === 'sold' ? '已售' : '已购' }}订单</text>
		</view>

		<view v-if="loading" class="loading-tip">加载中...</view>
	</view>
</template>

<script>
	import { getMySellOrders, getMyBuyOrders } from '@/api/order.js'

	export default {
		data() {
			return {
				orderType: 'sold',
				orderList: [],
				loading: false
			}
		},
		onLoad(options) {
			this.orderType = options.type || 'sold'
			const title = this.orderType === 'sold' ? '已售订单' : '已购订单'
			uni.setNavigationBarTitle({ title })
			this.loadOrders()
		},
		methods: {
			async loadOrders() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					this.orderList = []
					return
				}
				this.loading = true
				try {
					const result = this.orderType === 'sold'
						? await getMySellOrders(user.id)
						: await getMyBuyOrders(user.id)
					const list = (result && result.records) || result || []
					this.orderList = list.map(o => ({
						id: o.id,
						orderNo: o.orderNo,
						itemId: o.itemId,
						buyerId: o.buyerId,
						sellerId: o.sellerId,
						buyerName: o.buyerName || '',
						sellerName: o.sellerName || '',
						sellerAvatar: o.sellerAvatar || '',
						buyerAvatar: o.buyerAvatar || '',
						image: o.itemImageUrl || '',
						title: o.itemTitle || ('商品 #' + o.itemId),
						price: o.price,
						status: o.status,
						statusLabel: this.mapStatus(o.status),
						tradeLocation: o.tradeLocation,
						remark: o.remark,
						counterparty: '',
						createTime: o.createTime,
						completeTime: o.completeTime,
						cancelTime: o.cancelTime,
						cancelReason: o.cancelReason,
						hasReviewed: o.hasReviewed || false,
						time: o.createTime ? o.createTime.slice(0, 10) : ''
					}))
				} catch (e) {
					this.orderList = []
				} finally {
					this.loading = false
				}
			},
			mapStatus(status) {
				const map = { PENDING: '待交易', COMPLETED: '已完成', CANCELLED: '已取消' }
				return map[status] || status
			},
			statusClass(status) {
				if (status === 'COMPLETED') return 'os-done'
				if (status === 'CANCELLED') return 'os-cancel'
				return 'os-pending'
			},
			goDetail(o) {
				uni.setStorageSync('currentOrder', o)
				uni.navigateTo({ url: '/pages/order-detail/order-detail?id=' + o.id + '&type=' + this.orderType })
			}
		}
	}
</script>

<style>
	.orders-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding: 20rpx;
		box-sizing: border-box;
	}
	.order-list { display: flex; flex-direction: column; gap: 16rpx; }
	.o-card {
		background: #ffffff; border-radius: 16rpx;
		padding: 24rpx;
		box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.03);
	}
	.o-top { display: flex; align-items: center; gap: 16rpx; margin-bottom: 20rpx; }
	.o-img {
		width: 120rpx; height: 120rpx;
		border-radius: 12rpx; background: #eee;
		flex-shrink: 0;
	}
	.o-info { flex: 1; display: flex; flex-direction: column; gap: 10rpx; }
	.o-title {
		font-size: 28rpx; color: #333;
		display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2;
		overflow: hidden;
	}
	.o-price { font-size: 30rpx; color: #e74c3c; font-weight: bold; }
	.o-status { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 6rpx; flex-shrink: 0; }
	.os-done { color: #2E7D32; background: #e8f5e9; }
	.os-pending { color: #f0ad4e; background: #fef5e7; }
	.os-cancel { color: #999; background: #f0f0f0; }

	.o-bottom { display: flex; justify-content: space-between; align-items: center; }
	.o-people { font-size: 24rpx; color: #999; }
	.o-time { font-size: 24rpx; color: #ccc; }

	/* 空态 */
	.empty-wrap {
		display: flex; flex-direction: column; align-items: center;
		padding-top: 200rpx;
	}
	.empty-icon { font-size: 80rpx; margin-bottom: 24rpx; }
	.empty-text { font-size: 28rpx; color: #999; }

	.loading-tip { text-align: center; padding: 60rpx; font-size: 24rpx; color: #ccc; }
</style>

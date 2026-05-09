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
					<text class="o-status" :class="statusClass(o.status)">{{ o.status }}</text>
				</view>
				<view class="o-bottom">
					<text class="o-people">{{ orderType === 'sold' ? '买家' : '卖家' }}：{{ o.counterparty }}</text>
					<text class="o-time">{{ o.time }}</text>
				</view>
			</view>
		</view>
		<view v-else class="empty-wrap">
			<text class="empty-icon">{{ orderType === 'sold' ? '💰' : '🛒' }}</text>
			<text class="empty-text">暂无{{ orderType === 'sold' ? '已售' : '已购' }}订单</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				orderType: 'sold',
				orderList: []
			}
		},
		onLoad(options) {
			this.orderType = options.type || 'sold'
			const title = this.orderType === 'sold' ? '已售订单' : '已购订单'
			uni.setNavigationBarTitle({ title })
			this.loadOrders()
		},
		methods: {
			loadOrders() {
				const soldOrders = [
					{ id: 'S001', image: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop', title: '九成新公路自行车', price: '268.00', status: '已完成', counterparty: '李四', time: '2026-05-08' },
					{ id: 'S002', image: 'https://images.unsplash.com/photo-1585435557343-3b092031a831?q=80&w=400&auto=format&fit=crop', title: 'LED护眼台灯', price: '35.00', status: '已完成', counterparty: '王五', time: '2026-05-03' },
					{ id: 'S003', image: 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=400&auto=format&fit=crop', title: '二手教材《高等数学》', price: '8.00', status: '待发货', counterparty: '赵六', time: '2026-05-09' }
				]
				const purchasedOrders = [
					{ id: 'P001', image: 'https://images.unsplash.com/photo-1544816155-12df9643f363?q=80&w=400&auto=format&fit=crop', title: '四六级真题全套', price: '12.00', status: '已完成', counterparty: '张三', time: '2026-04-28' },
					{ id: 'P002', image: 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?q=80&w=400&auto=format&fit=crop', title: '蓝牙耳机', price: '45.00', status: '待收货', counterparty: '张三', time: '2026-05-07' }
				]
				this.orderList = this.orderType === 'sold' ? soldOrders : purchasedOrders
			},
			statusClass(status) {
				if (status === '已完成') return 'os-done'
				if (status === '待发货') return 'os-pending'
				if (status === '待收货') return 'os-pending'
				return ''
			},
			goDetail(o) {
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
	.os-done { color: #999; background: #f0f0f0; }
	.os-pending { color: #f0ad4e; background: #fef5e7; }

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
</style>

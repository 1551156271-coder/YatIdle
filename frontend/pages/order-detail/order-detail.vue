<template>
	<view class="od-page">
		<!-- 订单状态 -->
		<view class="od-status-card">
			<view class="od-status-icon">{{ statusIcon }}</view>
			<text class="od-status-text">{{ order.status }}</text>
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
				<text class="info-value">{{ order.id }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">{{ orderType === 'sold' ? '买家' : '卖家' }}</text>
				<text class="info-value">{{ order.counterparty }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">交易时间</text>
				<text class="info-value">{{ order.time }}</text>
			</view>
			<view class="info-row">
				<text class="info-label">交易金额</text>
				<text class="info-value info-price">¥{{ order.price }}</text>
			</view>
		</view>

		<!-- 底部操作 -->
		<view class="od-bottom" v-if="order.status !== '已完成'">
			<view class="od-btn od-btn-outline" @click="onContact">联系对方</view>
			<view class="od-btn od-btn-primary" @click="onConfirm">
				{{ orderType === 'sold' ? '确认发货' : '确认收货' }}
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				orderType: 'sold',
				order: {
					id: '',
					image: '',
					title: '',
					price: '',
					status: '',
					counterparty: '',
					time: '',
					goodsId: 1
				}
			}
		},
		computed: {
			statusIcon() {
				if (this.order.status === '已完成') return '✅'
				if (this.order.status === '待发货') return '📦'
				return '🚚'
			}
		},
		onLoad(options) {
			this.orderType = options.type || 'sold'
			const id = options.id || ''
			uni.setNavigationBarTitle({ title: '订单详情' })
			this.loadOrder(id)
		},
		methods: {
			loadOrder(id) {
				const mockOrders = {
					'S001': { id: 'S001', image: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop', title: '九成新公路自行车', price: '268.00', status: '已完成', counterparty: '李四', time: '2026-05-08 14:30', goodsId: 1 },
					'S002': { id: 'S002', image: 'https://images.unsplash.com/photo-1585435557343-3b092031a831?q=80&w=400&auto=format&fit=crop', title: 'LED护眼台灯', price: '35.00', status: '已完成', counterparty: '王五', time: '2026-05-03 10:15', goodsId: 2 },
					'S003': { id: 'S003', image: 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=400&auto=format&fit=crop', title: '二手教材《高等数学》', price: '8.00', status: '待发货', counterparty: '赵六', time: '2026-05-09 09:00', goodsId: 4 },
					'P001': { id: 'P001', image: 'https://images.unsplash.com/photo-1544816155-12df9643f363?q=80&w=400&auto=format&fit=crop', title: '四六级真题全套', price: '12.00', status: '已完成', counterparty: '张三', time: '2026-04-28 16:20', goodsId: 3 },
					'P002': { id: 'P002', image: 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?q=80&w=400&auto=format&fit=crop', title: '蓝牙耳机', price: '45.00', status: '待收货', counterparty: '张三', time: '2026-05-07 11:45', goodsId: 5 }
				}
				if (mockOrders[id]) {
					this.order = mockOrders[id]
				}
			},
			goGoods() {
				uni.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + this.order.goodsId })
			},
			onContact() {
				uni.showToast({ title: '聊天功能即将上线', icon: 'none' })
			},
			onConfirm() {
				const action = this.orderType === 'sold' ? '发货' : '收货'
				uni.showToast({ title: action + '成功', icon: 'none' })
				this.order.status = '已完成'
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
		padding-bottom: 140rpx;
	}

	/* 状态卡片 */
	.od-status-card {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		border-radius: 20rpx;
		padding: 40rpx;
		display: flex; flex-direction: column; align-items: center;
		margin-bottom: 20rpx;
	}
	.od-status-icon { font-size: 64rpx; margin-bottom: 16rpx; }
	.od-status-text { font-size: 32rpx; color: #ffffff; font-weight: bold; }

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
		display: flex; justify-content: space-between; align-items: center;
		padding: 18rpx 0;
		border-bottom: 1rpx solid #f5f5f5;
	}
	.info-row:last-child { border-bottom: none; }
	.info-label { font-size: 26rpx; color: #999; }
	.info-value { font-size: 26rpx; color: #333; }
	.info-price { color: #e74c3c; font-weight: bold; }

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
	.od-btn { flex: 1; height: 80rpx; line-height: 80rpx; text-align: center; border-radius: 40rpx; font-size: 28rpx; }
	.od-btn-outline { background: #f5f5f5; color: #333; }
	.od-btn-primary { background: linear-gradient(135deg, #3A6341, #4E7D56); color: #ffffff; }
</style>

<template>
	<view class="listings-page">
		<view v-if="goodsList.length > 0" class="goods-list">
			<view v-for="g in goodsList" :key="g.id" class="g-card" @click="goDetail(g)">
				<image class="g-img" :src="g.image" mode="aspectFill"></image>
				<view class="g-info">
					<text class="g-title">{{ g.title }}</text>
					<view class="g-bottom">
						<text class="g-price">¥{{ g.price }}</text>
						<text class="g-status" :class="g.status === '在售' ? 'status-on' : 'status-off'">{{ g.status }}</text>
					</view>
				</view>
			</view>
		</view>
		<view v-else class="empty-wrap">
			<text class="empty-icon">📦</text>
			<text class="empty-text">暂无发布的商品</text>
			<view class="empty-btn" @click="goPublish">去发布</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				goodsList: [
					{ id: 1, image: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop', title: '九成新公路自行车', price: '268.00', status: '在售' },
					{ id: 2, image: 'https://images.unsplash.com/photo-1585435557343-3b092031a831?q=80&w=400&auto=format&fit=crop', title: 'LED护眼台灯', price: '35.00', status: '在售' },
					{ id: 3, image: 'https://images.unsplash.com/photo-1544816155-12df9643f363?q=80&w=400&auto=format&fit=crop', title: '四六级真题全套', price: '12.00', status: '已售' },
					{ id: 4, image: 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=400&auto=format&fit=crop', title: '二手教材《高等数学》', price: '8.00', status: '在售' },
					{ id: 5, image: 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?q=80&w=400&auto=format&fit=crop', title: '蓝牙耳机', price: '45.00', status: '已下架' }
				]
			}
		},
		methods: {
			goDetail(g) {
				uni.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + g.id })
			},
			goPublish() {
				uni.switchTab({ url: '/pages/publish/publish' })
			}
		}
	}
</script>

<style>
	.listings-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding: 20rpx;
		box-sizing: border-box;
	}
	.goods-list { display: flex; flex-direction: column; gap: 16rpx; }
	.g-card {
		background: #ffffff; border-radius: 16rpx;
		display: flex; padding: 20rpx;
		box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.03);
	}
	.g-img {
		width: 160rpx; height: 160rpx;
		border-radius: 12rpx; background: #eee;
		flex-shrink: 0; margin-right: 20rpx;
	}
	.g-info { flex: 1; display: flex; flex-direction: column; justify-content: space-between; }
	.g-title {
		font-size: 28rpx; color: #333; line-height: 1.4;
		display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2;
		overflow: hidden;
	}
	.g-bottom { display: flex; justify-content: space-between; align-items: center; }
	.g-price { font-size: 32rpx; color: #e74c3c; font-weight: bold; }
	.g-status { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 6rpx; }
	.status-on { color: #00613C; background: #e8f5ee; }
	.status-off { color: #999; background: #f0f0f0; }

	/* 空态 */
	.empty-wrap {
		display: flex; flex-direction: column; align-items: center;
		padding-top: 200rpx;
	}
	.empty-icon { font-size: 80rpx; margin-bottom: 24rpx; }
	.empty-text { font-size: 28rpx; color: #999; margin-bottom: 40rpx; }
	.empty-btn {
		font-size: 28rpx; color: #ffffff;
		background: linear-gradient(135deg, #00613C, #00804B);
		padding: 16rpx 60rpx; border-radius: 44rpx;
	}
</style>

<template>
	<view class="listings-page">
		<view v-if="goodsList.length > 0" class="goods-list">
			<view v-for="g in goodsList" :key="g.id" class="g-card" @click="goDetail(g)">
				<image class="g-img" :src="g.image" mode="aspectFill"></image>
				<view class="g-info">
					<text class="g-title">{{ g.title }}</text>
					<view class="g-bottom">
						<text class="g-price">¥{{ g.price }}</text>
						<text class="g-status" :class="g.status === 'ON_SALE' ? 'status-on' : 'status-off'">{{ g.statusLabel }}</text>
					</view>
				</view>
			</view>
		</view>
		<view v-else-if="!loading" class="empty-wrap">
			<text class="empty-icon">📦</text>
			<text class="empty-text">暂无发布的商品</text>
			<view class="empty-btn" @click="goPublish">去发布</view>
		</view>
		<view v-if="loading" class="loading-tip">加载中...</view>
	</view>
</template>

<script>
	import { getUserItems } from '@/api/item.js'

	export default {
		data() {
			return {
				goodsList: [],
				loading: false
			}
		},
		onShow() {
			this.loadListings()
		},
		methods: {
			async loadListings() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) return
				this.loading = true
				try {
					const result = await getUserItems(user.id)
					const list = result.records || result || []
					this.goodsList = list.map(item => ({
						id: item.id,
						image: item.imageUrl || '',
						title: item.title,
						price: item.price,
						status: item.status,
						statusLabel: this.mapStatus(item.status)
					}))
				} catch (e) {
					this.goodsList = []
				} finally {
					this.loading = false
				}
			},
			mapStatus(status) {
				const map = { ON_SALE: '在售', SOLD: '已售', REMOVED: '已下架' }
				return map[status] || status
			},
			goDetail(g) {
				const statusMap = { ON_SALE: '在售', SOLD: '已售', REMOVED: '已下架' }
				uni.navigateTo({
					url: '/pages/goods-detail/goods-detail?id=' + g.id + '&mode=seller&status=' + (statusMap[g.status] || '在售')
				})
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
	.status-on { color: #3A6341; background: #e8f5ee; }
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
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		padding: 16rpx 60rpx; border-radius: 44rpx;
	}

	.loading-tip { text-align: center; padding: 60rpx; font-size: 24rpx; color: #ccc; }
</style>

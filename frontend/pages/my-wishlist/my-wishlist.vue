<template>
	<view class="wishlist-page">
		<view v-if="wishList.length > 0" class="goods-grid">
			<view v-for="g in wishList" :key="g.id" class="g-card" @click="goDetail(g)">
				<image class="g-img" :src="g.image" mode="aspectFill"></image>
				<view class="g-info">
					<text class="g-title">{{ g.title }}</text>
					<view class="g-bottom">
						<text class="g-price">¥{{ g.price }}</text>
						<text class="g-heart iconfont icon-xz"></text>
					</view>
				</view>
			</view>
		</view>
		<view v-else-if="!loading" class="empty-wrap">
			<text class="empty-icon">💝</text>
			<text class="empty-text">心愿单还是空的</text>
			<text class="empty-sub">去首页逛逛，收藏喜欢的宝贝吧</text>
		</view>
		<view v-if="loading" class="loading-tip">加载中...</view>
	</view>
</template>

<script>
	import { getMyFavorites } from '@/api/favorite.js'

	export default {
		data() {
			return {
				wishList: [],
				loading: false
			}
		},
		onShow() {
			this.loadWishlist()
		},
		methods: {
			async loadWishlist() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) return
				this.loading = true
				try {
					const result = await getMyFavorites(user.id); const list = (result && result.records) || result || []
					this.wishList = list.map(f => ({
						id: f.itemId,
						image: '',
						title: f.itemTitle || '商品 #' + f.itemId,
						price: f.price
					}))
				} catch (e) {
					this.wishList = []
				} finally {
					this.loading = false
				}
			},
			goDetail(g) {
				uni.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + g.id })
			}
		}
	}
</script>

<style>
	.wishlist-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding: 20rpx;
		box-sizing: border-box;
	}
	.goods-grid {
		display: flex; flex-wrap: wrap;
		margin: -10rpx;
	}
	.g-card {
		width: calc(50% - 20rpx);
		margin: 10rpx;
		background: #ffffff; border-radius: 16rpx;
		overflow: hidden;
		box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.03);
	}
	.g-img {
		width: 100%; height: 320rpx;
		background: #eee;
	}
	.g-info { padding: 16rpx 18rpx 20rpx; }
	.g-title {
		font-size: 26rpx; color: #333; line-height: 1.4;
		display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2;
		overflow: hidden; margin-bottom: 12rpx;
	}
	.g-bottom { display: flex; justify-content: space-between; align-items: center; }
	.g-price { font-size: 30rpx; color: #e74c3c; font-weight: bold; }
	.g-heart { font-size: 32rpx; color: #E85A4F; }

	/* 空态 */
	.empty-wrap {
		display: flex; flex-direction: column; align-items: center;
		padding-top: 200rpx;
	}
	.empty-icon { font-size: 80rpx; margin-bottom: 24rpx; }
	.empty-text { font-size: 30rpx; color: #999; margin-bottom: 12rpx; }
	.empty-sub { font-size: 24rpx; color: #ccc; }

	.loading-tip { text-align: center; padding: 60rpx; font-size: 24rpx; color: #ccc; }
</style>

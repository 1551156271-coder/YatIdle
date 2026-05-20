<template>
	<view class="pg-page">
		<!-- 顶部导航 -->
		<view class="pg-header" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="header-back" @click="goBack">
				<text class="back-icon">‹</text>
			</view>
			<text class="header-title">{{ user.nickname }}的在售商品</text>
			<view class="header-placeholder"></view>
		</view>

		<!-- 商品列表 -->
		<view v-if="goodsList.length > 0" class="goods-grid">
			<view v-for="g in goodsList" :key="g.id" class="goods-card" @click="goDetail(g)">
				<image class="goods-img" :src="g.image" mode="aspectFill"></image>
				<view class="goods-info">
					<text class="goods-title">{{ g.title }}</text>
					<view class="goods-bottom">
						<view class="goods-price">
							<text class="price-symbol">¥</text>
							<text class="price-num">{{ g.price }}</text>
						</view>
						<text class="goods-meta">{{ g.campus || '' }}</text>
					</view>
				</view>
			</view>
		</view>

		<view v-else-if="!loading" class="empty-state">
			<text class="empty-icon">📦</text>
			<text class="empty-text">暂无在售商品</text>
		</view>
	</view>
</template>

<script>
	import { getUserItems } from '@/api/item.js'

	export default {
		data() {
			return {
				statusBarHeight: 44,
				user: { nickname: '用户' },
				goodsList: [],
				loading: false
			}
		},
		onLoad(options) {
			try {
				const sys = uni.getSystemInfoSync()
				this.statusBarHeight = sys.statusBarHeight || 44
			} catch (e) {
				this.statusBarHeight = 44
			}
			if (options.id) {
				this.loadGoods(options.id)
			}
		},
		methods: {
			async loadGoods(userId) {
				this.loading = true
				try {
					const result = await getUserItems(userId, { status: 'ON_SALE' })
					const list = result.records || result || []
					this.goodsList = list.map(item => ({
						id: item.id,
						image: item.imageUrl || '',
						title: item.title,
						price: item.price,
						campus: item.campus || ''
					}))
				} catch (e) {
					this.goodsList = []
				} finally {
					this.loading = false
				}
			},
			goBack() {
				uni.navigateBack()
			},
			goDetail(g) {
				uni.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + g.id })
			}
		}
	}
</script>

<style>
	.pg-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
	}

	/* ===== 顶部导航 ===== */
	.pg-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding-left: 20rpx;
		padding-right: 20rpx;
		padding-bottom: 20rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		box-sizing: border-box;
	}
	.header-back { width: 60rpx; display: flex; align-items: center; justify-content: center; }
	.back-icon { font-size: 60rpx; color: #fff; line-height: 1; }
	.header-title { font-size: 34rpx; color: #fff; font-weight: bold; flex: 1; text-align: center; }
	.header-placeholder { width: 60rpx; }

	/* ===== 商品网格 ===== */
	.goods-grid { display: flex; flex-wrap: wrap; padding: 16rpx; gap: 16rpx; }
	.goods-card {
		width: calc(50% - 8rpx);
		background: #ffffff; border-radius: 16rpx;
		overflow: hidden; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
	}
	.goods-img { width: 100%; height: 340rpx; background: #eee; }
	.goods-info { padding: 16rpx; }
	.goods-title {
		font-size: 26rpx; color: #333; line-height: 1.4;
		display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2;
		overflow: hidden; margin-bottom: 12rpx;
	}
	.goods-bottom { display: flex; justify-content: space-between; align-items: center; }
	.goods-price { display: flex; align-items: baseline; }
	.price-symbol { font-size: 22rpx; color: #e74c3c; font-weight: bold; }
	.price-num { font-size: 30rpx; color: #e74c3c; font-weight: bold; }
	.goods-meta { font-size: 20rpx; color: #999; }

	/* 空态 */
	.empty-state { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx; }
	.empty-icon { font-size: 80rpx; margin-bottom: 24rpx; }
	.empty-text { font-size: 28rpx; color: #999; }
</style>

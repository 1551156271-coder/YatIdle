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

		<view v-else class="empty-state">
			<text class="empty-icon">📦</text>
			<text class="empty-text">暂无在售商品</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				statusBarHeight: 44,
				user: { nickname: '用户' },
				goodsList: []
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
			loadGoods(userId) {
				// TODO: 从API获取，此处模拟数据
				const mockUsers = {
					'2': {
						nickname: '张三（卖家）',
						goods: [
							{ id: 1, image: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop', title: '九成新公路自行车，带锁和挡泥板', price: '268.00', campus: '东校园' },
							{ id: 4, image: 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=400&auto=format&fit=crop', title: '二手教材《高等数学》', price: '8.00', campus: '东校园' },
							{ id: 5, image: 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?q=80&w=400&auto=format&fit=crop', title: '蓝牙耳机', price: '45.00', campus: '东校园' }
						]
					}
				}
				const data = mockUsers[userId]
				if (data) {
					this.user.nickname = data.nickname
					this.goodsList = data.goods
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

	.header-back {
		width: 60rpx;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.back-icon {
		font-size: 80rpx;
		color: #ffffff;
		font-weight: 300;
		line-height: 0.9;
	}

	.header-title {
		font-size: 36rpx;
		color: #ffffff;
		font-weight: bold;
	}

	.header-placeholder {
		width: 60rpx;
	}

	/* ===== 商品列表 ===== */
	.goods-grid {
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
		padding: 20rpx 16rpx 0;
		box-sizing: border-box;
	}

	.goods-card {
		width: calc(50% - 10rpx);
		background: #ffffff;
		border-radius: 16rpx;
		overflow: hidden;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
		box-sizing: border-box;
	}

	.goods-img {
		width: 100%;
		height: 340rpx;
		background: #f0f0f0;
	}

	.goods-info {
		padding: 16rpx 20rpx 20rpx;
		box-sizing: border-box;
	}

	.goods-title {
		font-size: 26rpx;
		color: #333;
		line-height: 1.4;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
		margin-bottom: 16rpx;
	}

	.goods-bottom {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.goods-price {
		display: flex;
		align-items: baseline;
	}

	.price-symbol {
		font-size: 22rpx;
		color: #e74c3c;
		font-weight: bold;
	}

	.price-num {
		font-size: 34rpx;
		color: #e74c3c;
		font-weight: bold;
	}

	.goods-meta {
		font-size: 22rpx;
		color: #999;
	}

	/* ===== 空状态 ===== */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: 240rpx;
	}
	.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
	.empty-text { font-size: 28rpx; color: #999; }
</style>

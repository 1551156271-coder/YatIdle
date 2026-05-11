<template>
	<view class="home-page">
		<!-- 顶部搜索栏 -->
		<view class="search-bar" @click="onSearchClick">
			<text class="search-icon">🔍</text>
			<text class="search-placeholder">搜索你想要的二手好物</text>
		</view>

		<!-- 分类标签 -->
		<scroll-view class="category-scroll" scroll-x :show-scrollbar="false">
			<view
				v-for="item in categories"
				:key="item.value"
				class="category-tag"
				:class="{ 'category-active': activeCategory === item.value }"
				@click="switchCategory(item.value)"
			>
				{{ item.label }}
			</view>
		</scroll-view>

		<!-- 商品列表（双列） -->
		<view v-if="activeCategory !== 'wanted' && goodsList.length > 0" class="goods-grid">
			<view
				v-for="goods in goodsList"
				:key="goods.id"
				class="goods-card"
				@click="goToDetail(goods)"
			>
				<image class="goods-img" :src="goods.image" mode="aspectFill"></image>
				<view class="goods-info">
					<text class="goods-title">{{ goods.title }}</text>
					<view class="goods-bottom">
						<view class="goods-price">
							<text class="price-symbol">¥</text>
							<text class="price-num">{{ goods.price }}</text>
						</view>
						<text class="goods-meta">{{ goods.campus }}</text>
					</view>
				</view>
			</view>
		</view>

		<view v-if="loading" class="loading-tip">加载中...</view>
		<view v-if="!hasMore && goodsList.length > 0" class="loading-tip">— 没有更多了 —</view>

		<view v-if="goodsList.length === 0 && !loading" class="empty-state">
			<text class="empty-icon">📦</text>
			<text class="empty-text">该分类暂无商品</text>
		</view>

		<!-- 底部安全距离 -->
		<view class="bottom-safe"></view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				activeCategory: 'all',
				loading: false,
				hasMore: true,
				page: 1,
				categories: [
					{ label: '全部', value: 'all' },
					{ label: '数码电子', value: 'digital' },
					{ label: '书籍教材', value: 'book' },
					{ label: '生活用品', value: 'life' },
					{ label: '运动户外', value: 'sport' },
					{ label: '服饰鞋包', value: 'fashion' },
					{ label: '其他', value: 'other' }
				],
				mockGoods: [
					{ id: 1, image: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop', title: '九成新公路自行车，带锁和挡泥板', price: '268.00', campus: '东校园', category: 'sport' },
					{ id: 2, image: 'https://images.unsplash.com/photo-1546868871-af0de0ae72be?q=80&w=400&auto=format&fit=crop', title: 'iPad 2021 64G 银色，考研自用', price: '1580.00', campus: '南校园', category: 'digital' },
					{ id: 3, image: 'https://images.unsplash.com/photo-1524995997946-a1c2e315a42f?q=80&w=400&auto=format&fit=crop', title: '高等数学上下册+习题集', price: '25.00', campus: '北校园', category: 'book' },
					{ id: 4, image: 'https://images.unsplash.com/photo-1585435557343-3b092031a831?q=80&w=400&auto=format&fit=crop', title: '台灯 LED 护眼学习灯', price: '35.00', campus: '东校园', category: 'life' },
					{ id: 5, image: 'https://images.unsplash.com/photo-1602143407151-7111542de6e8?q=80&w=400&auto=format&fit=crop', title: '头戴式降噪耳机，99新', price: '189.00', campus: '深圳校区', category: 'digital' },
					{ id: 6, image: 'https://images.unsplash.com/photo-1511499767150-a48a237f0083?q=80&w=400&auto=format&fit=crop', title: '雷朋太阳镜经典款', price: '320.00', campus: '珠海校区', category: 'fashion' },
					{ id: 7, image: 'https://images.unsplash.com/photo-1509042239860-f550ce710b93?q=80&w=400&auto=format&fit=crop', title: 'Dell 24寸显示器，带HDMI线', price: '450.00', campus: '东校园', category: 'digital' },
					{ id: 8, image: 'https://images.unsplash.com/photo-1544816155-12df9643f363?q=80&w=400&auto=format&fit=crop', title: '大学英语四六级真题全套', price: '12.00', campus: '南校园', category: 'book' },
					{ id: 9, image: 'https://images.unsplash.com/photo-1461896836934-bd45ba33ea39?q=80&w=400&auto=format&fit=crop', title: '瑜伽垫加厚防滑，只用过两次', price: '28.00', campus: '北校园', category: 'sport' },
					{ id: 10, image: 'https://images.unsplash.com/photo-1553062407-98eeb64c6a62?q=80&w=400&auto=format&fit=crop', title: 'Kindle Paperwhite 电子书', price: '420.00', campus: '东校园', category: 'digital' }
				],
				goodsList: []
			}
		},
		onLoad() {
			this.loadGoods()
		},
		onPullDownRefresh() {
			this.page = 1
			this.hasMore = true
			this.loadGoods()
		},
		onReachBottom() {
			if (this.loading || !this.hasMore) return
			this.page++
			this.loadGoods()
		},
		methods: {
			onSearchClick() {
				uni.navigateTo({
					url: '/pages/search/search'
				})
			},
			switchCategory(value) {
				if (this.activeCategory === value) return
				this.activeCategory = value
				this.page = 1
				this.hasMore = true
				this.loadGoods()
			},
			loadGoods() {
				this.loading = true
				setTimeout(() => {
					let filtered = this.activeCategory === 'all'
						? this.mockGoods
						: this.mockGoods.filter(g => g.category === this.activeCategory)

					const pageSize = 8
					const start = (this.page - 1) * pageSize
					const pageData = filtered.slice(start, start + pageSize)

					if (this.page === 1) {
						this.goodsList = pageData
					} else {
						this.goodsList = this.goodsList.concat(pageData)
					}

					this.hasMore = start + pageSize < filtered.length
					this.loading = false
					uni.stopPullDownRefresh()
				}, 300)
			},
			goToDetail(goods) {
				uni.navigateTo({
					url: '/pages/goods-detail/goods-detail?id=' + goods.id
				})
			},
		}
	}
</script>

<style>
	.home-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
	}

	/* ===== 搜索栏 ===== */
	.search-bar {
		background: linear-gradient(135deg, #00613C, #00804B);
		padding: 20rpx 30rpx 30rpx;
		display: flex;
		align-items: center;
		box-sizing: border-box;
	}

	.search-icon {
		font-size: 30rpx;
		margin-right: 12rpx;
	}

	.search-placeholder {
		font-size: 28rpx;
		color: #999;
	}

	/* ===== 分类标签 ===== */
	.category-scroll {
		background: #ffffff;
		padding: 20rpx 20rpx;
		white-space: nowrap;
		box-sizing: border-box;
	}

	.category-tag {
		display: inline-block;
		font-size: 26rpx;
		color: #666;
		padding: 12rpx 28rpx;
		margin-right: 12rpx;
		border-radius: 30rpx;
		background: #f5f5f5;
		transition: all 0.2s;
		box-sizing: border-box;
	}

	.category-active {
		background: #00613C;
		color: #ffffff;
		font-weight: bold;
	}

	/* ===== 商品列表 ===== */
	.goods-grid {
		display: flex;
		flex-wrap: wrap;
		justify-content: space-between;
		padding: 16rpx 16rpx 0;
		box-sizing: border-box;
	}

	.goods-card {
		width: calc(50% - 10rpx);
		background: #ffffff;
		border-radius: 16rpx;
		overflow: hidden;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
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

	/* ===== 状态提示 ===== */
	.loading-tip {
		text-align: center;
		padding: 30rpx;
		font-size: 24rpx;
		color: #ccc;
	}

	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: 160rpx;
	}

	.empty-icon {
		font-size: 100rpx;
		margin-bottom: 20rpx;
	}

	.empty-text {
		font-size: 28rpx;
		color: #999;
	}

	.bottom-safe {
		height: 20rpx;
	}
</style>

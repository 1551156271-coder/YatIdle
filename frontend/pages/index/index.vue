<template>
	<view class="home-page">
		<!-- 顶部搜索栏 -->
		<view class="search-bar" @click="onSearchClick">
			<view class="search-box">
				<text class="search-icon iconfont icon-sousuo"></text>
				<text class="search-placeholder">搜索你想要的二手好物</text>
			</view>
			<text class="search-btn">搜索</text>
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
			<text class="empty-icon iconfont icon-weizhaodao"></text>
			<text class="empty-text">该分类暂无商品</text>
		</view>

		<!-- 底部安全距离 -->
		<view class="bottom-safe"></view>
	</view>
	<tab-bar />
</template>

<script>
	import TabBar from '@/components/tab-bar.vue'
	import { getCategories, searchItems } from '@/api/item.js'
	export default {
		components: { TabBar },
		data() {
			return {
				activeCategory: 0,
				loading: false,
				hasMore: true,
				page: 1,
				pageSize: 10,
				categories: [],
				goodsList: []
			}
		},
		onLoad() {
			this.loadCategories()
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
		onShow() {
			uni.hideTabBar()
			if(uni.getStorageSync('needRefreshHomeGoods')){
				uni.removeStorageSync('needRefreshHomeGoods')
				this.page = 1
				this.hasMore = true
				this.loadGoods()
			}
		},
		methods: {
			onSearchClick() {
				uni.navigateTo({
					url: '/pages/search/search'
				})
			},
			async loadCategories() {
				try {
					const list = await getCategories()
					this.categories = [
						{ label: '全部', value: 0 },
						...list.map(c => ({ label: c.name, value: c.id }))
					]
				} catch (e) {
					this.categories = [
						{ label: '全部', value: 0 },
						{ label: '数码电子', value: 1 },
						{ label: '书籍教材', value: 2 },
						{ label: '生活用品', value: 3 },
						{ label: '运动户外', value: 4 },
						{ label: '服饰鞋包', value: 5 },
						{ label: '其他', value: 6 }
					]
				}
			},
			switchCategory(value) {
				if (this.activeCategory === value) return
				this.activeCategory = value
				this.page = 1
				this.hasMore = true
				this.loadGoods()
			},
			async loadGoods() {
				this.loading = true
				try {
					const params = {
						page: this.page,
						size: this.pageSize
					}
					if (this.activeCategory !== 0) {
						params.categoryId = this.activeCategory
					}
					const data = await searchItems(params)
					const records = (data && data.records) ? data.records : []
					const mapped = records.map(r => ({
						id: r.id,
						image: r.imageUrl || '',
						title: r.title,
						price: r.price,
						campus: r.campus
					}))
					if (this.page === 1) {
						this.goodsList = mapped
					} else {
						this.goodsList = this.goodsList.concat(mapped)
					}
					this.hasMore = this.page < (data.pages || 1)
				} catch (e) {
					if (this.page === 1) this.goodsList = []
				} finally {
					this.loading = false
					uni.stopPullDownRefresh()
				}
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
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		padding: 20rpx 24rpx 24rpx;
		display: flex;
		align-items: center;
		gap: 16rpx;
		box-sizing: border-box;
	}

	.search-box {
		flex: 1;
		background: #ffffff;
		border-radius: 36rpx;
		height: 72rpx;
		display: flex;
		align-items: center;
		padding: 0 24rpx;
		box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.1);
		box-sizing: border-box;
	}

	.search-icon {
		font-size: 28rpx;
		margin-right: 12rpx;
		flex-shrink: 0;
	}

	.search-placeholder {
		font-size: 26rpx;
		color: #bbb;
		flex: 1;
	}

	.search-btn {
		font-size: 28rpx;
		color: #ffffff;
		font-weight: bold;
		flex-shrink: 0;
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
		background: #3A6341;
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
		font-size: 120rpx;
		margin-bottom: 20rpx;
		color: #999
	}

	.empty-text {
		font-size: 28rpx;
		color: #999;
	}

	.bottom-safe {
		height: 20rpx;
	}
</style>

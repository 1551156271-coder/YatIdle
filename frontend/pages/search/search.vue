<template>
	<view class="search-page">
		<!-- 状态栏安全区域 + 搜索栏 -->
		<view class="search-header" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="search-row" :style="{ paddingRight: headerRightPad + 'px' }">
				<view class="header-back" @click="goBack">
					<text class="back-icon">‹</text>
				</view>
				<view class="search-box">
					<text class="sb-icon iconfont icon-sousuo"></text>
					<input
						class="sb-input"
						v-model="keyword"
						:focus="true"
						placeholder="搜索你想要的二手好物"
						@confirm="onSearch"
						confirm-type="search"
					/>
					<text v-if="keyword" class="sb-clear" @click="clearSearch">✕</text>
				</view>
			</view>
		</view>

		<!-- 筛选行（仅搜索后显示） -->
		<scroll-view
			v-if="searched"
			class="filter-row"
			scroll-x
			:show-scrollbar="false"
		>
			<!-- 成色 -->
			<picker :range="conditionOptions" @change="onConditionFilter">
				<view class="filter-tag" :class="{ 'filter-active': selectedCondition }">
					{{ selectedCondition || '成色' }}
					<text class="ft-arrow">▾</text>
				</view>
			</picker>
			<!-- 校区 -->
			<picker :range="campusOptions" @change="onCampusFilter">
				<view class="filter-tag" :class="{ 'filter-active': selectedFilterCampus }">
					{{ selectedFilterCampus || '校区' }}
					<text class="ft-arrow">▾</text>
				</view>
			</picker>
			<!-- 分类 -->
			<picker :range="categoryLabels" @change="onCategoryFilter">
				<view class="filter-tag" :class="{ 'filter-active': selectedFilterCategory }">
					{{ selectedFilterCategory || '分类' }}
					<text class="ft-arrow">▾</text>
				</view>
			</picker>
			<!-- 价格排序 -->
			<view class="filter-tag" :class="{ 'filter-active': priceSort }" @click="togglePriceSort">
				<text>{{ priceSortText }}</text>
				<text class="ft-arrow">{{ priceSortIcon }}</text>
			</view>
		</scroll-view>

		<!-- 搜索结果 -->
		<view v-if="searched && results.length > 0" class="result-count">
			共找到 {{ totalCount }} 件商品
		</view>

		<view v-if="results.length > 0" class="goods-grid">
			<view
				v-for="goods in results"
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

		<!-- 空状态：已搜索无结果 -->
		<view v-if="searched && results.length === 0 && !loading" class="empty-state">
			<text class="empty-icon">🔍</text>
			<text class="empty-text">未找到相关商品</text>
			<text class="empty-hint">试试其他关键词或调整筛选条件</text>
		</view>

		<!-- 空状态：未搜索 -->
		<view v-if="!searched" class="empty-state">
			<text class="empty-icon">👆</text>
			<text class="empty-text">输入关键词搜索二手好物</text>
		</view>
	</view>
</template>

<script>
	import { searchItems, getCategories } from '@/api/item.js'
	export default {
		data() {
			return {
				keyword: '',
				searched: false,
				loading: false,
				results: [],
				totalCount: 0,
				statusBarHeight: 44,
				headerRightPad: 20,
				page: 1,
				pageSize: 10,
				hasMore: true,

				// 筛选选项
				conditionOptions: ['不限', '全新', '99新', '95新', '90新', '85新', '80新以下'],
				campusOptions: ['不限', '东校园', '南校园', '北校园', '珠海校区', '深圳校区'],
				categoryOptions: [],

				// 当前筛选值
				selectedCondition: '',
				selectedFilterCampus: '',
				selectedFilterCategory: '',
				selectedCategoryId: null,
				priceSort: ''
			}
		},
		computed: {
			categoryLabels() {
				return ['不限', ...this.categoryOptions.map(c => c.name)]
			},
			priceSortText() {
				if (this.priceSort === 'desc') return '价格从高到低'
				if (this.priceSort === 'asc') return '价格从低到高'
				return '价格'
			},
			priceSortIcon() {
				if (this.priceSort === 'desc') return '▼'
				if (this.priceSort === 'asc') return '▲'
				return '▾'
			}
		},
		onLoad() {
			try {
				const sys = uni.getSystemInfoSync()
				this.statusBarHeight = sys.statusBarHeight || 44
				const menuButton = uni.getMenuButtonBoundingClientRect()
				this.headerRightPad = sys.windowWidth - menuButton.left + 12
			} catch (e) {
				this.statusBarHeight = 44
				this.headerRightPad = 20
			}
			this.loadCategories()
		},
		methods: {
			async loadCategories() {
				try {
					this.categoryOptions = await getCategories()
				} catch (e) {
					this.categoryOptions = [
						{ id: 1, name: '数码电子' }, { id: 2, name: '书籍教材' },
						{ id: 3, name: '生活用品' }, { id: 4, name: '运动户外' },
						{ id: 5, name: '服饰鞋包' }, { id: 6, name: '其他' }
					]
				}
			},
			onSearch(e) {
				const val = (e.detail && e.detail.value) || this.keyword
				this.keyword = val
				if (val.trim()) {
					this.page = 1
					this.hasMore = true
					this.doSearch()
				}
			},
			async doSearch() {
				this.searched = true
				this.loading = true
				try {
					const params = {
						keyword: this.keyword.trim(),
						page: this.page,
						size: this.pageSize
					}
					if (this.selectedFilterCampus) params.campus = this.selectedFilterCampus
					if (this.selectedCategoryId) params.categoryId = this.selectedCategoryId
					if (this.selectedCondition) params.conditionLevel = this.selectedCondition
					if (this.priceSort) params.priceSort = this.priceSort

					const data = await searchItems(params)
					const records = (data && data.records) ? data.records : []
					this.totalCount = (data && data.total) ? data.total : records.length

					const mapped = records.map(r => ({
						id: r.id,
						image: r.imageUrl || '',
						title: r.title,
						price: r.price,
						campus: r.campus
					}))

					if (this.page === 1) {
						this.results = mapped
					} else {
						this.results = this.results.concat(mapped)
					}
					this.hasMore = this.page < (data.pages || 1)
				} catch (e) {
					if (this.page === 1) this.results = []
				} finally {
					this.loading = false
				}
				},
			clearSearch() {
				this.keyword = ''
				this.results = []
				this.searched = false
				this.selectedCondition = ''
				this.selectedFilterCampus = ''
				this.selectedFilterCategory = ''
				this.selectedCategoryId = null
				this.priceSort = ''
			},

			onConditionFilter(e) {
				const val = this.conditionOptions[e.detail.value]
				this.selectedCondition = val === '不限' ? '' : val
				if (this.searched) this.doSearch()
			},
			onCampusFilter(e) {
				const val = this.campusOptions[e.detail.value]
				this.selectedFilterCampus = val === '不限' ? '' : val
				if (this.searched) { this.page = 1; this.doSearch() }
			},
			onCategoryFilter(e) {
				const idx = e.detail.value
				if (idx === 0) {
					this.selectedFilterCategory = ''
					this.selectedCategoryId = null
				} else {
					const cat = this.categoryOptions[idx - 1]
					this.selectedFilterCategory = cat.name
					this.selectedCategoryId = cat.id
				}
				if (this.searched) { this.page = 1; this.doSearch() }
			},
			togglePriceSort() {
				if (!this.priceSort) this.priceSort = 'desc'
				else if (this.priceSort === 'desc') this.priceSort = 'asc'
				else this.priceSort = ''
				if (this.searched) this.doSearch()
			},

			goBack() {
				uni.navigateBack()
			},
			goToDetail(goods) {
				uni.navigateTo({
					url: '/pages/goods-detail/goods-detail?id=' + goods.id
				})
			}
		}
	}
</script>

<style>
	.search-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
	}

	/* ===== 搜索栏头部（含状态栏安全区） ===== */
	.search-header {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		padding-left: 20rpx;
		padding-right: 20rpx;
		padding-bottom: 16rpx;
		box-sizing: border-box;
	}

	.search-row {
		display: flex;
		align-items: center;
		height: 72rpx;
		gap: 16rpx;
	}

	/* 返回按钮（与聊天页一致） */
	.header-back {
		width: 60rpx;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

	.back-icon {
		font-size: 80rpx;
		color: #ffffff;
		font-weight: 300;
		line-height: 0.9;
	}

	.search-box {
		flex: 1;
		display: flex;
		align-items: center;
		background: #ffffff;
		border-radius: 44rpx;
		padding: 0 24rpx;
		height: 60rpx;
		box-sizing: border-box;
	}

	.sb-icon { font-size: 28rpx; margin-right: 12rpx; flex-shrink: 0; }
	.sb-input {
		flex: 1;
		height: 60rpx;
		font-size: 28rpx;
		color: #333;
		background: transparent;
	}
	.sb-clear {
		font-size: 28rpx;
		color: #999;
		padding: 8rpx;
		flex-shrink: 0;
	}

	/* ===== 筛选行 ===== */
	.filter-row {
		background: #ffffff;
		padding: 16rpx 20rpx;
		white-space: nowrap;
		box-sizing: border-box;
		border-bottom: 1rpx solid #f0f0f0;
	}

	.filter-tag {
		display: inline-flex;
		align-items: center;
		font-size: 24rpx;
		color: #666;
		padding: 10rpx 22rpx;
		margin-right: 14rpx;
		border-radius: 30rpx;
		background: #f5f5f5;
		box-sizing: border-box;
	}

	.filter-active {
		background: #e8f5ee;
		color: #3A6341;
		font-weight: bold;
	}

	.ft-arrow {
		font-size: 18rpx;
		margin-left: 6rpx;
	}

	/* ===== 搜索结果计数 ===== */
	.result-count {
		padding: 20rpx 24rpx 0;
		font-size: 24rpx;
		color: #999;
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

	/* ===== 空状态 ===== */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: 200rpx;
	}

	.empty-icon { font-size: 80rpx; margin-bottom: 20rpx; }
	.empty-text { font-size: 28rpx; color: #999; }
	.empty-hint { font-size: 24rpx; color: #ccc; margin-top: 12rpx; }
</style>

<template>
	<view class="listings-page">
		<!-- 标签切换 -->
		<view class="tab-bar">
			<view class="tab-item" :class="{ active: activeTab === 'goods' }" @click="switchTab('goods')">
				<text>发布的商品</text>
			</view>
			<view class="tab-item" :class="{ active: activeTab === 'wanted' }" @click="switchTab('wanted')">
				<text>发布的求购</text>
			</view>
		</view>

		<!-- 商品列表 -->
		<view v-if="activeTab === 'goods'">
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
		</view>

		<!-- 求购列表 -->
		<view v-if="activeTab === 'wanted'">
			<view v-if="wantedList.length > 0" class="goods-list">
				<view v-for="w in wantedList" :key="w.id" class="g-card" @click="goWantedDetail(w)">
					<view class="w-icon-wrap">
						<text class="w-icon">📋</text>
					</view>
					<view class="g-info">
						<text class="g-title">{{ w.title }}</text>
						<text class="w-budget">预算 ¥{{ w.budgetMin }} — ¥{{ w.budgetMax }}</text>
						<view class="g-bottom">
							<text class="w-time">{{ w.time }}</text>
							<text class="g-status" :class="w.status === 'active' ? 'status-on' : 'status-off'">{{ w.statusLabel }}</text>
						</view>
					</view>
				</view>
			</view>
			<view v-else-if="!loading" class="empty-wrap">
				<text class="empty-icon">📋</text>
				<text class="empty-text">暂无发布的求购</text>
				<view class="empty-btn empty-btn-wanted" @click="goPublishWanted">去发布</view>
			</view>
		</view>

		<view v-if="loading" class="loading-tip">加载中...</view>
	</view>
</template>

<script>
	import { getUserItems } from '@/api/item.js'
	import { getMyWanted } from '@/api/wanted.js'
	import { resolveImageUrl } from '@/api/index.js'

	export default {
		data() {
			return {
				activeTab: 'goods',
				goodsList: [],
				wantedList: [],
				loading: false
			}
		},
		onShow() {
			this.loadListings()
		},
		methods: {
			switchTab(tab) {
				this.activeTab = tab
			},
			async loadListings() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) return
				this.loading = true
				try {
					const [itemsResult, wantedResult] = await Promise.all([
						getUserItems(user.id).catch(() => []),
						getMyWanted(user.id).catch(() => [])
					])
					const list = itemsResult.records || itemsResult || []
					this.goodsList = list.map(item => ({
						id: item.id,
						image: resolveImageUrl(item.imageUrl || ''),
						title: item.title,
						price: item.price,
						status: item.status,
						statusLabel: this.mapItemStatus(item.status)
					}))
					this.wantedList = (wantedResult || []).map(w => ({
						id: w.id,
						title: w.title,
						budgetMin: w.budgetMin,
						budgetMax: w.budgetMax,
						status: w.status,
						statusLabel: this.mapWantedStatus(w.status),
						time: this.formatTime(w.createTime)
					}))
				} catch (e) {
					this.goodsList = []
					this.wantedList = []
				} finally {
					this.loading = false
				}
			},
			mapItemStatus(status) {
				const map = { ON_SALE: '在售', SOLD: '已售', REMOVED: '已下架' }
				return map[status] || status
			},
			mapWantedStatus(status) {
				const map = { active: '求购中', closed: '已结束' }
				return map[status] || status
			},
			formatTime(time) {
				if (!time) return ''
				const now = Date.now()
				const diff = now - new Date(time).getTime()
				const min = Math.floor(diff / 60000)
				if (min < 60) return min + '分钟前'
				const hour = Math.floor(min / 60)
				if (hour < 24) return hour + '小时前'
				return Math.floor(hour / 24) + '天前'
			},
			goDetail(g) {
				const statusMap = { ON_SALE: '在售', SOLD: '已售', REMOVED: '已下架' }
				uni.navigateTo({
					url: '/pages/goods-detail/goods-detail?id=' + g.id + '&mode=seller&status=' + (statusMap[g.status] || '在售')
				})
			},
			goWantedDetail(w) {
				uni.navigateTo({
					url: '/pages/wanted-detail/wanted-detail?id=' + w.id + '&mode=self'
				})
			},
			goPublish() {
				uni.switchTab({ url: '/pages/publish/publish' })
			},
			goPublishWanted() {
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

	/* 标签切换 */
	.tab-bar {
		display: flex;
		background: #ffffff;
		border-radius: 16rpx;
		margin-bottom: 20rpx;
		overflow: hidden;
	}
	.tab-item {
		flex: 1;
		text-align: center;
		padding: 24rpx 0;
		font-size: 28rpx;
		color: #666;
		position: relative;
	}
	.tab-item.active {
		color: #3A6341;
		font-weight: bold;
	}
	.tab-item.active::after {
		content: '';
		position: absolute;
		bottom: 0;
		left: 50%;
		transform: translateX(-50%);
		width: 60rpx;
		height: 4rpx;
		background: #3A6341;
		border-radius: 2rpx;
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

	/* 求购卡片 */
	.w-icon-wrap {
		width: 160rpx; height: 160rpx;
		border-radius: 12rpx;
		background: #e8f5ee;
		display: flex; align-items: center; justify-content: center;
		flex-shrink: 0; margin-right: 20rpx;
	}
	.w-icon { font-size: 56rpx; }
	.w-budget {
		font-size: 24rpx; color: #5A7D9E; font-weight: bold;
		margin-top: 8rpx;
	}
	.w-time { font-size: 22rpx; color: #999; }

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
	.empty-btn-wanted {
		background: linear-gradient(135deg, #5A7D9E, #7A9DBE);
	}

	.loading-tip { text-align: center; padding: 60rpx; font-size: 24rpx; color: #ccc; }
</style>

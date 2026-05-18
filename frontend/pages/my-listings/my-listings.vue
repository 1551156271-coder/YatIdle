<template>
	<view class="listings-page">
		<!-- 出售/求购 切换 -->
		<view class="tab-row">
			<view
				class="tab-item"
				:class="{ 'tab-active': currentTab === 'sell' }"
				@click="switchTab('sell')"
			>出售</view>
			<view
				class="tab-item"
				:class="{ 'tab-active': currentTab === 'buy' }"
				@click="switchTab('buy')"
			>求购</view>
		</view>

		<!-- 出售列表 -->
		<view v-if="currentTab === 'sell'">
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

		<!-- 求购列表 -->
		<view v-else>
			<view v-if="wantedList.length > 0" class="goods-list">
				<view v-for="w in wantedList" :key="w.id" class="g-card" @click="goWantedDetail(w)">
					<image class="g-img" :src="w.image" mode="aspectFill"></image>
					<view class="g-info">
						<text class="g-title">{{ w.title }}</text>
						<view class="g-bottom">
							<text class="g-price">¥{{ w.budgetMin }}-{{ w.budgetMax }}</text>
							<text class="g-status" :class="w.status === '求购中' ? 'status-on' : 'status-off'">{{ w.status }}</text>
						</view>
					</view>
				</view>
			</view>
			<view v-else class="empty-wrap">
				<text class="empty-icon">🔍</text>
				<text class="empty-text">暂无发布的求购</text>
				<view class="empty-btn" @click="goPublish">去发布</view>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				currentTab: 'sell',
				goodsList: [
					{ id: 1, image: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop', title: '九成新公路自行车', price: '268.00', status: '在售' },
					{ id: 2, image: 'https://images.unsplash.com/photo-1585435557343-3b092031a831?q=80&w=400&auto=format&fit=crop', title: 'LED护眼台灯', price: '35.00', status: '在售' },
					{ id: 3, image: 'https://images.unsplash.com/photo-1544816155-12df9643f363?q=80&w=400&auto=format&fit=crop', title: '四六级真题全套', price: '12.00', status: '已售' },
					{ id: 4, image: 'https://images.unsplash.com/photo-1495446815901-a7297e633e8d?q=80&w=400&auto=format&fit=crop', title: '二手教材《高等数学》', price: '8.00', status: '在售' },
					{ id: 5, image: 'https://images.unsplash.com/photo-1586953208448-b95a79798f07?q=80&w=400&auto=format&fit=crop', title: '蓝牙耳机', price: '45.00', status: '已下架' }
				],
				wantedList: [
					{ id: 101, image: 'https://images.unsplash.com/photo-1517336714731-489689fd1ca8?q=80&w=400&auto=format&fit=crop', title: 'MacBook Pro 14寸', budgetMin: '5000', budgetMax: '7000', status: '求购中' },
					{ id: 102, image: 'https://images.unsplash.com/photo-1546868871-af0de0ae72be?q=80&w=400&auto=format&fit=crop', title: 'iPad + 苹果笔', budgetMin: '2000', budgetMax: '3500', status: '求购中' },
					{ id: 103, image: 'https://images.unsplash.com/photo-1583394838782-c0b8bf19d35f?q=80&w=400&auto=format&fit=crop', title: '数位板', budgetMin: '200', budgetMax: '500', status: '已找到' }
				]
			}
		},
		methods: {
			switchTab(tab) {
				this.currentTab = tab
			},
			goDetail(g) {
				uni.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + g.id + '&mode=seller&status=' + g.status })
			},
			goWantedDetail(w) {
				uni.navigateTo({ url: '/pages/wanted-detail/wanted-detail?id=' + w.id + '&mode=self&status=' + w.status })
			},
			goPublish() {
				if (this.currentTab === 'buy') {
					uni.navigateTo({ url: '/pages/publish-form/publish-form?type=buy' })
				} else {
					uni.switchTab({ url: '/pages/publish/publish' })
				}
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

	/* 切换标签 */
	.tab-row {
		display: flex;
		background: #ffffff;
		border-radius: 12rpx;
		padding: 8rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.03);
	}
	.tab-item {
		flex: 1;
		text-align: center;
		font-size: 28rpx;
		color: #666;
		padding: 16rpx 0;
		border-radius: 10rpx;
		transition: all 0.2s;
	}
	.tab-active {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		color: #ffffff;
		font-weight: bold;
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
</style>

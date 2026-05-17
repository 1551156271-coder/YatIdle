<template>
	<view class="pr-page">
		<!-- 顶部导航 -->
		<view class="pr-header" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="header-back" @click="goBack">
				<text class="back-icon">‹</text>
			</view>
			<text class="header-title">{{ user.nickname }}的评价</text>
			<view class="header-placeholder"></view>
		</view>

		<!-- 评分概览 -->
		<view class="rating-summary">
			<view class="rating-score">
				<text class="score-num">{{ avgRating }}</text>
				<text class="score-unit">分</text>
			</view>
			<view class="rating-stars">
				<text v-for="s in 5" :key="s" class="big-star" :class="{ 'star-on': s <= avgRating }">★</text>
			</view>
			<text class="rating-count">共 {{ reviews.length }} 条评价</text>
		</view>

		<!-- 评价列表 -->
		<view v-if="reviews.length > 0" class="review-list">
			<view v-for="r in reviews" :key="r.id" class="review-item">
				<view class="rv-top">
					<view class="rv-user">
						<text class="rv-avatar">{{ r.avatar }}</text>
						<text class="rv-name">{{ r.name }}</text>
					</view>
					<view class="rv-stars">
						<text v-for="s in 5" :key="s" class="star" :class="{ 'star-on': s <= r.rating }">★</text>
					</view>
					<text class="rv-time">{{ r.time }}</text>
				</view>
				<text v-if="r.content" class="rv-content">{{ r.content }}</text>
				<view v-if="r.tags && r.tags.length > 0" class="rv-tags">
					<text v-for="t in r.tags" :key="t" class="rv-tag">{{ t }}</text>
				</view>
			</view>
		</view>

		<view v-else class="empty-state">
			<text class="empty-icon">💬</text>
			<text class="empty-text">暂无评价</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				statusBarHeight: 44,
				user: { nickname: '用户' },
				reviews: []
			}
		},
		computed: {
			avgRating() {
				if (this.reviews.length === 0) return 0
				const sum = this.reviews.reduce((s, r) => s + r.rating, 0)
				return (sum / this.reviews.length).toFixed(1)
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
				this.loadReviews(options.id)
			}
		},
		methods: {
			loadReviews(userId) {
				// TODO: 从API获取，此处模拟数据
				const mockData = {
					'2': {
						nickname: '张三（卖家）',
						reviews: [
							{ id: 1, avatar: '📚', name: '李四', rating: 5, content: '卖家很爽快，车况和描述完全一致，还送了挡泥板！', tags: ['响应迅速', '诚实描述'], time: '3天前' },
							{ id: 2, avatar: '🎧', name: '王五', rating: 5, content: '交易顺利，当面验货很放心', tags: ['诚信交易'], time: '1周前' },
							{ id: 3, avatar: '📱', name: '赵六', rating: 4, content: '台灯好用，就是有点小划痕，整体不错', tags: ['物有所值'], time: '2周前' },
							{ id: 4, avatar: '💻', name: '孙七', rating: 5, content: '很nice的卖家，沟通顺畅，还帮我送到楼下', tags: ['服务好', '送货快'], time: '3周前' },
							{ id: 5, avatar: '🎮', name: '周八', rating: 5, content: '第二次交易了，一如既往靠谱', tags: ['回头客'], time: '1月前' },
							{ id: 6, avatar: '📖', name: '吴九', rating: 4, content: '物品不错，价格合理，就是约的时间改了一次', tags: ['物有所值'], time: '1月前' }
						]
					}
				}
				const data = mockData[userId]
				if (data) {
					this.user.nickname = data.nickname
					this.reviews = data.reviews
				}
			},
			goBack() {
				uni.navigateBack()
			}
		}
	}
</script>

<style>
	.pr-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
	}

	/* ===== 顶部导航 ===== */
	.pr-header {
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

	/* ===== 评分概览 ===== */
	.rating-summary {
		background: #ffffff;
		margin: 20rpx;
		border-radius: 20rpx;
		padding: 40rpx 30rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		box-sizing: border-box;
	}

	.rating-score {
		display: flex;
		align-items: baseline;
		margin-bottom: 12rpx;
	}

	.score-num {
		font-size: 80rpx;
		color: #3A6341;
		font-weight: bold;
		line-height: 1;
	}

	.score-unit {
		font-size: 28rpx;
		color: #999;
		margin-left: 6rpx;
	}

	.rating-stars {
		display: flex;
		gap: 6rpx;
		margin-bottom: 12rpx;
	}

	.big-star {
		font-size: 40rpx;
		color: #eee;
	}

	.big-star.star-on {
		color: #f5a623;
	}

	.rating-count {
		font-size: 24rpx;
		color: #999;
	}

	/* ===== 评价列表 ===== */
	.review-list {
		margin: 0 20rpx;
	}

	.review-item {
		background: #ffffff;
		border-radius: 16rpx;
		padding: 24rpx;
		margin-bottom: 16rpx;
		box-sizing: border-box;
	}

	.rv-top {
		display: flex;
		align-items: center;
		gap: 12rpx;
		margin-bottom: 14rpx;
	}

	.rv-user {
		display: flex;
		align-items: center;
		gap: 10rpx;
	}

	.rv-avatar {
		width: 48rpx;
		height: 48rpx;
		background: #f0f0f0;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		font-size: 24rpx;
		flex-shrink: 0;
	}

	.rv-name {
		font-size: 26rpx;
		color: #333;
		font-weight: bold;
	}

	.rv-stars {
		display: flex;
		gap: 2rpx;
		flex: 1;
	}

	.star {
		font-size: 24rpx;
		color: #eee;
	}

	.star-on {
		color: #f5a623;
	}

	.rv-time {
		font-size: 22rpx;
		color: #ccc;
	}

	.rv-content {
		font-size: 26rpx;
		color: #666;
		line-height: 1.6;
		display: block;
		margin-bottom: 14rpx;
	}

	.rv-tags {
		display: flex;
		flex-wrap: wrap;
		gap: 10rpx;
	}

	.rv-tag {
		font-size: 22rpx;
		color: #3A6341;
		background: #e8f5ee;
		padding: 6rpx 16rpx;
		border-radius: 6rpx;
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
</style>

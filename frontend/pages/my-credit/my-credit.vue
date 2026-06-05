<template>
	<view class="credit-page">
		<!-- 信用分大卡 -->
		<view class="score-card">
			<view class="score-circle" :class="creditLevel">
				<text class="score-num">{{ credit.creditScore }}</text>
				<!--<text class="score-unit">分</text>-->
			</view>
			<text class="score-desc" :class="creditLevel">{{ creditDesc }}</text>
		</view>

		<!-- 数据统计 -->
		<view class="section-card">
			<view class="stats-row">
				<view class="stat-item">
					<text class="stat-num">{{ credit.dealCount }}</text>
					<text class="stat-label">成交</text>
				</view>
				<view class="stat-item">
					<text class="stat-num">{{ credit.goodsCount }}</text>
					<text class="stat-label">在售</text>
				</view>
				<view class="stat-item">
					<text class="stat-num">{{ credit.reviewCount }}</text>
					<text class="stat-label">评价</text>
				</view>
			</view>
		</view>

		<!-- 收到的评价 -->
		<view class="section-card">
			<view class="section-head">
				<text class="section-title">收到的评价（{{ reviews.length }}条）</text>
			</view>
			<view v-if="loading" class="empty-inline">
				<text class="empty-txt">加载中...</text>
			</view>
			<view v-else-if="reviews.length > 0" class="review-list">
				<view v-for="r in reviews" :key="r.id" class="review-item">
					<view class="rv-top">
						<view class="rv-user">
							<view class="rv-avatar">
							<image v-if="r.avatar" class="rv-avatar-img" :src="r.avatar" mode="aspectFill"></image>
							<text v-else class="rv-avatar-emoji">{{ r.defaultAvatar }}</text>
						</view>
							<text class="rv-name">{{ r.name }}</text>
						</view>
						<view class="rv-stars">
							<text v-for="s in 5" :key="s" class="star" :class="{ 'star-on': s <= r.rating }">★</text>
						</view>
						<text class="rv-time">{{ r.time }}</text>
					</view>
					<text class="rv-content" v-if="r.content">{{ r.content }}</text>
				</view>
			</view>
			<view v-else class="empty-inline">
				<text class="empty-txt">暂无评价</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { getUserReviews } from '@/api/review.js'
	import { getUserInfo } from '@/api/user.js'

	export default {
		data() {
			return {
				credit: {
					creditScore: 0,
					dealCount: 0,
					goodsCount: 0,
					reviewCount: 0
				},
				reviews: [],
				loading: true
			}
		},
		computed: {
			creditLevel() {
				const s = this.credit.creditScore
				if (s >= 90) return 'credit-high'
				if (s >= 70) return 'credit-mid'
				return 'credit-low'
			},
			creditDesc() {
				const s = this.credit.creditScore
				if (s >= 90) return '信用极好'
				if (s >= 70) return '信用良好'
				return '信用较差'
			}
		},
		onLoad() {
			this.loadData()
		},
		methods: {
			async loadData() {
				const user = uni.getStorageSync('user')
				const userId = user ? user.id : 1
				this.loading = true
				try {
					const [userData, reviewData] = await Promise.all([
						getUserInfo(userId).catch(() => null),
						getUserReviews(userId).catch(() => null)
					])

					if (userData) {
						this.credit.creditScore = userData.creditScore || 0
						this.credit.dealCount = userData.dealCount || 0
						this.credit.goodsCount = userData.goodsCount || 0
						this.credit.reviewCount = userData.reviewCount || 0
					}

					if (reviewData) {
						const reviewList = Array.isArray(reviewData) ? reviewData : (reviewData.reviews || [])
						this.reviews = reviewList.map(rv => ({
							id: rv.id,
							avatar: rv.reviewerAvatar || '',
							defaultAvatar: rv.reviewerName ? rv.reviewerName.charAt(0) : '?',
							name: rv.reviewerName || '匿名用户',
							rating: rv.rating || 5,
							content: rv.content || '',
							time: this.formatTime(rv.createTime)
						}))
					}
				} catch (e) {
					// ignore
				} finally {
					this.loading = false
				}
			},
			formatTime(dateStr) {
				if (!dateStr) return ''
				const now = Date.now()
				const t = new Date(dateStr).getTime()
				const diff = now - t
				const day = 86400000
				if (diff < day) return '今天'
				if (diff < 2 * day) return '昨天'
				if (diff < 7 * day) return Math.floor(diff / day) + '天前'
				if (diff < 30 * day) return Math.floor(diff / (7 * day)) + '周前'
				if (diff < 365 * day) return Math.floor(diff / (30 * day)) + '个月前'
				return Math.floor(diff / (365 * day)) + '年前'
			}
		}
	}
</script>

<style>
	.credit-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding: 20rpx;
		box-sizing: border-box;
	}

	/* 信用分大卡 */
	.score-card {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		border-radius: 20rpx;
		padding: 50rpx 30rpx;
		display: flex; flex-direction: column; align-items: center;
		margin-bottom: 20rpx;
	}
	.score-circle {
		width: 180rpx; height: 180rpx;
		border-radius: 50%;
		background: rgba(255,255,255,0.15);
		border: 8rpx solid rgba(255,255,255,0.3);
		display: flex; flex-direction: column; align-items: center; justify-content: center;
		margin-bottom: 20rpx;
	}
	.score-num { font-size: 72rpx; color: #ffffff; font-weight: bold; line-height: 1; }
	/*.score-unit { font-size: 24rpx; color: rgba(255,255,255,0.6); margin-top: 4rpx; }*/
	.score-desc { font-size: 30rpx; color: #ffffff; font-weight: bold; }

	.score-circle.credit-high { border-color: #4cd964; }
	.score-desc.credit-high { color: #4cd964; }
	.score-circle.credit-mid { border-color: #f0ad4e; }
	.score-desc.credit-mid { color: #f0ad4e; }
	.score-circle.credit-low { border-color: #e74c3c; }
	.score-desc.credit-low { color: #e74c3c; }

	/* 统计卡片 */
	.section-card {
		background: #ffffff; margin-bottom: 20rpx; border-radius: 20rpx;
		padding: 24rpx;
		box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
	}
	.stats-row { display: flex; }
	.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
	.stat-num { font-size: 36rpx; color: #333; font-weight: bold; margin-bottom: 6rpx; }
	.stat-label { font-size: 24rpx; color: #999; }

	/* 评价 */
	.section-head { margin-bottom: 10rpx; }
	.section-title { font-size: 30rpx; color: #333; font-weight: bold; }

	.review-item { padding: 20rpx 0; border-bottom: 1rpx solid #f5f5f5; }
	.review-item:last-child { border-bottom: none; }
	.rv-top { display: flex; align-items: center; gap: 12rpx; margin-bottom: 10rpx; }
	.rv-user { display: flex; align-items: center; gap: 8rpx; }
	.rv-avatar {
	width: 48rpx; height: 48rpx; background: #f0f0f0; border-radius: 50%;
	display: flex; align-items: center; justify-content: center; font-size: 24rpx; flex-shrink: 0; overflow: hidden;
}
.rv-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
.rv-avatar-emoji { font-size: 24rpx; }
	.rv-name { font-size: 26rpx; color: #333; }
	.rv-stars { display: flex; gap: 2rpx; }
	.star { font-size: 26rpx; color: #eee; }
	.star-on { color: #f5a623; }
	.rv-time { font-size: 22rpx; color: #ccc; margin-left: auto; }
	.rv-content { font-size: 26rpx; color: #666; line-height: 1.6; }

	/* 空态 */
	.empty-inline { padding: 30rpx 0; text-align: center; }
	.empty-txt { font-size: 24rpx; color: #ccc; }
</style>

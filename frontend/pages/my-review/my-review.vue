<template>
	<view class="review-page">
		<!-- 卖家信息 -->
		<view class="seller-card">
			<view class="seller-avatar">
				<image v-if="sellerAvatar" class="seller-avatar-img" :src="sellerAvatar" mode="aspectFill"></image>
				<text v-else class="seller-avatar-text">{{ sellerName.charAt(0) }}</text>
			</view>
			<view class="seller-info">
				<text class="seller-name">{{ sellerName }}</text>
				<text class="seller-hint">交易已完成，给TA一个评价吧</text>
			</view>
		</view>

		<!-- 打分 -->
		<view class="section-card">
			<text class="section-title">评分</text>
			<view class="star-row">
				<text
					v-for="i in 5"
					:key="i"
					class="star-item"
					:class="i <= rating ? 'star-active' : ''"
					@click="setRating(i)"
				>{{ i <= rating ? '★' : '☆' }}</text>
			</view>
			<text class="rating-text">{{ ratingText }}</text>
		</view>

		<!-- 评价内容（选填） -->
		<view class="section-card">
			<text class="section-title">评价<text class="optional-hint">（选填）</text></text>
			<textarea
				class="review-textarea"
				v-model="reviewText"
				placeholder="说说你的交易体验吧~"
				:maxlength="300"
			/>
			<text class="char-count">{{ reviewText.length }}/300</text>
		</view>

		<!-- 提交 -->
		<button class="submit-btn" @click="onSubmit">提交评价</button>
	</view>
</template>

<script>
	import { submitReview, updateReview } from '@/api/review.js'

	export default {
		data() {
			return {
				sellerName: '',
				sellerAvatar: '',
				sellerId: '',
				orderId: '',
				reviewId: '',
				rating: 0,
				reviewText: ''
			}
		},
		computed: {
			ratingText() {
				const map = { 0: '点击星星评分', 1: '非常差', 2: '较差', 3: '一般', 4: '满意', 5: '非常满意' }
				return map[this.rating] || ''
			}
		},
		onLoad(options) {
			this.sellerName = options.seller || '卖家'
			this.sellerAvatar = options.sellerAvatar || ''
			this.sellerId = options.sellerId || ''
			this.orderId = options.orderId || ''
			this.reviewId = options.reviewId || ''
		},
		methods: {
			setRating(i) {
				this.rating = i
			},
			async onSubmit() {
				if (this.rating === 0) {
					uni.showToast({ title: '请先给卖家评分', icon: 'none' })
					return
				}
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return
				}
				const data = {
					orderId: parseInt(this.orderId),
					revieweeId: parseInt(this.sellerId),
					rating: this.rating,
					content: this.reviewText || ''
				}
				try {
					if (this.reviewId) {
						await updateReview(this.reviewId, user.id, data)
					} else {
						await submitReview(user.id, data)
					}
					uni.showToast({ title: '评价成功！', icon: 'success' })
					setTimeout(() => { uni.navigateBack() }, 1200)
				} catch (e) {
					// error handled by api layer
				}
			}
		}
	}
</script>

<style>
	.review-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding: 20rpx;
		box-sizing: border-box;
	}

	/* 卖家卡片 */
	.seller-card {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 30rpx;
		display: flex;
		align-items: center;
		gap: 20rpx;
		margin-bottom: 20rpx;
	}
	.seller-avatar {
		width: 88rpx; height: 88rpx;
		border-radius: 50%;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		display: flex; align-items: center; justify-content: center;
		flex-shrink: 0; overflow: hidden;
	}
t	.seller-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
	.seller-avatar-text {
		font-size: 40rpx; color: #ffffff; font-weight: bold;
	}
	.seller-info { flex: 1; }
	.seller-name { font-size: 30rpx; color: #333; font-weight: bold; display: block; }
	.seller-hint { font-size: 24rpx; color: #999; margin-top: 6rpx; display: block; }

	/* 分区卡片 */
	.section-card {
		background: #ffffff;
		border-radius: 20rpx;
		padding: 30rpx;
		margin-bottom: 20rpx;
	}
	.section-title {
		font-size: 28rpx; color: #333; font-weight: bold;
		margin-bottom: 24rpx; display: block;
	}
	.optional-hint { font-size: 24rpx; color: #999; font-weight: normal; }

	/* 星星 */
	.star-row {
		display: flex; justify-content: center; gap: 20rpx;
	}
	.star-item {
		font-size: 64rpx;
		color: #ddd;
		transition: all 0.15s;
	}
	.star-active {
		color: #f0ad4e;
	}
	.rating-text {
		display: block; text-align: center;
		font-size: 26rpx; color: #f0ad4e; margin-top: 16rpx;
	}

	/* 评价输入 */
	.review-textarea {
		width: 100%;
		height: 200rpx;
		background: #f5f5f5;
		border-radius: 12rpx;
		padding: 20rpx;
		font-size: 28rpx;
		box-sizing: border-box;
	}
	.char-count {
		text-align: right; font-size: 22rpx; color: #ccc;
		margin-top: 10rpx; display: block;
	}

	/* 提交按钮 */
	.submit-btn {
		width: 100%; height: 88rpx; line-height: 88rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		color: #ffffff; font-size: 32rpx; font-weight: bold;
		border-radius: 44rpx; border: none;
		box-shadow: 0 8rpx 24rpx rgba(0,97,60,0.3);
		box-sizing: border-box;
	}
</style>

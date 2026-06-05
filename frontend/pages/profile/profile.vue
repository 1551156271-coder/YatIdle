<template>
	<view class="profile-page">
		<!-- 头部个人信息 -->
		<view class="header-card">
			<view class="header-bg">
				<view class="more-btn" @click="showMoreActions">
					<text class="more-icon">⋯</text>
				</view>
			</view>
			<view class="header-content">
				<view class="avatar-wrap">
					<image v-if="user.avatar" class="avatar-img" :src="user.avatar" mode="aspectFill"></image>
					<text v-else class="avatar-emoji">{{ user.defaultAvatar }}</text>
				</view>
				<text class="user-name">{{ user.nickname }}</text>
				<text class="user-bio" v-if="user.bio">{{ user.bio }}</text>
				<view class="user-tags">
					<text class="u-tag">{{ user.campus }}</text>
					<text v-if="user.verified" class="u-tag u-tag-verified">已认证</text>
				</view>
			</view>
		</view>

		<!-- 信用分 + 数据 -->
		<view class="credit-card">
			<view class="credit-main">
				<text class="credit-label">信用分</text>
				<text class="credit-score" :class="creditLevel">{{ user.creditScore }}</text>
				<text class="credit-desc">{{ creditDesc }}</text>
			</view>
			<view class="credit-stats">
				<view class="cs-item">
					<text class="cs-num">{{ user.dealCount }}</text>
					<text class="cs-label">成交</text>
				</view>
				<view class="cs-item">
					<text class="cs-num">{{ user.goodsCount }}</text>
					<text class="cs-label">在售</text>
				</view>
			</view>
		</view>

		<!-- 在售商品 -->
		<view class="section">
			<view class="section-head">
				<text class="section-title">在售商品</text>
				<text class="section-more" @click="viewAllGoods">全部 ›</text>
			</view>
			<view v-if="user.goods.length > 0" class="goods-list">
				<view v-for="g in user.goods" :key="g.id" class="g-card" @click="goGoodsDetail(g)">
					<image class="g-img" :src="g.image" mode="aspectFill"></image>
					<view class="g-info">
						<text class="g-title">{{ g.title }}</text>
						<text class="g-price">¥{{ g.price }}</text>
					</view>
				</view>
			</view>
			<view v-else class="empty-inline">
				<text class="empty-txt">暂无在售商品</text>
			</view>
		</view>

		<!-- 评价 -->
		<view class="section">
			<view class="section-head">
				<text class="section-title">评价</text>
				<text class="section-more" @click="viewAllReviews">全部 {{ user.reviews.length }} ›</text>
			</view>
			<view v-if="user.reviews.length > 0" class="review-list">
				<view v-for="r in user.reviews" :key="r.id" class="review-item">
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
	import { getUserInfo } from '@/api/user.js'
	import { getUserReviews } from '@/api/review.js'
	import { getUserItems } from '@/api/item.js'
	import { resolveImageUrl } from '@/api/index.js'

	export default {
		data() {
			return {
				userId: '',
				user: {
					nickname: '中大在校生',
					defaultAvatar: '🎓',
					avatar: '',
					bio: '爱生活爱二手，诚信交易',
					campus: '东校园',
					verified: false,
					creditScore: 0,
					dealCount: 0,
					goodsCount: 0,
					goods: [],
					reviews: []
				},
				loading: true
			}
		},
		computed: {
			creditLevel() {
				const s = this.user.creditScore
				if (s >= 90) return 'credit-high'
				if (s >= 70) return 'credit-mid'
				return 'credit-low'
			},
			creditDesc() {
				const s = this.user.creditScore
				if (s >= 90) return '信用极好'
				if (s >= 70) return '信用良好'
				return '信用一般'
			}
		},
		onLoad(options) {
			if (options.id) {
				this.userId = options.id
				this.loadUser(options.id)
			}
		},
		methods: {
			async loadUser(id) {
				this.loading = true
				try {
					const [userData, reviewData, itemsData] = await Promise.all([
						getUserInfo(id).catch(() => null),
						getUserReviews(id).catch(() => null),
						getUserItems(id, { status: 'ON_SALE' }).catch(() => null)
					])

					if (userData) {
						const u = userData.data || userData
						this.user.nickname = u.nickname || u.username || '用户'
						this.user.defaultAvatar = u.nickname ? u.nickname.charAt(0) : '🎓'
						this.user.avatar = resolveImageUrl(u.avatar || '')
						this.user.bio = u.bio || ''
						this.user.campus = u.campus || ''
						this.user.creditScore = u.creditScore || 0
						this.user.dealCount = u.dealCount || 0
						this.user.goodsCount = u.goodsCount || 0
					}

					if (reviewData) {
						const r = reviewData.data || reviewData
						const reviews = r.reviews || r || []
						this.user.reviews = (reviews || []).slice(0, 3).map(rv => ({
							id: rv.id,
							avatar: rv.reviewerAvatar || '',
							defaultAvatar: rv.reviewerName ? rv.reviewerName.charAt(0) : '?',
							name: rv.reviewerName || '匿名用户',
							rating: rv.rating || 5,
							content: rv.content || '',
							time: this.formatTime(rv.createTime)
						}))
					}

					if (itemsData) {
						const list = itemsData.records || itemsData || []
						this.user.goods = list.map(g => ({
							id: g.id,
							image: g.imageUrl || g.coverImage || '',
							title: g.title || '',
							price: g.price || '0.00'
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
			},
			goGoodsDetail(g) {
				uni.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + g.id })
			},
			viewAllGoods() {
				uni.navigateTo({ url: '/pages/profile-goods/profile-goods?id=' + (this.userId || '2') })
			},
			viewAllReviews() {
				uni.navigateTo({ url: '/pages/profile-reviews/profile-reviews?id=' + (this.userId || '2') })
			},
			showMoreActions() {
				uni.showActionSheet({
					itemList: ['举报'],
					success: (res) => {
						if (res.tapIndex === 0) {
							uni.navigateTo({
								url: '/pages/report/report?targetUserId=' + (this.userId || '')
							})
						}
					}
				})
			}
		}
	}
</script>

<style>
	.profile-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
	}

	/* ===== 头部 ===== */
	.header-card { position: relative; }

	.header-bg {
		height: 200rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		position: relative;
	}

	.more-btn {
		position: absolute;
		top: 16rpx;
		right: 20rpx;
		width: 60rpx;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
		z-index: 10;
	}

	.more-icon {
		font-size: 48rpx;
		color: #ffffff;
		font-weight: bold;
		line-height: 1;
	}

	.header-content {
		background: #ffffff;
		margin: -60rpx 20rpx 0;
		border-radius: 20rpx;
		padding: 0 30rpx 36rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
		position: relative;
	}
	.avatar-wrap {
		width: 140rpx; height: 140rpx;
		border-radius: 50%;
		background: #ffffff;
		margin-top: -70rpx;
		display: flex; align-items: center; justify-content: center;
		border: 6rpx solid #ffffff;
		box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.08);
		overflow: hidden;
	}
	.avatar-img { width: 100%; height: 100%; border-radius: 50%; }
	.avatar-emoji { font-size: 60rpx; }
	.user-name { font-size: 36rpx; color: #333; font-weight: bold; margin-top: 20rpx; }
	.user-bio {
		font-size: 26rpx; color: #999; margin-top: 10rpx; text-align: center;
	}
	.user-tags { display: flex; gap: 12rpx; margin-top: 16rpx; }
	.u-tag {
		font-size: 22rpx; color: #3A6341; background: #e8f5ee;
		padding: 6rpx 20rpx; border-radius: 20rpx;
	}
	.u-tag-verified { color: #1565C0; background: #e3f2fd; }

	/* ===== 信用卡片 ===== */
	.credit-card {
		background: #ffffff;
		margin: 20rpx; border-radius: 20rpx; padding: 30rpx;
		display: flex; align-items: center;
		box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
	}
	.credit-main {
		display: flex; flex-direction: column; align-items: center;
		padding-right: 30rpx; border-right: 1rpx solid #f0f0f0; margin-right: 30rpx;
	}
	.credit-label { font-size: 24rpx; color: #999; margin-bottom: 8rpx; }
	.credit-score { font-size: 64rpx; font-weight: bold; line-height: 1; margin-bottom: 6rpx; }
	.credit-high { color: #4cd964; }
	.credit-mid { color: #f0ad4e; }
	.credit-low { color: #e74c3c; }
	.credit-desc { font-size: 22rpx; color: #999; }

	.credit-stats { flex: 1; display: flex; justify-content: space-around; }
	.cs-item { display: flex; flex-direction: column; align-items: center; }
	.cs-num { font-size: 36rpx; color: #333; font-weight: bold; }
	.cs-label { font-size: 22rpx; color: #999; margin-top: 4rpx; }

	/* ===== 通用 section ===== */
	.section {
		background: #ffffff; margin: 0 20rpx 20rpx; border-radius: 20rpx;
		padding: 24rpx 24rpx 10rpx;
	}
	.section-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; }
	.section-title { font-size: 30rpx; color: #333; font-weight: bold; }
	.section-more { font-size: 24rpx; color: #999; }

	/* ===== 商品列表 ===== */
	.goods-list { display: flex; gap: 16rpx; overflow-x: auto; padding-bottom: 16rpx; }
	.g-card { width: 220rpx; flex-shrink: 0; background: #f9f9f9; border-radius: 12rpx; overflow: hidden; }
	.g-img { width: 100%; height: 200rpx; background: #eee; }
	.g-info { padding: 14rpx 16rpx 16rpx; }
	.g-title {
		font-size: 24rpx; color: #333; line-height: 1.4;
		display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2;
		overflow: hidden; margin-bottom: 10rpx;
	}
	.g-price { font-size: 28rpx; color: #e74c3c; font-weight: bold; }

	/* ===== 评价 ===== */
	.review-list { padding-bottom: 10rpx; }
	.review-item { padding: 20rpx 0; border-bottom: 1rpx solid #f5f5f5; }
	.review-item:last-child { border-bottom: none; }
	.rv-top { display: flex; align-items: center; gap: 12rpx; margin-bottom: 10rpx; }
	.rv-user { display: flex; align-items: center; gap: 8rpx; }
	.rv-avatar {
	width: 48rpx; height: 48rpx;
	background: #f0f0f0;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 24rpx;
	flex-shrink: 0; overflow: hidden;
}
.rv-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
.rv-avatar-emoji { font-size: 24rpx; }
	.rv-name { font-size: 26rpx; color: #333; }
	.rv-stars { display: flex; gap: 2rpx; }
	.star { font-size: 26rpx; color: #eee; }
	.star-on { color: #f5a623; }
	.rv-time { font-size: 22rpx; color: #ccc; margin-left: auto; }
	.rv-content { font-size: 26rpx; color: #666; line-height: 1.6; }

	/* ===== 空态 ===== */
	.empty-inline { padding: 30rpx 0; text-align: center; }
	.empty-txt { font-size: 24rpx; color: #ccc; }

</style>

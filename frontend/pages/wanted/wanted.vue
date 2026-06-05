<template>
	<view class="wanted-page">
		<view class="wanted-hero">
			<text class="hero-icon">📋</text>
			<text class="hero-title">校园求购</text>
			<text class="hero-sub">看看同学们都在求购什么</text>
		</view>

		<view class="wanted-list">
			<view
				v-for="item in wantedList"
				:key="item.id"
				class="wanted-card"
				@click="goToDetail(item)"
			>
				<view class="wanted-top">
					<text class="wanted-emoji">📋</text>
					<text class="wanted-title">{{ item.title }}</text>
				</view>
				<view class="wanted-meta">
					<text class="wanted-budget">预算 ¥{{ item.budgetMin }} — ¥{{ item.budgetMax }}</text>
					<text class="wanted-campus">{{ item.campus }}</text>
				</view>
				<view class="wanted-tags">
					<text class="wanted-tag">{{ item.condition }}</text>
					<text class="wanted-tag">{{ item.categoryLabel }}</text>
				</view>
				<text class="wanted-desc">{{ item.desc }}</text>
				<view class="wanted-footer">
					<view class="wanted-user">
						<image v-if="item.avatar" class="wanted-avatar" :src="item.avatar" mode="aspectFill"></image>
						<text v-else class="wanted-avatar-def">{{ (item.nickname || item.username || '?').charAt(0) }}</text>
						<text class="wanted-name">{{ item.nickname || item.username }}</text>
					</view>
					<text class="wanted-time">{{ item.time }}</text>
				</view>
			</view>
		</view>

		<view v-if="wantedList.length === 0" class="empty-state">
			<text class="empty-icon">📋</text>
			<text class="empty-text">暂无求购信息</text>
		</view>
	</view>
	<tab-bar />
</template>

<script>
	import TabBar from '@/components/tab-bar.vue'
	import { getWantedList } from '@/api/wanted.js'
	import { getCategories } from '@/api/item.js'

	export default {
		components: { TabBar },
		data() {
			return {
				wantedList: [],
				categories: []
			}
		},
		onLoad() {
			this.loadCategories()
		},
		onShow() {
			uni.hideTabBar()
			this.loadWantedList()
		},
		methods: {
			async loadCategories() {
				try {
					this.categories = await getCategories()
				} catch (e) {
					this.categories = []
				}
			},
			async loadWantedList() {
				try {
					const list = await getWantedList()
					this.wantedList = list.map(item => ({
						id: item.id,
						title: item.title,
						budgetMin: item.budgetMin,
						budgetMax: item.budgetMax,
						campus: item.campus || '',
						condition: item.conditionLevel || '不限',
						categoryLabel: this.getCategoryLabel(item.categoryId),
						desc: '',
						nickname: item.nickname || '',
						username: item.username || '',
						avatar: item.avatar || '',
						time: this.formatTime(item.createTime)
					}))
				} catch (e) {
					console.error('加载求购列表失败', e)
				}
			},
			getCategoryLabel(categoryId) {
				if (!this.categories.length) return ''
				const cat = this.categories.find(c => c.id === categoryId)
				return cat ? cat.name : ''
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
			goToDetail(item) {
				uni.navigateTo({
					url: '/pages/wanted-detail/wanted-detail?id=' + item.id
				})
			}
		}
	}
</script>

<style>
	.wanted-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
	}

	.wanted-hero {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		padding: 40rpx 30rpx 50rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.hero-icon { font-size: 64rpx; margin-bottom: 16rpx; }
	.hero-title { font-size: 36rpx; color: #ffffff; font-weight: bold; margin-bottom: 8rpx; }
	.hero-sub { font-size: 24rpx; color: rgba(255,255,255,0.8); }

	/* ===== 求购列表 ===== */
	.wanted-list {
		padding: 20rpx 20rpx 0;
		box-sizing: border-box;
	}

	.wanted-card {
		width: 100%;
		background: #ffffff;
		border-radius: 16rpx;
		padding: 24rpx 24rpx 20rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
		box-sizing: border-box;
		border-left: 8rpx solid #598f62;
	}

	.wanted-card:active {
		opacity: 0.9;
	}

	.wanted-top {
		display: flex;
		align-items: center;
		margin-bottom: 12rpx;
	}

	.wanted-emoji {
		font-size: 32rpx;
		margin-right: 12rpx;
	}

	.wanted-title {
		font-size: 30rpx;
		color: #333;
		font-weight: bold;
		flex: 1;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 1;
		overflow: hidden;
	}

	.wanted-meta {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 12rpx;
	}

	.wanted-budget {
		font-size: 26rpx;
		color: #5A7D9E;
		font-weight: bold;
	}

	.wanted-campus {
		font-size: 22rpx;
		color: #999;
	}

	.wanted-tags {
		display: flex;
		gap: 10rpx;
		margin-bottom: 12rpx;
	}

	.wanted-tag {
		font-size: 22rpx;
		color: #5A7D9E;
		background: #EDF2F6;
		padding: 4rpx 16rpx;
		border-radius: 20rpx;
	}

	.wanted-desc {
		font-size: 26rpx;
		color: #666;
		line-height: 1.5;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
		margin-bottom: 16rpx;
	}

	.wanted-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.wanted-user {
		display: flex; align-items: center; gap: 8rpx;
	}
	.wanted-avatar {
		width: 36rpx; height: 36rpx; border-radius: 50%; background: #EDF2F6;
	}
	.wanted-avatar-def {
		width: 36rpx; height: 36rpx; border-radius: 50%;
		background: #EDF2F6; color: #5A7D9E;
		font-size: 20rpx; font-weight: bold;
		display: flex; align-items: center; justify-content: center;
	}
	.wanted-name {
		font-size: 24rpx; color: #999;
	}

	.wanted-time {
		font-size: 22rpx;
		color: #ccc;
	}

	/* ===== 空状态 ===== */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: 160rpx;
	}

	.empty-icon { font-size: 100rpx; margin-bottom: 20rpx; }
	.empty-text { font-size: 28rpx; color: #999; }
</style>

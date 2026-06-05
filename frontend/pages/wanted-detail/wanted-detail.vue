<template>
	<view class="wd-page">
		<!-- 顶部信息区 -->
		<view class="wd-hero">
			<text class="wd-hero-icon">📋</text>
			<text class="wd-hero-title">{{ detail.title }}</text>
			<view class="wd-budget-row">
				<text class="wd-budget-label">预算</text>
				<text class="wd-budget-value">¥{{ detail.budgetMin }} — ¥{{ detail.budgetMax }}</text>
			</view>
		</view>

		<!-- 基本信息卡片 -->
		<view class="wd-info-card">
			<view class="wd-info-row">
				<view class="wd-info-item">
					<text class="wd-info-label">校区</text>
					<text class="wd-info-value">{{ detail.campus }}</text>
				</view>
				<view class="wd-info-item">
					<text class="wd-info-label">分类</text>
					<text class="wd-info-value">{{ detail.categoryLabel }}</text>
				</view>
				<view class="wd-info-item">
					<text class="wd-info-label">期望成色</text>
					<text class="wd-info-value">{{ detail.condition }}</text>
				</view>
			</view>
		</view>

		<!-- 详细描述 -->
		<view class="wd-desc-card">
			<text class="wd-section-title">需求描述</text>
			<text class="wd-desc-text">{{ detail.desc }}</text>
			<view class="wd-image-grid" v-if="detail.images && detail.images.length">
				<image
					v-for="(img, idx) in detail.images"
					:key="idx"
					class="wd-image-thumb"
					:src="img"
					mode="aspectFill"
					@click="previewImage(idx)"
				/>
			</view>
		</view>

		<!-- 发布者信息 -->
		<view class="wd-publisher-card">
			<view class="wd-pub-left">
				<view class="wd-pub-avatar" @click="goPublisherProfile">
					<image v-if="detail.avatar" class="wd-pub-avatar-img" :src="detail.avatar" mode="aspectFill"></image>
					<text v-else class="wd-pub-avatar-emoji">{{ (detail.nickname || detail.username || '?').charAt(0) }}</text>
				</view>
				<view class="wd-pub-info">
					<text class="wd-pub-name">{{ detail.nickname || detail.username }}</text>
					<text class="wd-pub-time">{{ detail.time }} 发布</text>
				</view>
			</view>
			<view class="wd-pub-credit">
				<text class="wd-credit-text">信用良好</text>
			</view>
		</view>

		<!-- 温馨提示 -->
		<view v-if="!isOwner" class="wd-tips">
			<text class="wd-tips-icon">💡</text>
			<text class="wd-tips-text">如果你有符合要求的物品，可以直接联系TA哦</text>
		</view>

		<!-- 本人进行中 - 状态标签 -->
		<view v-if="isOwner" class="wd-status-tag" :class="detail.status === 'active' ? 'status-seeking' : 'status-done'">
			<text>{{ detail.status }}</text>
		</view>

		<!-- 底部操作栏：本人进行中 - 编辑/撤销 -->
		<view v-if="isOwner && detail.status === 'active'" class="wd-bottom-bar wd-owner-bar">
			<button class="wd-edit-btn" @click="editWanted">编辑信息</button>
			<button class="wd-cancel-btn" @click="handleCloseWanted">结束求购</button>
		</view>

		<!-- 底部操作栏：非本人 - 收藏/联系 -->
		<view v-else-if="!isOwner" class="wd-bottom-bar">
			<view class="wd-collect" @click="toggleCollect">
				<text class="wd-collect-icon iconfont" :class="isCollected ? 'icon-xz' : 'icon-shoucang'"></text>
			</view>
			<button class="wd-contact-btn" @click="contactSeller">💬 联系TA</button>
		</view>
	</view>
</template>

<script>
	import { getWantedDetail, closeWanted } from '@/api/wanted.js'
	import { getCategories } from '@/api/item.js'
	import { createSession } from '@/api/chat.js'
	import { addWantedFavorite, removeWantedFavorite, getMyFavorites } from '@/api/favorite.js'

	export default {
		data() {
			return {
				detail: {},
				categories: [],
				isCollected: false,
				isOwner: false
			}
		},
		async onLoad(options) {
			const id = parseInt(options.id)
			if (options.mode === 'self') {
				this.isOwner = true
			}
			await this.loadCategories()
			if (id) {
				await this.loadDetail(id)
			}
		},
		methods: {
			async loadCategories() {
				try {
					this.categories = await getCategories()
				} catch (e) {
					this.categories = []
				}
			},
			async loadDetail(id) {
				try {
					const data = await getWantedDetail(id)
					const user = uni.getStorageSync('user') || {}
					this.isOwner = data.userId === user.id
					this.detail = {
						id: data.id,
						title: data.title,
						budgetMin: data.budgetMin,
						budgetMax: data.budgetMax,
						campus: data.campus || '',
						condition: data.conditionLevel || '不限',
						categoryId: data.categoryId,
						categoryLabel: this.getCategoryLabel(data.categoryId),
						status: data.status,
						desc: data.description || '',
						images: data.images || [],
						nickname: data.nickname || '',
						username: data.username || '',
						avatar: data.avatar || '',
						time: this.formatTime(data.createTime),
						userId: data.userId,
						isOwner: data.userId === user.id
					}
					if (user && user.id) {
						this.checkIfCollected(user.id)
					}
				} catch (e) {
					uni.showToast({ title: '加载求购详情失败', icon: 'none' })
				}
			},
			async checkIfCollected(userId) {
				try {
					const favList = await getMyFavorites(userId)
					const list = (favList && favList.records) || favList || []
					this.isCollected = list.some(f => f.wantedId === this.detail.id)
				} catch (e) {}
			},
			getCategoryLabel(categoryId) {
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
			async toggleCollect() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return
				}
				try {
					if (this.isCollected) {
						await removeWantedFavorite(this.detail.id, user.id)
						this.isCollected = false
						uni.showToast({ title: '已取消收藏', icon: 'none' })
					} else {
						await addWantedFavorite(this.detail.id, user.id)
						this.isCollected = true
						uni.showToast({ title: '已收藏', icon: 'none' })
					}
				} catch (e) {}
			},
			goPublisherProfile() {
				if (this.detail.userId) {
					uni.navigateTo({ url: '/pages/profile/profile?id=' + this.detail.userId })
				}
			},
			async contactSeller() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return
				}
				try {
					const session = await createSession(user.id, { wantedId: this.detail.id })
					const pid = session.buyerId === user.id ? session.sellerId : session.buyerId
						uni.navigateTo({ url: '/pages/chat/chat?id=' + session.id + '&partnerId=' + (pid || 0) + '&name=' + encodeURIComponent(this.detail.username) + '&avatar=' + encodeURIComponent(session.partnerAvatar || '') })
				} catch (e) {
					uni.showToast({ title: '创建会话失败', icon: 'none' })
				}
			},
			previewImage(idx) {
				uni.previewImage({
					current: idx,
					urls: this.detail.images
				})
			},
			editWanted() {
				uni.setStorageSync('editWantedData', this.detail)
				uni.navigateTo({ url: '/pages/publish-form/publish-form?type=buy&edit=1&id=' + this.detail.id })
			},
			handleCloseWanted() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) return
				uni.showModal({
					title: '结束求购',
					content: '确认结束该求购？结束后其他用户将无法看到该求购信息。',
					success: async (res) => {
						if (res.confirm) {
							try {
								await closeWanted(this.detail.id, user.id)
								this.detail.status = 'closed'
								uni.showToast({ title: '已结束', icon: 'success' })
							} catch (e) {
								uni.showToast({ title: '操作失败', icon: 'none' })
							}
						}
					}
				})
			},	}
	}
</script>

<style>
	.wd-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding-bottom: 140rpx;
		box-sizing: border-box;
	}

	/* ===== 顶部 ===== */
	.wd-hero {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		padding: 44rpx 30rpx 50rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.wd-hero-icon {
		font-size: 56rpx;
		margin-bottom: 16rpx;
	}

	.wd-hero-title {
		font-size: 38rpx;
		color: #ffffff;
		font-weight: bold;
		text-align: center;
		margin-bottom: 24rpx;
		line-height: 1.4;
	}

	.wd-budget-row {
		background: rgba(255,255,255,0.2);
		border-radius: 40rpx;
		padding: 12rpx 32rpx;
		display: flex;
		align-items: center;
		gap: 12rpx;
	}

	.wd-budget-label {
		font-size: 24rpx;
		color: rgba(255,255,255,0.7);
	}

	.wd-budget-value {
		font-size: 32rpx;
		color: #ffffff;
		font-weight: bold;
	}

	/* ===== 基本信息卡片 ===== */
	.wd-info-card {
		background: #ffffff;
		margin: 20rpx;
		border-radius: 16rpx;
		padding: 32rpx 24rpx;
		box-sizing: border-box;
	}

	.wd-info-row {
		display: flex;
		justify-content: space-around;
	}

	.wd-info-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 10rpx;
	}

	.wd-info-label {
		font-size: 22rpx;
		color: #999;
	}

	.wd-info-value {
		font-size: 26rpx;
		color: #333;
		font-weight: bold;
	}

	/* ===== 描述 ===== */
	.wd-desc-card {
		background: #ffffff;
		margin: 0 20rpx 20rpx;
		border-radius: 16rpx;
		padding: 30rpx 24rpx;
		box-sizing: border-box;
	}

	.wd-section-title {
		font-size: 28rpx;
		color: #3A6341;
		font-weight: bold;
		margin-bottom: 20rpx;
		display: block;
	}

	.wd-desc-text {
		font-size: 28rpx;
		color: #444;
		line-height: 1.8;
	}

	.wd-image-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 12rpx;
		margin-top: 24rpx;
	}

	.wd-image-thumb {
		width: 200rpx;
		height: 200rpx;
		border-radius: 12rpx;
		background: #f0f0f0;
	}

	/* ===== 发布者 ===== */
	.wd-publisher-card {
		background: #ffffff;
		margin: 0 20rpx 20rpx;
		border-radius: 16rpx;
		padding: 24rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
		box-sizing: border-box;
	}

	.wd-pub-left {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.wd-pub-avatar {
		width: 80rpx; height: 80rpx;
		border-radius: 50%;
		background: #EDF2F6;
		color: #5A7D9E;
		font-size: 36rpx;
		font-weight: bold;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0; overflow: hidden;
	}
	.wd-pub-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
	.wd-pub-avatar-emoji { font-size: 36rpx; color: #5A7D9E; font-weight: bold; }

	.wd-pub-info {
		display: flex;
		flex-direction: column;
		gap: 6rpx;
	}

	.wd-pub-name {
		font-size: 30rpx;
		color: #333;
		font-weight: bold;
	}

	.wd-pub-time {
		font-size: 22rpx;
		color: #999;
	}

	.wd-pub-credit {
		background: #EDF2F6;
		padding: 8rpx 20rpx;
		border-radius: 20rpx;
		display: flex;
		align-items: center;
	}

	.wd-credit-text {
		font-size: 22rpx;
		color: #5A7D9E;
		line-height: 1;
	}

	/* ===== 本人状态标签 ===== */
	.wd-status-tag {
		margin: 0 20rpx 20rpx;
		padding: 16rpx 24rpx;
		border-radius: 12rpx;
		text-align: center;
		font-size: 26rpx;
		font-weight: bold;
	}
	.status-seeking { background: #e8f5ee; color: #3A6341; }
	.status-done { background: #f0f0f0; color: #999; }

	/* ===== 提示 ===== */
	.wd-tips {
		margin: 0 20rpx;
		padding: 20rpx 24rpx;
		background: #fff9e6;
		border-radius: 12rpx;
		display: flex;
		align-items: center;
		gap: 12rpx;
		box-sizing: border-box;
	}

	.wd-tips-icon {
		font-size: 28rpx;
		flex-shrink: 0;
	}

	.wd-tips-text {
		font-size: 24rpx;
		color: #b8860b;
		line-height: 1.4;
	}

	/* ===== 底部操作栏 ===== */
	.wd-bottom-bar {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		height: 120rpx;
		background: #ffffff;
		display: flex;
		align-items: center;
		padding: 0 30rpx;
		border-top: 1rpx solid #f0f0f0;
		box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
		box-sizing: border-box;
	}

	.wd-collect {
		width: 100rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 4rpx;
	}

	.wd-collect-icon { font-size: 48rpx; color: #ccc; transition: color 0.3s; }
	.icon-xz { color: #E85A4F; }

	.wd-contact-btn {
		flex: 1;
		margin-left: 20rpx;
		height: 80rpx;
		line-height: 80rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		color: #ffffff;
		font-size: 30rpx;
		font-weight: bold;
		border-radius: 40rpx;
		border: none;
		box-shadow: 0 8rpx 24rpx rgba(90,125,158,0.3);
	}

	/* ===== 本人操作栏 ===== */
	.wd-owner-bar {
		justify-content: center;
		gap: 20rpx;
	}
	.wd-edit-btn {
		flex: 1;
		background: #e8f5ee; color: #3A6341;
		font-size: 28rpx; font-weight: bold;
		height: 80rpx; line-height: 80rpx;
		border-radius: 40rpx; margin: 0; border: none;
	}
	.wd-cancel-btn {
		flex: 1;
		background: #f5f5f5; color: #3f3f3f;
		font-size: 28rpx; font-weight: bold;
		height: 80rpx; line-height: 80rpx;
		border-radius: 40rpx; margin: 0; border: none;
	}
</style>

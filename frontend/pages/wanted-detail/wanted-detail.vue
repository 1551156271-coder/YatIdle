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
				<view class="wd-pub-avatar">
					<image v-if="detail.avatar" class="wd-pub-avatar-img" :src="detail.avatar" mode="aspectFill"></image>
					<text v-else class="wd-pub-avatar-emoji">{{ detail.username.charAt(0) }}</text>
				</view>
				<view class="wd-pub-info">
					<text class="wd-pub-name">{{ detail.username }}</text>
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

		<!-- 本人求购中 - 状态标签 -->
		<view v-if="isOwner" class="wd-status-tag" :class="detail.status === '求购中' ? 'status-seeking' : 'status-done'">
			<text>{{ detail.status }}</text>
		</view>

		<!-- 底部操作栏：本人求购中 - 编辑/撤销 -->
		<view v-if="isOwner && detail.status === '求购中'" class="wd-bottom-bar wd-owner-bar">
			<button class="wd-edit-btn" @click="editWanted">编辑信息</button>
			<button class="wd-cancel-btn" @click="cancelWanted">撤销求购</button>
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
	export default {
		data() {
			return {
				detail: {},
				isCollected: false,
				isOwner: false,
				mockData: [
					{ id: 101, title: 'MacBook Pro 14寸', budgetMin: '5000', budgetMax: '7000', campus: '东校园', condition: '90新以上', categoryLabel: '数码电子', status: '求购中', desc: '主要用来写代码和做设计，14寸最好，16寸也可以接受。最好是M系列芯片，16G内存以上，成色不要太差。', username: '测试用户', time: '2天前', images: ['https://images.unsplash.com/photo-1517336714731-489689fd1ca8?q=80&w=400&auto=format&fit=crop'] },
					{ id: 102, title: 'iPad + 苹果笔', budgetMin: '2000', budgetMax: '3500', campus: '南校园', condition: '85新以上', categoryLabel: '数码电子', status: '求购中', desc: '主要用于记笔记和看网课，iPad Air或Pro都可以，带笔优先。屏幕不要有划痕，电池续航正常就行。', username: '测试用户', time: '3天前', images: ['https://images.unsplash.com/photo-1546868871-af0de0ae72be?q=80&w=400&auto=format&fit=crop'] },
					{ id: 103, title: '数位板', budgetMin: '200', budgetMax: '500', campus: '东校园', condition: '不限', categoryLabel: '数码电子', status: '已找到', desc: '入门级数位板就行，Wacom或国产都可以，主要用来画一些简单的插画。', username: '测试用户', time: '1周前' },
					{ id: 104, title: '收一双42码跑鞋', budgetMin: '100', budgetMax: '250', campus: '北校园', condition: '95新以上', categoryLabel: '运动户外', status: '求购中', desc: '体育课需要，穿不了几次所以不想买全新的。耐克或者阿迪的都可以，42码，颜色不限，只要没有明显磨损就行。', username: '运动达人', time: '昨天' }
				]
			}
		},
		onLoad(options) {
			const id = parseInt(options.id)
			this.detail = this.mockData.find(item => item.id === id) || this.mockData[0]
			if (options.mode === 'self') {
				this.isOwner = true
			}
		},
		methods: {
			toggleCollect() {
				this.isCollected = !this.isCollected
				uni.showToast({ title: this.isCollected ? '已收藏' : '取消收藏', icon: 'none' })
			},
			contactSeller() {
				uni.navigateTo({ url: '/pages/chat/chat?id=2' })
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
			cancelWanted() {
				uni.showModal({
					title: '撤销求购',
					content: '确认撤销该求购？撤销后其他用户将无法看到该求购信息。',
					success: (res) => {
						if (res.confirm) {
							this.detail.status = '已找到'
							uni.showToast({ title: '已撤销', icon: 'success' })
						}
					}
				})
			},
		}
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

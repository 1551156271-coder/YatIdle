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
		</view>

		<!-- 发布者信息 -->
		<view class="wd-publisher-card">
			<view class="wd-pub-left">
				<view class="wd-pub-avatar">{{ detail.username.charAt(0) }}</view>
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
		<view class="wd-tips">
			<text class="wd-tips-icon">💡</text>
			<text class="wd-tips-text">如果你有符合要求的物品，可以直接联系TA哦</text>
		</view>

		<!-- 底部操作栏 -->
		<view class="wd-bottom-bar">
			<view class="wd-collect" @click="toggleCollect">
				<text class="wd-collect-icon" :style="{ color: isCollected ? '#e74c3c' : '#ccc' }">♥</text>
				<text class="wd-collect-text">{{ isCollected ? '已收藏' : '收藏' }}</text>
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
				mockData: [
					{ id: 101, title: '求购一台二手笔记本电脑', budgetMin: '2000', budgetMax: '3500', campus: '东校园', condition: '85新以上', categoryLabel: '数码电子', desc: '女生自用，主要用于写论文和看视频，电池续航好一些的。希望屏幕不要太小，14寸左右最好，品牌不限但最好是轻薄本。', username: '小橙子', time: '10分钟前' },
					{ id: 102, title: '收高数下册+习题集', budgetMin: '15', budgetMax: '30', campus: '南校园', condition: '不限', categoryLabel: '书籍教材', desc: '下学期要用，有笔记也可以，价格好商量。最好是同济第七版的高等数学下册，配套习题集一起收。', username: '数学苦手', time: '1小时前' },
					{ id: 103, title: '二手电动车代步用', budgetMin: '600', budgetMax: '1200', campus: '珠海校区', condition: '90新以上', categoryLabel: '生活用品', desc: '校区太大走路太累，求购一辆二手电动车，续航好一点的。最好是雅迪或者小牛，电池能跑30公里以上就行。', username: '骑车上学', time: '3小时前' },
					{ id: 104, title: '收一双42码跑鞋', budgetMin: '100', budgetMax: '250', campus: '北校园', condition: '95新以上', categoryLabel: '运动户外', desc: '体育课需要，穿不了几次所以不想买全新的。耐克或者阿迪的都可以，42码，颜色不限，只要没有明显磨损就行。', username: '运动达人', time: '昨天' }
				]
			}
		},
		onLoad(options) {
			const id = parseInt(options.id)
			this.detail = this.mockData.find(item => item.id === id) || this.mockData[0]
		},
		methods: {
			toggleCollect() {
				this.isCollected = !this.isCollected
				uni.showToast({ title: this.isCollected ? '已收藏' : '取消收藏', icon: 'none' })
			},
			contactSeller() {
				uni.showToast({ title: '聊天功能即将上线', icon: 'none' })
			}
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
		background: linear-gradient(135deg, #00613C, #00804B);
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
		color: #00613C;
		font-weight: bold;
		margin-bottom: 20rpx;
		display: block;
	}

	.wd-desc-text {
		font-size: 28rpx;
		color: #444;
		line-height: 1.8;
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
		width: 80rpx;
		height: 80rpx;
		border-radius: 50%;
		background: #e8f5ee;
		color: #00613C;
		font-size: 36rpx;
		font-weight: bold;
		display: flex;
		align-items: center;
		justify-content: center;
		flex-shrink: 0;
	}

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
		background: #e8f5ee;
		padding: 8rpx 20rpx;
		border-radius: 20rpx;
	}

	.wd-credit-text {
		font-size: 22rpx;
		color: #00613C;
	}

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

	.wd-collect-icon {
		font-size: 44rpx;
		transition: color 0.3s;
	}

	.wd-collect-text {
		font-size: 20rpx;
		color: #999;
	}

	.wd-contact-btn {
		flex: 1;
		margin-left: 20rpx;
		height: 80rpx;
		line-height: 80rpx;
		background: linear-gradient(135deg, #00613C, #00804B);
		color: #ffffff;
		font-size: 30rpx;
		font-weight: bold;
		border-radius: 40rpx;
		border: none;
		box-shadow: 0 8rpx 24rpx rgba(0,97,60,0.3);
	}
</style>

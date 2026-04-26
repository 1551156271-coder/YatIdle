<template>
	<view class="home-page">
		<!-- 顶部装饰 -->
		<view class="top-circle"></view>

		<!-- 主内容 -->
		<view class="main-card">
			<view class="logo-box">
				<image class="logo" src="/static/logo.png" mode="aspectFit"></image>
			</view>
			<text class="app-name">闲鸭蛋</text>
			<text class="slogan">中大校园二手交易平台</text>
			<view class="features">
				<view class="feat-item">📦 海量好物</view>
				<view class="feat-item">🤝 校友直卖</view>
				<view class="feat-item">📍 校内自提</view>
			</view>
			<button class="enter-btn" @click="goToDetail">开始使用</button>
		</view>

		<!-- 底部简短说明 -->
		<text class="footer-tip">点击上方按钮了解详情</text>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				title: '闲鸭蛋'
			}
		},
		methods: {
			goToDetail() {
				uni.navigateTo({
					url: '/pages/goods-detail/goods-detail'
				});
			}
		}
	}
</script>

<style>
	/* 页面容器 */
	.home-page {
		min-height: 100vh;
		background: linear-gradient(160deg, #e8f5ee 0%, #ffffff 100%);
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		padding: 0 50rpx;
		position: relative;
		overflow: hidden;
	}

	/* 背景装饰圆 */
	.top-circle {
		position: absolute;
		top: -200rpx;
		right: -100rpx;
		width: 600rpx;
		height: 600rpx;
		background: rgba(0, 97, 60, 0.06);
		border-radius: 50%;
	}

	/* 主体卡片 */
	.main-card {
		background: white;
		border-radius: 40rpx;
		padding: 80rpx 50rpx 60rpx;
		width: 100%;
		box-shadow: 0 20rpx 60rpx rgba(0, 97, 60, 0.08);
		display: flex;
		flex-direction: column;
		align-items: center;
		position: relative;
		z-index: 1;
	}

	/* logo 容器 */
	.logo-box {
		width: 160rpx;
		height: 160rpx;
		background: #f0faf4;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 30rpx;
	}
	.logo {
		width: 120rpx;
		height: 120rpx;
	}

	/* 应用名 */
	.app-name {
		font-size: 52rpx;
		font-weight: bold;
		color: #00613C;
		letter-spacing: 6rpx;
		margin-bottom: 16rpx;
	}

	/* 标语 */
	.slogan {
		font-size: 28rpx;
		color: #666;
		margin-bottom: 40rpx;
	}

	/* 特点介绍 */
	.features {
		display: flex;
		gap: 20rpx;
		margin-bottom: 50rpx;
		flex-wrap: wrap;
		justify-content: center;
	}
	.feat-item {
		font-size: 24rpx;
		color: #00613C;
		background: #e8f5ee;
		padding: 10rpx 24rpx;
		border-radius: 30rpx;
	}

	/* 进入按钮 */
	.enter-btn {
		width: 100%;
		height: 90rpx;
		line-height: 90rpx;
		background: #00613C;
		color: white;
		font-size: 32rpx;
		font-weight: bold;
		border-radius: 50rpx;
		border: none;
		box-shadow: 0 12rpx 30rpx rgba(0, 97, 60, 0.25);
	}

	/* 底部小字 */
	.footer-tip {
		margin-top: 40rpx;
		font-size: 22rpx;
		color: #aaa;
	}
</style>
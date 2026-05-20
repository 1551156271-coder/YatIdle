<template>
	<view class="publish-select-page">
		<view class="ps-hero">
			<text class="ps-icon">📦</text>
			<text class="ps-title">选择发布类型</text>
			<text class="ps-sub">你想出售闲置还是求购好物？</text>
		</view>

		<view class="ps-cards">
			<view class="ps-card ps-card-sell" @click="goPublish('sell')">
				<view class="ps-card-icon">
					<text class="ps-card-emoji">💸</text>
				</view>
				<text class="ps-card-title">出售闲置</text>
				<text class="ps-card-desc">上传商品信息，把闲置物品卖给需要的同学</text>
				<view class="ps-card-btn">发布闲置</view>
			</view>

			<view class="ps-card ps-card-buy" @click="goPublish('buy')">
				<view class="ps-card-icon">
					<text class="ps-card-emoji">📋</text>
				</view>
				<text class="ps-card-title">求购物品</text>
				<text class="ps-card-desc">发布求购信息，让有货的同学主动联系你</text>
				<view class="ps-card-btn">发布求购</view>
			</view>
		</view>
	</view>
	<tab-bar />
</template>

<script>
	import TabBar from '@/components/tab-bar.vue'
	export default {
		components: { TabBar },
		onShow() {
			uni.hideTabBar()
		},
		methods: {
			goPublish(type) {
				uni.navigateTo({
					url: '/pages/publish-form/publish-form?type=' + type
				})
			}
		}
	}
</script>

<style>
	.publish-select-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
	}

	.ps-hero {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		padding: 60rpx 30rpx 60rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.ps-icon { font-size: 72rpx; margin-bottom: 16rpx; }
	.ps-title { font-size: 40rpx; color: #ffffff; font-weight: bold; margin-bottom: 10rpx; }
	.ps-sub { font-size: 26rpx; color: rgba(255,255,255,0.8); }

	.ps-cards {
		padding: 30rpx 24rpx;
		display: flex;
		flex-direction: column;
		gap: 24rpx;
		box-sizing: border-box;
	}

	.ps-card {
		background: #ffffff;
		border-radius: 24rpx;
		padding: 40rpx 30rpx 36rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
		transition: transform 0.15s;
	}

	.ps-card:active {
		transform: scale(0.97);
	}

	.ps-card-icon {
		width: 120rpx;
		height: 120rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
		margin-bottom: 24rpx;
	}

	.ps-card-sell .ps-card-icon { background: #e8f5ee; }
	.ps-card-buy .ps-card-icon { background: #EDF2F6; }

	.ps-card-emoji { font-size: 56rpx; }

	.ps-card-title {
		font-size: 34rpx;
		color: #333;
		font-weight: bold;
		margin-bottom: 12rpx;
	}

	.ps-card-desc {
		font-size: 26rpx;
		color: #999;
		text-align: center;
		line-height: 1.5;
		margin-bottom: 28rpx;
	}

	.ps-card-btn {
		padding: 16rpx 60rpx;
		border-radius: 44rpx;
		font-size: 28rpx;
		font-weight: bold;
	}

	.ps-card-sell .ps-card-btn {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		color: #ffffff;
	}

	.ps-card-buy .ps-card-btn {
		background: linear-gradient(135deg, #5A7D9E, #7A9DBE);
		color: #ffffff;
	}
</style>

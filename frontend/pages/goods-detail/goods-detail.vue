<template>
	<view class="container">
		<view class="image-box">
			<image class="main-image" src="https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=1000&auto=format&fit=crop" mode="aspectFill"></image>
		</view>

		<view class="info-card">
			<view class="price-row">
				<text class="symbol">￥</text>
				<text class="price">{{ price }}</text>
				<view class="tag-badge">九成新</view>
			</view>
			<view class="title">{{ title }}</view>
			<view class="meta-row">
				<view class="meta-item">📦 自提</view>
				<view class="meta-item">📍 东校园</view>
				<view class="meta-item">🕐 2天前发布</view>
			</view>
		</view>

		<view class="seller-card">
			<view class="seller-left">
				<view class="avatar" @click="goToProfile">🎓</view>
				<view class="seller-text">
					<view class="name-row">
						<text class="seller-name">中大在校生</text>
						<text class="verified-badge">已认证</text>
					</view>
				</view>
			</view>
			<view class="follow-btn" :class="{ 'followed': isFollowed }" @click="toggleFollow">
				{{ isFollowed ? '已关注' : '+ 关注' }}
			</view>
		</view>

		<view class="detail-section">
			<view class="section-title">商品详情</view>
			<text class="description">{{ description }}</text>
		</view>

		<view class="tags-section">
			<view class="tag">自行车</view>
			<view class="tag">出行</view>
			<view class="tag">东校区自提</view>
		</view>

		<view class="bottom-action">
			<view class="icon-group" @click="toggleCollect">
				<text class="collect-icon" :style="{ color: isCollected ? '#e74c3c' : '#ccc' }">♥</text>
			</view>
			<view class="btn-group">
				<button class="chat-btn" @click="handleAction('咨询')">💬 咨询</button>
				<button class="buy-btn" @click="handleAction('购买')">立即购买</button>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				title: "出九成新公路自行车，带锁和挡泥板",
				price: "268.00",
				description: "去年学期初在校外车行买的，平时只在教学楼和宿舍之间通勤。车架很轻，变速灵敏。离校转手，东校园自提。配件齐全，包括车锁和挡泥板，骑行体验非常好。",
				isCollected: false,
				isFollowed: false
			}
		},
		methods: {
			goToProfile() {
				uni.navigateTo({ url: '/pages/profile/profile?id=2' })
			},
			toggleFollow() {
				this.isFollowed = !this.isFollowed
				uni.showToast({ title: this.isFollowed ? '已关注' : '已取消关注', icon: 'none' })
			},
			toggleCollect() {
				this.isCollected = !this.isCollected;
				uni.showToast({
					title: this.isCollected ? '已收藏' : '取消收藏',
					icon: 'none'
				});
			},
			handleAction(type) {
				uni.showToast({
					title: '点击了' + type,
					icon: 'none'
				});
			}
		}
	}
</script>

<style>
	.container {
		background-color: #f5f5f5;
		min-height: 100vh; width: 100%; overflow: hidden; box-sizing: border-box;
		padding-bottom: 140rpx;
	}

	.image-box {
		width: 100%;
		height: 750rpx;
		position: relative;
	}
	.main-image { width: 100%; height: 100%; }
	.tag-badge {
		background: #e8f5ee;
		color: #00613C;
		font-size: 24rpx;
		padding: 6rpx 18rpx;
		border-radius: 30rpx;
		align-self: center;
		margin-left: 12rpx;
		white-space: nowrap;
	}

	.info-card {
		background: white;
		margin: 20rpx;
		padding: 40rpx 30rpx;
		border-radius: 20rpx;
	}
	.price-row { display: flex; align-items: baseline; margin-bottom: 16rpx; }
	.symbol { color: #da0000; font-size: 32rpx; font-weight: bold; }
	.price { color: #da0000; font-size: 56rpx; font-weight: bold; margin-right: 16rpx; }
	.title { font-size: 36rpx; color: #1a1a1a; font-weight: 500; line-height: 1.4; margin-bottom: 20rpx; }
	.meta-row { display: flex; align-items: center; gap: 12rpx; margin-bottom: 8rpx; }
	.meta-item { font-size: 24rpx; color: #999; }

	/* ===== 卖家卡片 ===== */
	.seller-card {
		background: white;
		margin: 0 20rpx 20rpx;
		padding: 30rpx;
		border-radius: 20rpx;
		display: flex;
		align-items: center;
		justify-content: space-between;
	}
	.seller-left { display: flex; align-items: center; gap: 20rpx; flex: 1; }
	.avatar {
		width: 88rpx; height: 88rpx;
		background: #e8f5ee;
		border-radius: 50%;
		display: flex; align-items: center; justify-content: center;
		font-size: 44rpx; flex-shrink: 0;
	}
	.seller-text { display: flex; flex-direction: column; justify-content: center; }
	.name-row { display: flex; align-items: center; gap: 12rpx; }
	.seller-name { font-size: 30rpx; font-weight: bold; color: #333; line-height: 1.4; }
	.verified-badge {
		font-size: 20rpx;
		color: #1565C0;
		background: #e3f2fd;
		padding: 2rpx 12rpx;
		border-radius: 6rpx;
		line-height: 1.4;
		white-space: nowrap;
	}
	.follow-btn {
		flex-shrink: 0;
		padding: 14rpx 32rpx;
		background: #00613C;
		color: #ffffff;
		font-size: 26rpx;
		font-weight: bold;
		border-radius: 32rpx;
	}
	.follow-btn.followed { background: #f0f0f0; color: #666; }

	.detail-section {
		background: white;
		margin: 0 20rpx 20rpx;
		padding: 30rpx;
		border-radius: 20rpx;
	}
	.section-title { font-size: 28rpx; color: #00613C; font-weight: bold; margin-bottom: 20rpx; letter-spacing: 2rpx; }
	.description { font-size: 30rpx; color: #444; line-height: 1.8; }

	.tags-section { display: flex; gap: 16rpx; padding: 0 20rpx 20rpx; flex-wrap: wrap; }
	.tag { background: #e8f5ee; color: #00613C; font-size: 24rpx; padding: 10rpx 24rpx; border-radius: 30rpx; }

	.bottom-action {
		position: fixed; bottom: 0; left: 0; right: 0;
		height: 120rpx; background: white;
		display: flex; align-items: center;
		padding: 0 30rpx;
		border-top: 1rpx solid #f0f0f0;
		box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
	}
	.icon-group { width: 80rpx; display: flex; align-items: center; justify-content: center; margin-right: 20rpx; }
	.collect-icon { font-size: 54rpx; transition: all 0.3s; }
	.btn-group { flex: 1; display: flex; gap: 16rpx; justify-content: flex-end; }
	.chat-btn {
		background: #e8f5ee; color: #00613C;
		font-size: 28rpx; font-weight: bold;
		width: 180rpx; height: 80rpx; line-height: 80rpx;
		border-radius: 40rpx; margin: 0; border: none;
	}
	.buy-btn {
		background: #00613C; color: white;
		font-size: 28rpx; font-weight: bold;
		width: 220rpx; height: 80rpx; line-height: 80rpx;
		border-radius: 40rpx; margin: 0; border: none;
	}
</style>

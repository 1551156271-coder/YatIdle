<template>
	<view class="container">
		<view class="image-box">
			<image class="main-image" src="https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=1000&auto=format&fit=crop" mode="aspectFill"></image>
			<view v-if="isSeller" class="status-badge" :class="statusClass">{{ statusText }}</view>
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

		<!-- 买家模式：卖家信息卡 -->
		<view v-if="!isSeller" class="seller-card">
			<view class="seller-left">
				<view class="avatar" @click="goToProfile">
					<image v-if="sellerAvatar" class="avatar-img" :src="sellerAvatar" mode="aspectFill"></image>
					<text v-else class="avatar-emoji">🎓</text>
				</view>
				<view class="seller-text">
					<view class="name-row">
						<text class="seller-name">中大在校生</text>
						<text class="verified-badge">已认证</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 卖家模式 + 已售：买家信息卡 -->
		<view v-if="isSeller && goodsStatus === 'sold'" class="seller-card buyer-card">
			<view class="section-label">购买者信息</view>
			<view class="seller-left" style="margin-top: 16rpx;">
				<view class="avatar" @click="goToBuyerProfile">
					<image v-if="buyerAvatar" class="avatar-img" :src="buyerAvatar" mode="aspectFill"></image>
					<text v-else class="avatar-emoji">📚</text>
				</view>
				<view class="seller-text">
					<view class="name-row">
						<text class="seller-name">李四（买家）</text>
						<text class="verified-badge">已认证</text>
					</view>
					<text class="buyer-contact">交易时间：2024-05-09</text>
				</view>
			</view>
			<view class="contact-buyer-btn" @click="contactBuyer">联系买家</view>
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

		<!-- 底部操作栏：买家模式 -->
		<view v-if="!isSeller" class="bottom-action">
			<view class="icon-group" @click="toggleCollect">
				<text class="collect-icon iconfont" :class="isCollected ? 'icon-xz' : 'icon-shoucang'"></text>
			</view>
			<view class="btn-group">
				<button class="chat-btn" @click="handleAction('咨询')">💬 咨询</button>
				<button class="buy-btn" @click="handleAction('购买')">立即购买</button>
			</view>
		</view>

		<!-- 底部操作栏：卖家模式 - 在售 -->
		<view v-if="isSeller && goodsStatus === 'selling'" class="bottom-action seller-bottom">
			<button class="edit-btn" @click="editGoods">编辑信息</button>
			<button class="cancel-btn" @click="cancelListing">下架商品</button>
		</view>

		<!-- 底部操作栏：卖家模式 - 已下架 -->
		<view v-if="isSeller && goodsStatus === 'off'" class="bottom-action seller-bottom">
			<button class="buy-btn" style="flex:1;" @click="relistGoods">重新上架</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				goodsId: 1,
				title: "出九成新公路自行车，带锁和挡泥板",
				price: "268.00",
				description: "去年学期初在校外车行买的，平时只在教学楼和宿舍之间通勤。车架很轻，变速灵敏。离校转手，东校园自提。配件齐全，包括车锁和挡泥板，骑行体验非常好。",
				isCollected: false,
				isSeller: false,
				sellerAvatar: "",
				buyerAvatar: "",
				goodsStatus: 'selling', // 'selling' | 'sold' | 'off'
			}
		},
		computed: {
			statusText() {
				const map = { selling: '在售', sold: '已售', off: '已下架' }
				return map[this.goodsStatus] || ''
			},
			statusClass() {
				return this.goodsStatus === 'selling' ? 'status-on' : 'status-off'
			}
		},
		onLoad(options) {
			if (options.mode === 'seller') {
				this.isSeller = true
			}
			if (options.status) {
				// status: 'selling' | 'sold' | 'off'
				const statusMap = { '在售': 'selling', '已售': 'sold', '已下架': 'off' }
				this.goodsStatus = statusMap[options.status] || 'selling'
			}
			if (options.id) {
				this.goodsId = options.id
			}
		},
		methods: {
			goToProfile() {
				uni.navigateTo({ url: '/pages/profile/profile?id=2' })
			},
			goToBuyerProfile() {
				uni.navigateTo({ url: '/pages/profile/profile?id=3' })
			},
			contactBuyer() {
				uni.navigateTo({ url: '/pages/chat/chat?id=3' })
			},
			toggleCollect() {
				this.isCollected = !this.isCollected;
				uni.showToast({
					title: this.isCollected ? '已收藏' : '取消收藏',
					icon: 'none'
				});
			},
			handleAction(type) {
				if (type === '咨询') {
					uni.navigateTo({ url: '/pages/chat/chat?id=2' })
				} else {
					uni.navigateTo({ url: '/pages/buy/buy?id=' + this.goodsId })
				}
			},
			editGoods() {
				uni.navigateTo({ url: '/pages/goods-edit/goods-edit?id=' + this.goodsId })
			},
			cancelListing() {
				uni.showModal({
					title: '下架商品',
					content: '确认下架该商品？下架后其他用户将无法看到该商品。',
					success: (res) => {
						if (res.confirm) {
							this.goodsStatus = 'off'
							uni.showToast({ title: '已下架', icon: 'success' })
						}
					}
				})
			},
			relistGoods() {
				uni.showModal({
					title: '重新上架',
					content: '确认重新上架该商品？',
					success: (res) => {
						if (res.confirm) {
							this.goodsStatus = 'selling'
							uni.showToast({ title: '已上架', icon: 'success' })
						}
					}
				})
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

	.status-badge {
		position: absolute;
		top: 24rpx;
		right: 24rpx;
		font-size: 24rpx;
		padding: 8rpx 24rpx;
		border-radius: 30rpx;
	}
	.status-on { color: #3A6341; background: #e8f5ee; }
	.status-off { color: #999; background: rgba(0,0,0,0.5); color: #fff; }

	.tag-badge {
		background: #e8f5ee;
		color: #3A6341;
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
	font-size: 44rpx; flex-shrink: 0; overflow: hidden;
}
.avatar-img { width: 100%; height: 100%; border-radius: 50%; }
.avatar-emoji { font-size: 44rpx; }
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

	/* ===== 买家信息卡（卖家视角） ===== */
	.buyer-card {
		flex-direction: column;
		align-items: stretch;
	}
	.section-label {
		font-size: 24rpx;
		color: #999;
	}
	.buyer-contact {
		font-size: 24rpx;
		color: #999;
		margin-top: 6rpx;
	}
	.contact-buyer-btn {
		margin-top: 24rpx;
		height: 72rpx;
		line-height: 72rpx;
		text-align: center;
		background: #e8f5ee;
		color: #3A6341;
		font-size: 26rpx;
		font-weight: bold;
		border-radius: 36rpx;
	}

	.detail-section {
		background: white;
		margin: 0 20rpx 20rpx;
		padding: 30rpx;
		border-radius: 20rpx;
	}
	.section-title { font-size: 28rpx; color: #3A6341; font-weight: bold; margin-bottom: 20rpx; letter-spacing: 2rpx; }
	.description { font-size: 30rpx; color: #444; line-height: 1.8; }

	.tags-section { display: flex; gap: 16rpx; padding: 0 20rpx 20rpx; flex-wrap: wrap; }
	.tag { background: #e8f5ee; color: #3A6341; font-size: 24rpx; padding: 10rpx 24rpx; border-radius: 30rpx; }

	.bottom-action {
		position: fixed; bottom: 0; left: 0; right: 0;
		height: 120rpx; background: white;
		display: flex; align-items: center;
		padding: 0 30rpx;
		border-top: 1rpx solid #f0f0f0;
		box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
	}
	.icon-group { width: 80rpx; display: flex; align-items: center; justify-content: center; margin-right: 20rpx; }
	.collect-icon { font-size: 48rpx; color: #ccc; transition: all 0.3s; }
	.icon-xz { color: #E85A4F; }
	.btn-group { flex: 1; display: flex; gap: 16rpx; justify-content: flex-end; }
	.chat-btn {
		background: #e8f5ee; color: #3A6341;
		font-size: 28rpx; font-weight: bold;
		width: 180rpx; height: 80rpx; line-height: 80rpx;
		border-radius: 40rpx; margin: 0; border: none;
	}
	.buy-btn {
		background: #3A6341; color: white;
		font-size: 28rpx; font-weight: bold;
		width: 220rpx; height: 80rpx; line-height: 80rpx;
		border-radius: 40rpx; margin: 0; border: none;
	}

	/* ===== 卖家底部操作栏 ===== */
	.seller-bottom {
		justify-content: center;
		gap: 20rpx;
	}
	.edit-btn {
		flex: 1;
		background: #e8f5ee; color: #3A6341;
		font-size: 28rpx; font-weight: bold;
		height: 80rpx; line-height: 80rpx;
		border-radius: 40rpx; margin: 0; border: none;
	}
	.cancel-btn {
		flex: 1;
		background: #f5f5f5; color: #3f3f3f;
		font-size: 28rpx; font-weight: bold;
		height: 80rpx; line-height: 80rpx;
		border-radius: 40rpx; margin: 0; border: none;
	}
</style>

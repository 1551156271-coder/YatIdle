<template>
	<view class="container">
		<!-- 图片轮播 -->
		<view class="image-box">
			<swiper class="image-swiper" indicator-dots="false" autoplay="false" :current="currentIndex" @change="onSwiperChange">
				<swiper-item v-for="(img, idx) in item.imageUrls" :key="idx" @click="previewImage(idx)">
					<image class="swiper-image" :src="img" mode="aspectFill"></image>
				</swiper-item>
			</swiper>
			<!-- 计数器 -->
			<view v-if="item.imageUrls.length > 1" class="image-counter">{{ currentIndex + 1 }} / {{ item.imageUrls.length }}</view>
			<!-- 状态徽章 -->
			<view v-if="isSeller" class="status-badge" :class="statusClass">{{ statusText }}</view>
		</view>

		<view class="info-card">
			<view class="price-row">
				<text class="symbol">￥</text>
				<text class="price">{{ item.price }}</text>
				<text class="tag-badge" v-if="item.conditionLevel">{{ item.conditionLevel }}</text>
			</view>
			<view class="title">{{ item.title }}</view>
			<view class="meta-row">
				<view class="meta-item">📦 自提</view>
				<view class="meta-item">📍 {{ item.campus }}</view>
				<view class="meta-item">🕐 {{ item.createTime }}</view>
			</view>
		</view>

		<!-- 买家模式：卖家信息卡 -->
		<view v-if="!isSeller" class="seller-card" @click="goToProfile">
			<view class="seller-left">
				<view class="avatar">
					<image v-if="sellerInfo.avatar" class="avatar-img" :src="sellerInfo.avatar" mode="aspectFill"></image>
					<text v-else class="avatar-emoji">🎓</text>
				</view>
				<view class="seller-text">
					<view class="name-row">
						<text class="seller-name">{{ sellerInfo.username || '卖家' }}</text>
						<text v-if="sellerInfo.verified" class="verified-badge">已认证</text>
					</view>
				</view>
			</view>
		</view>

		<view class="detail-section">
			<view class="section-title">商品详情</view>
			<text class="description">{{ item.description || '暂无描述' }}</text>
		</view>

		<!-- 底部操作栏：买家模式 -->
		<view v-if="!isSeller" class="bottom-action">
			<view class="icon-group" @click="toggleCollect">
				<text class="collect-icon iconfont" :class="isCollected ? 'icon-xz' : 'icon-shoucang'"></text>
			</view>
			<view class="btn-group">
				<button class="chat-btn" @click="handleConsult">💬 咨询</button>
				<button class="buy-btn" @click="handleBuy">立即购买</button>
			</view>
		</view>

		<!-- 底部操作栏：卖家模式 - 在售 -->
		<view v-if="isSeller && itemStatus === 'ON_SALE'" class="bottom-action seller-bottom">
			<button class="edit-btn" @click="editGoods">编辑信息</button>
			<button class="cancel-btn" @click="offlineGoods">下架商品</button>
		</view>

		<!-- 底部操作栏：卖家模式 - 已下架 -->
		<view v-if="isSeller && itemStatus === 'REMOVED'" class="bottom-action seller-bottom">
			<button class="buy-btn" style="flex:1;" @click="relistGoods">重新上架</button>
		</view>

		</view>
</template>

<script>
	import { getItemDetail, offlineItem, onlineItem } from '@/api/item.js'
	import { addFavorite, removeFavorite } from '@/api/favorite.js'
	import { createSession } from '@/api/chat.js'
	import { getUserInfo } from '@/api/user.js'

	export default {
		data() {
			return {
				goodsId: null,
				currentIndex: 0,
				item: {
					title: '',
					price: 0,
					description: '',
					campus: '',
					conditionLevel: '',
					imageUrls: [],
					userId: null,
					status: 'ON_SALE',
					createTime: ''
				},
				isCollected: false,
				isSeller: false,
				itemStatus: 'ON_SALE',
				sellerInfo: {}
			}
		},
		computed: {
			statusText() {
				const map = { ON_SALE: '在售', SOLD: '已售', REMOVED: '已下架' }
				return map[this.itemStatus] || this.itemStatus
			},
			statusClass() {
				return this.itemStatus === 'ON_SALE' ? 'status-on' : 'status-off'
			}
		},
		onLoad(options) {
			if (options.mode === 'seller') this.isSeller = true
			if (options.status) {
				const statusMap = { '在售': 'ON_SALE', '已售': 'SOLD', '已下架': 'REMOVED' }
				this.itemStatus = statusMap[options.status] || 'ON_SALE'
			}
			if (options.id) {
				this.goodsId = options.id
				this.loadItem()
			}
		},
		methods: {
			async loadItem() {
				try {
					const data = await getItemDetail(this.goodsId)
					this.item = data
					this.itemStatus = data.status || 'ON_SALE'
					const user = uni.getStorageSync('user')
					if (user && user.id === data.userId) {
						this.isSeller = true
					}
					if (data.userId) {
						this.loadSellerInfo(data.userId)
					}
				} catch (e) {
					uni.showToast({ title: '加载商品失败', icon: 'none' })
				}
			},
			async loadSellerInfo(userId) {
				try {
					this.sellerInfo = await getUserInfo(userId)
				} catch (e) {}
			},
			goToProfile() {
				if (this.item.userId) {
					uni.navigateTo({ url: '/pages/profile/profile?id=' + this.item.userId })
				}
			},
			onSwiperChange(e) {
				this.currentIndex = e.detail.current
			},
			previewImage(idx) {
				uni.previewImage({
					urls: this.item.imageUrls,
					current: idx
				})
			},
			async toggleCollect() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return
				}
				try {
					if (this.isCollected) {
						await removeFavorite(this.goodsId, user.id)
						this.isCollected = false
						uni.showToast({ title: '已取消收藏', icon: 'none' })
					} else {
						await addFavorite(this.goodsId, user.id)
						this.isCollected = true
						uni.showToast({ title: '已收藏', icon: 'none' })
					}
				} catch (e) {}
			},
			async handleConsult() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return
				}
				try {
					const session = await createSession(user.id, { itemId: Number(this.goodsId) })
					uni.navigateTo({ url: '/pages/chat/chat?id=' + session.id })
				} catch (e) {
					uni.navigateTo({ url: '/pages/chat/chat?id=' + this.goodsId })
				}
			},
			handleBuy() {
				uni.navigateTo({ url: '/pages/buy/buy?id=' + this.goodsId })
			},
			editGoods() {
				uni.navigateTo({ url: '/pages/goods-edit/goods-edit?id=' + this.goodsId })
			},
			async offlineGoods() {
				uni.showModal({
					title: '下架商品',
					content: '确认下架该商品？下架后其他用户将无法看到该商品。',
					success: async (res) => {
						if (res.confirm) {
							try {
								const user = uni.getStorageSync('user')
							await offlineItem(this.goodsId, user ? user.id : 0)
							this.itemStatus = 'REMOVED'
								uni.showToast({ title: '已下架', icon: 'success' })
							} catch (e) {}
						}
					}
				})
			},
			async relistGoods() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) return
				try {
					await onlineItem(this.goodsId, user.id)
					this.itemStatus = 'ON_SALE'
					uni.showToast({ title: '已重新上架', icon: 'success' })
				} catch (e) {}
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
		background: #000;
	}
	.image-swiper { width: 100%; height: 100%; }
	.swiper-image { width: 100%; height: 100%; }

	.image-counter {
		position: absolute;
		bottom: 24rpx;
		right: 24rpx;
		background: rgba(0,0,0,0.45);
		color: #fff;
		font-size: 22rpx;
		padding: 4rpx 16rpx;
		border-radius: 20rpx;
	}

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

	.detail-section {
		background: white;
		margin: 0 20rpx 20rpx;
		padding: 30rpx;
		border-radius: 20rpx;
	}
	.section-title { font-size: 28rpx; color: #3A6341; font-weight: bold; margin-bottom: 20rpx; letter-spacing: 2rpx; }
	.description { font-size: 30rpx; color: #444; line-height: 1.8; }

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

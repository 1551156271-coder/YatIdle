<template>
	<view class="my-page">
		<!-- ========== 侧边栏遮罩 ========== -->
		<view v-if="showSidebar" class="sidebar-overlay" @click="closeSidebar" @touchmove.stop.prevent></view>

		<!-- ========== 侧边栏面板 ========== -->
		<view class="sidebar-panel" :class="{ 'sidebar-open': showSidebar }" @touchmove.stop.prevent>
			<view class="sidebar-header">
				<text class="sidebar-title">更多</text>
				<text class="sidebar-close" @click="closeSidebar">✕</text>
			</view>
			<view class="sidebar-menu">
				<view class="sidebar-item" @click="onSidebarClick('浏览历史')">
					<text class="si-icon">🕐</text>
					<text class="si-text">浏览历史</text>
					<text class="si-arrow">›</text>
				</view>
				<view class="sidebar-item" @click="onSidebarClick('认证中心')">
					<text class="si-icon">✅</text>
					<text class="si-text">认证中心</text>
					<text class="si-arrow">›</text>
				</view>
				<view class="sidebar-item" @click="onSidebarClick('设置')">
					<text class="si-icon">⚙️</text>
					<text class="si-text">设置</text>
					<text class="si-arrow">›</text>
				</view>
				<view class="sidebar-item" @click="onSidebarClick('关于')">
					<text class="si-icon">ℹ️</text>
					<text class="si-text">关于闲鸭蛋</text>
					<text class="si-arrow">›</text>
				</view>
			</view>
		</view>

		<!-- ========== 头部 ========== -->
		<view class="header-card">
			<view class="header-bg">
				<view class="hamburger-btn" @click="openSidebar">
					<view class="hb-line"></view>
					<view class="hb-line"></view>
					<view class="hb-line"></view>
				</view>
			</view>
			<view class="header-content">
				<!-- 头像 -->
				<view class="avatar-wrap" @click="userInfo.isLogin ? goEditProfile() : goToLogin()">
					<image v-if="userInfo.avatar" class="avatar-img" :src="userInfo.avatar" mode="aspectFill"></image>
					<text v-else class="avatar-emoji">🎓</text>
				</view>

				<!-- 昵称 + 编辑按钮 -->
				<view class="name-row" @click="userInfo.isLogin ? goEditProfile() : goToLogin()">
					<text class="user-name">{{ userInfo.isLogin ? userInfo.nickname : '点击登录' }}</text>
					<text v-if="userInfo.isLogin" class="edit-pen">✎</text>
				</view>

				<!-- 标签 -->
				<view class="user-tags" v-if="userInfo.isLogin">
					<text class="u-tag">{{ userInfo.campus }}</text>
					<text v-if="userInfo.verified" class="u-tag u-tag-verified">已认证</text>
				</view>
				<view class="user-tags" v-else>
					<text class="u-tag u-tag-dim">登录后解锁更多</text>
				</view>
			</view>
		</view>

		<!-- ========== 未登录占位 ========== -->
		<view v-if="!userInfo.isLogin" class="login-hint-card">
			<text class="login-hint-icon">🔒</text>
			<text class="login-hint-text">登录后可查看交易与信用信息</text>
			<view class="login-hint-btn" @click="goToLogin">立即登录</view>
		</view>

		<!-- ========== 已登录卡片区 ========== -->
		<block v-else>
			<!-- 卡片1: 我的交易 -->
			<view class="card">
				<view class="card-head">
					<text class="card-title">我的交易</text>
				</view>
				<view class="trade-row">
					<view class="trade-item" @click="goToListings">
						<text class="trade-num">{{ userInfo.publishCount }}</text>
						<text class="trade-label">发布</text>
					</view>
					<view class="trade-item" @click="goToOrders('sold')">
						<text class="trade-num">{{ userInfo.soldCount }}</text>
						<text class="trade-label">已售</text>
					</view>
					<view class="trade-item" @click="goToOrders('purchased')">
						<text class="trade-num">{{ userInfo.purchasedCount }}</text>
						<text class="trade-label">已购</text>
					</view>
				</view>
			</view>

			<!-- 卡片2: 我的信用 -->
			<view class="card card-credit" @click="goToCredit">
				<view class="card-head">
					<text class="card-title">我的信用</text>
					<text class="card-more">详情 ›</text>
				</view>
				<view class="credit-row">
					<view class="credit-main">
						<text class="credit-score" :class="creditLevel">{{ userInfo.creditScore }}</text>
						<text class="credit-desc">{{ creditDesc }}</text>
					</view>
					<view class="credit-stats">
						<view class="cs-item">
							<text class="cs-num">{{ userInfo.dealCount }}</text>
							<text class="cs-label">成交</text>
						</view>
						<view class="cs-item">
							<text class="cs-num">{{ userInfo.goodsCount }}</text>
							<text class="cs-label">在售</text>
						</view>
						<view class="cs-item">
							<text class="cs-num">{{ userInfo.reviewCount }}</text>
							<text class="cs-label">评价</text>
						</view>
					</view>
				</view>
			</view>

			<!-- 卡片3: 心愿单 -->
			<view class="card card-wish" @click="goToWishlist">
				<view class="card-head">
					<text class="card-title"><text class="iconfont icon-xinyuandan" style="font-size: 32rpx; margin-right: 4rpx;"></text>心愿单</text>
					<text class="card-more">{{ userInfo.wishCount }} 件商品 ›</text>
				</view>
				<view v-if="userInfo.wishCount > 0" class="wish-preview">
					<text class="wish-hint">点击查看你收藏的宝贝</text>
				</view>
				<view v-else class="wish-empty">
					<text class="wish-empty-text">暂无收藏，去逛逛吧</text>
				</view>
			</view>
		</block>
	</view>
	<tab-bar />
</template>

<script>
	import TabBar from '@/components/tab-bar.vue'
	export default {
		components: { TabBar },
		data() {
			// TODO: 开发完后删掉 mock，改回 uni.getStorageSync('user')
	const user = uni.getStorageSync('user') || { username: '测试用户', campus: '东校园', verified: true, creditScore: 85, publishCount: 3, soldCount: 5, purchasedCount: 2, dealCount: 7, goodsCount: 3, reviewCount: 12, wishCount: 4 }
			return {
				showSidebar: false,
				userInfo: user ? {
					isLogin: true,
					nickname: user.username || '',
					avatar: user.avatar || '',
					campus: user.campus || '',
					verified: user.verified || false,
					creditScore: user.creditScore || 0,
					publishCount: user.publishCount || 0,
					soldCount: user.soldCount || 0,
					purchasedCount: user.purchasedCount || 0,
					dealCount: user.dealCount || 0,
					goodsCount: user.goodsCount || 0,
					reviewCount: user.reviewCount || 0,
					wishCount: user.wishCount || 0
				} : {
					isLogin: false,
					nickname: '',
					avatar: '',
					campus: '',
					verified: false,
					creditScore: 0,
					publishCount: 0,
					soldCount: 0,
					purchasedCount: 0,
					dealCount: 0,
					goodsCount: 0,
					reviewCount: 0,
					wishCount: 0
				}
			}
		},
		onShow() {
			uni.hideTabBar()
			// TODO: 开发完后删掉 mock，改回 uni.getStorageSync('user')
	const user = uni.getStorageSync('user') || { username: '测试用户', campus: '东校园', verified: true, creditScore: 85, publishCount: 3, soldCount: 5, purchasedCount: 2, dealCount: 7, goodsCount: 3, reviewCount: 12, wishCount: 4 }
			if (user) {
				this.userInfo.isLogin = true
				this.userInfo.nickname = user.username || ''
				this.userInfo.avatar = user.avatar || ''
				this.userInfo.campus = user.campus || ''
				this.userInfo.verified = user.verified || false
				this.userInfo.creditScore = user.creditScore || 0
				this.userInfo.publishCount = user.publishCount || 0
				this.userInfo.soldCount = user.soldCount || 0
				this.userInfo.purchasedCount = user.purchasedCount || 0
				this.userInfo.dealCount = user.dealCount || 0
				this.userInfo.goodsCount = user.goodsCount || 0
				this.userInfo.reviewCount = user.reviewCount || 0
				this.userInfo.wishCount = user.wishCount || 0
			}
		},
		computed: {
			creditLevel() {
				const s = this.userInfo.creditScore
				if (s >= 90) return 'credit-high'
				if (s >= 70) return 'credit-mid'
				return 'credit-low'
			},
			creditDesc() {
				const s = this.userInfo.creditScore
				if (s >= 90) return '信用极好'
				if (s >= 70) return '信用良好'
				return '信用较差'
			}
		},
		methods: {
			openSidebar() {
				this.showSidebar = true
			},
			closeSidebar() {
				this.showSidebar = false
			},
			onSidebarClick(name) {
				this.showSidebar = false
				uni.showToast({ title: name + ' 即将上线', icon: 'none' })
			},
			goToLogin() {
				uni.navigateTo({ url: '/pages/login/login' })
			},
			goEditProfile() {
				uni.navigateTo({ url: '/pages/my-edit/my-edit' })
			},
			goToListings() {
				uni.navigateTo({ url: '/pages/my-listings/my-listings' })
			},
			goToOrders(type) {
				uni.navigateTo({ url: '/pages/my-orders/my-orders?type=' + type })
			},
			goToCredit() {
				uni.navigateTo({ url: '/pages/my-credit/my-credit' })
			},
			goToWishlist() {
				uni.navigateTo({ url: '/pages/my-wishlist/my-wishlist' })
			}
		}
	}
</script>

<style>
	.my-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
		padding-bottom: 20rpx;
	}

	/* ===== 侧边栏遮罩 ===== */
	.sidebar-overlay {
		position: fixed; top: 0; left: 0; right: 0; bottom: 0;
		background: rgba(0,0,0,0.45);
		z-index: 998;
	}

	/* ===== 侧边栏面板 ===== */
	.sidebar-panel {
		position: fixed; top: 0; left: 0; bottom: 0;
		width: 540rpx;
		background: #ffffff;
		z-index: 999;
		transform: translateX(-100%);
		transition: transform 0.25s ease;
		display: flex; flex-direction: column;
	}
	.sidebar-open { transform: translateX(0); }

	.sidebar-header {
		display: flex; align-items: center; justify-content: space-between;
		padding: 36rpx 30rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
	}
	.sidebar-title { font-size: 36rpx; color: #ffffff; font-weight: bold; }
	.sidebar-close { font-size: 36rpx; color: rgba(255,255,255,0.8); padding: 8rpx; }

	.sidebar-menu { flex: 1; padding-top: 16rpx; }
	.sidebar-item {
		display: flex; align-items: center;
		padding: 32rpx 30rpx;
		border-bottom: 1rpx solid #f5f5f5;
	}
	.sidebar-item:active { background: #f9f9f9; }
	.si-icon { font-size: 36rpx; margin-right: 24rpx; }
	.si-text { flex: 1; font-size: 30rpx; color: #333; }
	.si-arrow { font-size: 32rpx; color: #ccc; }

	/* ===== 头部 ===== */
	.header-card { position: relative; }
	.header-bg {
		height: 200rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		position: relative;
	}

	/* 汉堡按钮 */
	.hamburger-btn {
		position: absolute; top: 30rpx; left: 30rpx;
		width: 52rpx; height: 44rpx;
		display: flex; flex-direction: column; justify-content: space-between;
		padding: 8rpx 6rpx;
		z-index: 10;
	}
	.hb-line {
		width: 100%; height: 4rpx;
		background: #ffffff; border-radius: 2rpx;
	}

	.header-content {
		background: #ffffff;
		margin: -60rpx 20rpx 0;
		border-radius: 20rpx;
		padding: 0 30rpx 36rpx;
		display: flex; flex-direction: column; align-items: center;
		box-shadow: 0 4rpx 20rpx rgba(0,0,0,0.06);
		position: relative;
	}

	.avatar-wrap {
		width: 140rpx; height: 140rpx;
		border-radius: 50%;
		background: #e8f5ee;
		margin-top: -70rpx;
		display: flex; align-items: center; justify-content: center;
		border: 6rpx solid #ffffff;
		box-shadow: 0 4rpx 16rpx rgba(0,0,0,0.08);
		overflow: hidden;
	}
	.avatar-img { width: 100%; height: 100%; border-radius: 50%; }
	.avatar-emoji { font-size: 60rpx; }

	/* 昵称 */
	.name-row {
		display: flex; align-items: center; gap: 10rpx;
		margin-top: 20rpx;
	}
	.user-name { font-size: 36rpx; color: #333; font-weight: bold; }
	.edit-pen { font-size: 28rpx; color: #999; }

	/* 标签 */
	.user-tags { display: flex; gap: 12rpx; margin-top: 14rpx; }
	.u-tag {
		font-size: 22rpx; color: #3A6341; background: #e8f5ee;
		padding: 6rpx 20rpx; border-radius: 20rpx;
	}
	.u-tag-verified { color: #1565C0; background: #e3f2fd; }
	.u-tag-dim { color: #999; background: #f0f0f0; }

	/* ===== 未登录占位 ===== */
	.login-hint-card {
		background: #ffffff; margin: 20rpx; border-radius: 20rpx;
		padding: 60rpx 30rpx;
		display: flex; flex-direction: column; align-items: center;
		box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
	}
	.login-hint-icon { font-size: 64rpx; margin-bottom: 20rpx; }
	.login-hint-text { font-size: 28rpx; color: #999; margin-bottom: 30rpx; }
	.login-hint-btn {
		font-size: 28rpx; color: #ffffff;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		padding: 16rpx 60rpx; border-radius: 44rpx;
	}

	/* ===== 通用卡片 ===== */
	.card {
		background: #ffffff; margin: 20rpx; border-radius: 20rpx;
		padding: 24rpx 24rpx 20rpx;
		box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
	}
	.card-head {
		display: flex; justify-content: space-between; align-items: center;
		margin-bottom: 24rpx;
	}
	.card-title { font-size: 30rpx; color: #333; font-weight: bold; }
	.card-more { font-size: 24rpx; color: #999; }

	/* ===== 交易卡片 ===== */
	.trade-row { display: flex; justify-content: space-around; }
	.trade-item {
		display: flex; flex-direction: column; align-items: center;
		padding: 10rpx 30rpx;
	}
	.trade-num { font-size: 40rpx; color: #3A6341; font-weight: bold; margin-bottom: 6rpx; }
	.trade-icon { font-size: 40rpx; margin-bottom: 6rpx; }
	.trade-label { font-size: 24rpx; color: #999; }

	/* ===== 信用卡片 ===== */
	.credit-row {
		display: flex; align-items: center;
	}
	.credit-main {
		display: flex; flex-direction: column; align-items: center;
		padding-right: 30rpx; border-right: 1rpx solid #f0f0f0; margin-right: 30rpx;
	}
	.credit-score { font-size: 64rpx; font-weight: bold; line-height: 1; margin-bottom: 6rpx; }
	.credit-high { color: #4cd964; }
	.credit-mid { color: #f0ad4e; }
	.credit-low { color: #e74c3c; }
	.credit-desc { font-size: 22rpx; color: #999; }

	.credit-stats { flex: 1; display: flex; justify-content: space-around; }
	.cs-item { display: flex; flex-direction: column; align-items: center; }
	.cs-num { font-size: 36rpx; color: #333; font-weight: bold; }
	.cs-label { font-size: 22rpx; color: #999; margin-top: 4rpx; }

	/* ===== 心愿单卡片 ===== */
	.wish-preview { text-align: center; padding: 10rpx 0; }
	.wish-hint { font-size: 26rpx; color: #999; }
	.wish-empty { text-align: center; padding: 10rpx 0; }
	.wish-empty-text { font-size: 26rpx; color: #ccc; }
</style>

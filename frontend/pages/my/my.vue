<template>
	<view class="my-page">
		<view class="profile-card">
			<view class="profile-top">
				<view class="avatar-box" @click="changeAvatar">
					<image v-if="userInfo.avatar" class="avatar-img" :src="userInfo.avatar" mode="aspectFill"></image>
					<text v-else class="avatar-text">{{ userInfo.defaultAvatar }}</text>
					<view class="avatar-camera"><text class="camera-icon">📷</text></view>
				</view>
				<view class="profile-info">
					<!-- 编辑昵称内联模式 -->
					<view v-if="editingNickname" class="nickname-edit-row">
						<input
							class="nickname-input"
							v-model="nicknameDraft"
							:placeholder="userInfo.nickname"
							maxlength="16"
							:focus="true"
							@blur="saveNickname"
							@confirm="saveNickname"
						/>
						<text class="nickname-confirm" @click="saveNickname">确定</text>
					</view>
					<view v-else class="nickname-row" @click="editNickname">
						<text class="nickname">{{ userInfo.isLogin ? userInfo.nickname : '点击登录' }}</text>
						<text v-if="userInfo.isLogin" class="edit-hint">✎</text>
					</view>
					<text class="school-badge">中山大学</text>
				</view>
			</view>
		</view>

		<view class="stats-row">
			<view class="stat-item">
				<text class="stat-num">{{ userInfo.publishCount }}</text>
				<text class="stat-label">发布</text>
			</view>
			<view class="stat-item">
				<text class="stat-num">{{ userInfo.favCount }}</text>
				<text class="stat-label">收藏</text>
			</view>
			<view class="stat-item">
				<text class="stat-num">{{ userInfo.soldCount }}</text>
				<text class="stat-label">已售</text>
			</view>
			<view class="stat-item">
				<text class="stat-num">{{ userInfo.rateCount }}</text>
				<text class="stat-label">评价</text>
			</view>
		</view>

		<view class="menu-section">
			<view class="menu-item" @click="onMenuClick('我的发布')">
				<text class="menu-icon">📦</text>
				<text class="menu-text">我的发布</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" @click="onMenuClick('我的收藏')">
				<text class="menu-icon">❤️</text>
				<text class="menu-text">我的收藏</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" @click="onMenuClick('浏览历史')">
				<text class="menu-icon">🕐</text>
				<text class="menu-text">浏览历史</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" @click="onMenuClick('认证中心')">
				<text class="menu-icon">✅</text>
				<text class="menu-text">认证中心</text>
				<text class="menu-arrow">›</text>
			</view>
		</view>

		<view class="menu-section">
			<view class="menu-item" @click="onMenuClick('设置')">
				<text class="menu-icon">⚙️</text>
				<text class="menu-text">设置</text>
				<text class="menu-arrow">›</text>
			</view>
			<view class="menu-item" @click="onMenuClick('关于')">
				<text class="menu-icon">ℹ️</text>
				<text class="menu-text">关于闲鸭蛋</text>
				<text class="menu-arrow">›</text>
			</view>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				editingNickname: false,
				nicknameDraft: '',
				userInfo: {
					isLogin: false,
					nickname: '点击登录',
					avatar: '',
					defaultAvatar: '🎓',
					publishCount: 0,
					favCount: 0,
					soldCount: 0,
					rateCount: 0
				}
			}
		},
		methods: {
			goToLogin() {
				uni.navigateTo({
					url: '/pages/login/login'
				})
			},
			changeAvatar() {
				if (!this.userInfo.isLogin) {
					this.goToLogin()
					return
				}
				uni.chooseImage({
					count: 1,
					sizeType: ['compressed'],
					sourceType: ['album', 'camera'],
					success: (res) => {
						this.userInfo.avatar = res.tempFilePaths[0]
						uni.showToast({ title: '头像已更新', icon: 'none' })
					}
				})
			},
			editNickname() {
				if (!this.userInfo.isLogin) {
					this.goToLogin()
					return
				}
				this.nicknameDraft = this.userInfo.nickname
				this.editingNickname = true
			},
			saveNickname() {
				const val = (this.nicknameDraft || '').trim()
				if (val && val !== this.userInfo.nickname) {
					this.userInfo.nickname = val
					uni.showToast({ title: '昵称已更新', icon: 'none' })
				}
				this.editingNickname = false
				this.nicknameDraft = ''
			},
			onMenuClick(name) {
				uni.showToast({ title: name + ' 即将上线', icon: 'none' })
			}
		}
	}
</script>

<style>
	.my-page { min-height: 100vh; width: 100%; background: #f5f5f5; overflow: hidden; box-sizing: border-box; }

	.profile-card {
		background: linear-gradient(135deg, #00613C, #00804B);
		padding: 60rpx 30rpx 40rpx;
	}
	.profile-top { display: flex; align-items: center; }
	.avatar-box {
		width: 120rpx; height: 120rpx;
		background: rgba(255,255,255,0.2); border-radius: 50%;
		display: flex; align-items: center; justify-content: center;
		border: 4rpx solid rgba(255,255,255,0.4); margin-right: 24rpx;
		position: relative; overflow: visible;
	}
	.avatar-img { width: 100%; height: 100%; border-radius: 50%; }
	.avatar-text { font-size: 56rpx; }
	.avatar-camera {
		position: absolute; bottom: -6rpx; right: -6rpx;
		width: 40rpx; height: 40rpx;
		background: #ffffff; border-radius: 50%;
		display: flex; align-items: center; justify-content: center;
		box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.15);
	}
	.camera-icon { font-size: 22rpx; }

	.profile-info { display: flex; flex-direction: column; }
	.nickname-row { display: flex; align-items: center; gap: 8rpx; margin-bottom: 8rpx; }
	.nickname { font-size: 36rpx; color: #ffffff; font-weight: bold; }
	.edit-hint { font-size: 24rpx; color: rgba(255,255,255,0.6); }

	/* 昵称内联编辑 */
	.nickname-edit-row {
		display: flex; align-items: center; gap: 12rpx; margin-bottom: 8rpx;
	}
	.nickname-input {
		width: 260rpx; height: 56rpx;
		background: rgba(255,255,255,0.25); border-radius: 8rpx;
		padding: 0 16rpx; font-size: 30rpx; color: #ffffff;
		box-sizing: border-box;
		border: 1rpx solid rgba(255,255,255,0.5);
	}
	.nickname-confirm {
		font-size: 26rpx; color: #ffffff;
		padding: 8rpx 20rpx; background: rgba(255,255,255,0.2); border-radius: 6rpx;
	}

	.school-badge {
		font-size: 24rpx; color: rgba(255,255,255,0.85);
		background: rgba(255,255,255,0.15); padding: 4rpx 16rpx; border-radius: 6rpx;
		display: inline-block; width: fit-content;
	}

	.stats-row {
		background: #ffffff; margin: 20rpx; border-radius: 16rpx;
		display: flex; padding: 24rpx 0;
	}
	.stat-item { flex: 1; display: flex; flex-direction: column; align-items: center; }
	.stat-num { font-size: 40rpx; color: #00613C; font-weight: bold; margin-bottom: 8rpx; }
	.stat-label { font-size: 24rpx; color: #999; }

	.menu-section {
		background: #ffffff; margin: 0 20rpx 20rpx; border-radius: 16rpx; overflow: hidden;
	}
	.menu-item {
		display: flex; align-items: center; padding: 28rpx 24rpx;
		border-bottom: 1rpx solid #f5f5f5;
	}
	.menu-item:last-child { border-bottom: none; }
	.menu-icon { font-size: 36rpx; margin-right: 20rpx; }
	.menu-text { flex: 1; font-size: 28rpx; color: #333; }
	.menu-arrow { font-size: 32rpx; color: #ccc; }
</style>

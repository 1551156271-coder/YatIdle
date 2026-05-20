<template>
	<view class="login-page">
		<!-- 顶部装饰区 -->
		<view class="header-section">
			<text class="app-name">闲鸭蛋</text>
			<text class="app-slogan">让闲置流动起来</text>
		</view>

		<!-- Tab 切换 -->
		<view class="tab-row">
			<view
				class="tab-item"
				:class="{ 'tab-active': activeTab === 'login' }"
				@click="switchTab('login')"
			>
				登录
			</view>
			<view
				class="tab-item"
				:class="{ 'tab-active': activeTab === 'register' }"
				@click="switchTab('register')"
			>
				注册
			</view>
		</view>

		<!-- 登录表单 -->
		<view v-if="activeTab === 'login'" class="form-card">
			<view class="form-item">
				<text class="form-label">用户名</text>
				<input
					class="form-input"
					v-model="loginForm.username"
					placeholder="请输入用户名"
				/>
			</view>

			<view class="form-item">
				<text class="form-label">密码</text>
				<input
					class="form-input"
					v-model="loginForm.password"
					:password="true"
					placeholder="请输入密码"
				/>
			</view>

			<button class="submit-btn" @click="onLogin" :disabled="submitting">登录</button>
		</view>

		<!-- 注册表单 -->
		<view v-if="activeTab === 'register'" class="form-card">
			<view class="form-item">
				<text class="form-label">用户名</text>
				<input
					class="form-input"
					v-model="registerForm.username"
					placeholder="请输入用户名"
					maxlength="20"
				/>
			</view>

			<view class="form-item">
				<text class="form-label">密码</text>
				<input
					class="form-input"
					v-model="registerForm.password"
					:password="true"
					placeholder="请输入密码"
				/>
			</view>

			<view class="form-item">
				<text class="form-label">确认密码</text>
				<input
					class="form-input"
					v-model="registerForm.confirmPassword"
					:password="true"
					placeholder="请再次输入密码"
				/>
			</view>

			<button class="submit-btn" @click="onRegister" :disabled="submitting">注册</button>
		</view>
	</view>
</template>

<script>
import { login, register } from '../../api/user.js'

export default {
	data() {
		return {
			activeTab: 'login',
			submitting: false,
			loginForm: {
				username: '',
				password: ''
			},
			registerForm: {
				username: '',
				password: '',
				confirmPassword: ''
			}
		}
	},
	methods: {
		switchTab(tab) {
			this.activeTab = tab
		},
		redirectAfterAuth() {
			const pages = getCurrentPages()
			if (pages.length > 1) {
				uni.navigateBack()
			} else {
				uni.switchTab({
					url: '/pages/my/my'
				})
			}
		},
		onLogin() {
			const { username, password } = this.loginForm
			if (!username.trim()) {
				uni.showToast({ title: '请输入用户名', icon: 'none' })
				return
			}
			if (!password) {
				uni.showToast({ title: '请输入密码', icon: 'none' })
				return
			}

			this.submitting = true
			login(username.trim(), password)
				.then((res) => {
					uni.setStorageSync('token', res.token)
					uni.setStorageSync('user', res.user)
					uni.showToast({ title: '登录成功', icon: 'none' })
					setTimeout(() => {
						this.redirectAfterAuth()
					}, 800)
				})
				.catch(() => {})
				.finally(() => {
					this.submitting = false
				})
		},
		onRegister() {
			const { username, password, confirmPassword } = this.registerForm
			if (!username.trim()) {
				uni.showToast({ title: '请输入用户名', icon: 'none' })
				return
			}
			if (!password) {
				uni.showToast({ title: '请输入密码', icon: 'none' })
				return
			}
			if (password !== confirmPassword) {
				uni.showToast({ title: '两次密码不一致', icon: 'none' })
				return
			}

			this.submitting = true
			register(username.trim(), password)
				.then((res) => {
					uni.setStorageSync('token', res.token)
					uni.setStorageSync('user', res.user)
					uni.showToast({ title: '注册成功', icon: 'none' })
					setTimeout(() => {
						this.redirectAfterAuth()
					}, 800)
				})
				.catch(() => {})
				.finally(() => {
					this.submitting = false
				})
		}
	}
}
</script>

<style>
.login-page {
	min-height: 100vh;
	width: 100%;
	background: #f5f5f5;
	padding-bottom: 80rpx;
	box-sizing: border-box;
	overflow: hidden;
}

/* ===== 顶部装饰 ===== */
.header-section {
	background: linear-gradient(135deg, #3A6341, #4E7D56);
	padding: 80rpx 0 60rpx;
	display: flex;
	flex-direction: column;
	align-items: center;
}
.app-name {
	font-size: 56rpx;
	color: #ffffff;
	font-weight: bold;
	letter-spacing: 8rpx;
	margin-bottom: 12rpx;
}
.app-slogan {
	font-size: 26rpx;
	color: rgba(255, 255, 255, 0.8);
}

/* ===== Tab ===== */
.tab-row {
	display: flex;
	background: #ffffff;
	margin: 20rpx 20rpx 0;
	border-radius: 16rpx 16rpx 0 0;
	overflow: hidden;
}
.tab-item {
	flex: 1;
	text-align: center;
	font-size: 30rpx;
	color: #666;
	padding: 28rpx 0;
	position: relative;
	transition: all 0.2s;
}
.tab-active {
	color: #3A6341;
	font-weight: bold;
}
.tab-active::after {
	content: '';
	position: absolute;
	bottom: 0;
	left: 50%;
	transform: translateX(-50%);
	width: 48rpx;
	height: 6rpx;
	background: #3A6341;
	border-radius: 3rpx;
}

/* ===== 表单 ===== */
.form-card {
	background: #ffffff;
	margin: 0 20rpx;
	padding: 40rpx 30rpx 50rpx;
	border-radius: 0 0 16rpx 16rpx;
	box-sizing: border-box;
}

.form-item {
	margin-bottom: 36rpx;
}
.form-label {
	display: block;
	font-size: 28rpx;
	color: #333;
	font-weight: bold;
	margin-bottom: 16rpx;
}
.form-input {
	width: 100%;
	height: 84rpx;
	background: #f5f5f5;
	border-radius: 12rpx;
	padding: 0 20rpx;
	font-size: 28rpx;
	box-sizing: border-box;
}

.submit-btn {
	width: 100%;
	height: 88rpx;
	line-height: 88rpx;
	background: linear-gradient(135deg, #3A6341, #4E7D56);
	color: #ffffff;
	font-size: 32rpx;
	font-weight: bold;
	border-radius: 44rpx;
	border: none;
	box-shadow: 0 8rpx 24rpx rgba(0, 97, 60, 0.3);
	margin-top: 20rpx;
}
</style>

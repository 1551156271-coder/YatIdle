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
				<text class="form-label">手机号</text>
				<input
					class="form-input"
					v-model="loginForm.phone"
					type="number"
					maxlength="11"
					placeholder="请输入手机号"
				/>
			</view>

			<view class="form-item">
				<text class="form-label">验证码</text>
				<view class="code-row">
					<input
						class="form-input code-input"
						v-model="loginForm.code"
						type="number"
						maxlength="6"
						placeholder="请输入验证码"
					/>
					<view class="code-btn" :class="{ 'code-sending': codeSending }" @click="sendCode('login')">
						{{ loginCodeText }}
					</view>
				</view>
			</view>

			<view class="agreement-row">
				<view class="checkbox-dot" :class="{ checked: loginAgreed }" @click="loginAgreed = !loginAgreed"></view>
				<text class="agreement-text">已阅读并同意</text>
				<text class="agreement-link" @click="showAgreement">《用户服务协议》</text>
				<text class="agreement-text">和</text>
				<text class="agreement-link" @click="showAgreement">《隐私政策》</text>
			</view>

			<button class="submit-btn" @click="onLogin">登录</button>
		</view>

		<!-- 注册表单 -->
		<view v-if="activeTab === 'register'" class="form-card">
			<!-- 头像上传 -->
			<view class="form-item avatar-item">
				<text class="form-label">头像</text>
				<view class="avatar-upload" @click="uploadAvatar">
					<image v-if="registerForm.avatar" class="avatar-preview" :src="registerForm.avatar" mode="aspectFill"></image>
					<view v-else class="avatar-placeholder">
						<text class="avatar-plus">+</text>
						<text class="avatar-hint">上传头像</text>
					</view>
				</view>
			</view>

			<view class="form-item">
				<text class="form-label">手机号</text>
				<input
					class="form-input"
					v-model="registerForm.phone"
					type="number"
					maxlength="11"
					placeholder="请输入手机号"
				/>
			</view>

			<view class="form-item">
				<text class="form-label">验证码</text>
				<view class="code-row">
					<input
						class="form-input code-input"
						v-model="registerForm.code"
						type="number"
						maxlength="6"
						placeholder="请输入验证码"
					/>
					<view class="code-btn" :class="{ 'code-sending': codeSending }" @click="sendCode('register')">
						{{ registerCodeText }}
					</view>
				</view>
			</view>

			<view class="form-item">
				<text class="form-label">昵称</text>
				<input
					class="form-input"
					v-model="registerForm.nickname"
					placeholder="给自己取个昵称吧"
					maxlength="16"
				/>
			</view>

			<view class="form-item">
				<text class="form-label">所在校区</text>
				<picker :range="campusList" @change="onCampusChange">
					<view class="picker-text">{{ registerForm.campus || '请选择校区' }}</view>
				</picker>
			</view>

			<view class="agreement-row">
				<view class="checkbox-dot" :class="{ checked: registerAgreed }" @click="registerAgreed = !registerAgreed"></view>
				<text class="agreement-text">已阅读并同意</text>
				<text class="agreement-link" @click="showAgreement">《用户服务协议》</text>
				<text class="agreement-text">和</text>
				<text class="agreement-link" @click="showAgreement">《隐私政策》</text>
			</view>

			<button class="submit-btn" @click="onRegister">注册</button>
		</view>

		<!-- 微信一键登录 -->
		<view class="wechat-section">
			<view class="divider-row">
				<view class="divider-line"></view>
				<text class="divider-text">其他方式</text>
				<view class="divider-line"></view>
			</view>
			<view class="wechat-btn" @click="onWechatLogin">
				<text class="wechat-icon">💬</text>
				<text class="wechat-text">微信一键登录</text>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			activeTab: 'login',
			loginForm: {
				phone: '',
				code: ''
			},
			registerForm: {
				phone: '',
				code: '',
				nickname: '',
				campus: '',
				avatar: ''
			},
			campusList: ['东校园', '南校园', '北校园', '珠海校区', '深圳校区'],
			loginAgreed: false,
			registerAgreed: false,
			codeSending: false,
			loginCountdown: 0,
			registerCountdown: 0,
			loginTimer: null,
			registerTimer: null
		}
	},
	computed: {
		loginCodeText() {
			return this.loginCountdown > 0 ? this.loginCountdown + 's 后重发' : '获取验证码'
		},
		registerCodeText() {
			return this.registerCountdown > 0 ? this.registerCountdown + 's 后重发' : '获取验证码'
		}
	},
	onUnload() {
		clearInterval(this.loginTimer)
		clearInterval(this.registerTimer)
	},
	methods: {
		switchTab(tab) {
			this.activeTab = tab
		},
		uploadAvatar() {
			uni.chooseImage({
				count: 1,
				sizeType: ['compressed'],
				sourceType: ['album', 'camera'],
				success: (res) => {
					this.registerForm.avatar = res.tempFilePaths[0]
				}
			})
		},
		sendCode(type) {
			if (this.codeSending) return

			const phone = type === 'login' ? this.loginForm.phone : this.registerForm.phone
			if (!phone || phone.length < 11) {
				uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
				return
			}

			this.codeSending = true
			const countdownKey = type === 'login' ? 'loginCountdown' : 'registerCountdown'
			const timerKey = type === 'login' ? 'loginTimer' : 'registerTimer'

			this[countdownKey] = 60
			this[timerKey] = setInterval(() => {
				if (this[countdownKey] <= 1) {
					clearInterval(this[timerKey])
					this[countdownKey] = 0
					this.codeSending = false
				} else {
					this[countdownKey]--
				}
			}, 1000)

			uni.showToast({ title: '验证码已发送', icon: 'none' })
		},
		onCampusChange(e) {
			this.registerForm.campus = this.campusList[e.detail.value]
		},
		onLogin() {
			if (!this.loginForm.phone || this.loginForm.phone.length < 11) {
				uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
				return
			}
			if (!this.loginForm.code || this.loginForm.code.length < 4) {
				uni.showToast({ title: '请输入验证码', icon: 'none' })
				return
			}
			if (!this.loginAgreed) {
				uni.showToast({ title: '请先阅读并同意服务协议', icon: 'none' })
				return
			}

			uni.showToast({ title: '登录成功', icon: 'none' })
			setTimeout(() => {
				uni.navigateBack()
			}, 800)
		},
		onRegister() {
			if (!this.registerForm.phone || this.registerForm.phone.length < 11) {
				uni.showToast({ title: '请输入正确的手机号', icon: 'none' })
				return
			}
			if (!this.registerForm.code || this.registerForm.code.length < 4) {
				uni.showToast({ title: '请输入验证码', icon: 'none' })
				return
			}
			if (!this.registerForm.nickname.trim()) {
				uni.showToast({ title: '请输入昵称', icon: 'none' })
				return
			}
			if (!this.registerForm.campus) {
				uni.showToast({ title: '请选择校区', icon: 'none' })
				return
			}
			if (!this.registerAgreed) {
				uni.showToast({ title: '请先阅读并同意服务协议', icon: 'none' })
				return
			}

			uni.showToast({ title: '注册成功', icon: 'none' })
			setTimeout(() => {
				uni.navigateBack()
			}, 800)
		},
		onWechatLogin() {
			uni.showToast({ title: '微信登录功能即将上线', icon: 'none' })
		},
		showAgreement() {
			uni.showToast({ title: '协议详情即将上线', icon: 'none' })
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
	background: linear-gradient(135deg, #00613C, #00804B);
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
	color: #00613C;
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
	background: #00613C;
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

/* 头像上传 */
.avatar-item {
	display: flex;
	flex-direction: column;
	align-items: center;
}
.avatar-upload {
	margin-top: 8rpx;
}
.avatar-preview {
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	background: #f5f5f5;
}
.avatar-placeholder {
	width: 160rpx;
	height: 160rpx;
	border-radius: 50%;
	border: 2rpx dashed #ccc;
	display: flex;
	flex-direction: column;
	align-items: center;
	justify-content: center;
	background: #fafafa;
}
.avatar-plus {
	font-size: 48rpx;
	color: #ccc;
	line-height: 1;
}
.avatar-hint {
	font-size: 20rpx;
	color: #ccc;
	margin-top: 4rpx;
}

.code-row {
	display: flex;
	align-items: center;
	gap: 16rpx;
}
.code-input {
	flex: 1;
}
.code-btn {
	width: 200rpx;
	height: 84rpx;
	line-height: 84rpx;
	text-align: center;
	background: #e8f5ee;
	color: #00613C;
	font-size: 26rpx;
	border-radius: 12rpx;
	flex-shrink: 0;
	transition: all 0.2s;
}
.code-sending {
	background: #f0f0f0;
	color: #999;
}

.picker-text {
	width: 100%;
	height: 84rpx;
	line-height: 84rpx;
	background: #f5f5f5;
	border-radius: 12rpx;
	padding: 0 20rpx;
	font-size: 28rpx;
	color: #999;
	box-sizing: border-box;
}

/* ===== 协议 ===== */
.agreement-row {
	display: flex;
	align-items: center;
	flex-wrap: wrap;
	margin-bottom: 40rpx;
}
.checkbox-dot {
	width: 32rpx;
	height: 32rpx;
	border: 2rpx solid #ccc;
	border-radius: 50%;
	margin-right: 10rpx;
	flex-shrink: 0;
	transition: all 0.2s;
}
.checkbox-dot.checked {
	border-color: #00613C;
	background: #00613C;
	position: relative;
}
.checkbox-dot.checked::after {
	content: '✓';
	position: absolute;
	top: 50%;
	left: 50%;
	transform: translate(-50%, -50%);
	font-size: 20rpx;
	color: #fff;
}
.agreement-text {
	font-size: 24rpx;
	color: #999;
}
.agreement-link {
	font-size: 24rpx;
	color: #00613C;
}

.submit-btn {
	width: 100%;
	height: 88rpx;
	line-height: 88rpx;
	background: linear-gradient(135deg, #00613C, #00804B);
	color: #ffffff;
	font-size: 32rpx;
	font-weight: bold;
	border-radius: 44rpx;
	border: none;
	box-shadow: 0 8rpx 24rpx rgba(0, 97, 60, 0.3);
}

/* ===== 微信登录 ===== */
.wechat-section {
	padding: 40rpx 40rpx;
}
.divider-row {
	display: flex;
	align-items: center;
	gap: 20rpx;
	margin-bottom: 40rpx;
}
.divider-line {
	flex: 1;
	height: 1rpx;
	background: #e0e0e0;
}
.divider-text {
	font-size: 24rpx;
	color: #ccc;
	flex-shrink: 0;
}
.wechat-btn {
	width: 100%;
	height: 88rpx;
	line-height: 88rpx;
	text-align: center;
	background: #ffffff;
	border: 1rpx solid #e0e0e0;
	border-radius: 44rpx;
	display: flex;
	align-items: center;
	justify-content: center;
	gap: 12rpx;
}
.wechat-icon {
	font-size: 36rpx;
}
.wechat-text {
	font-size: 28rpx;
	color: #333;
}
</style>

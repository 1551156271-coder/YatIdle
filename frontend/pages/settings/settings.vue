<template>
	<view class="settings-page">
		<!-- 账号安全 -->
		<view class="section">
			<view class="section-title">账号安全</view>
			<view class="cell-group">
				<view class="cell" @click="showPwdModal = true">
					<text class="cell-text">修改密码</text>
					<text class="cell-arrow">›</text>
				</view>
			</view>
		</view>

		<!-- 通用 -->
		<view class="section">
			<view class="section-title">通用</view>
			<view class="cell-group">
				<view class="cell" @click="clearCache">
					<text class="cell-text">清除缓存</text>
					<text class="cell-hint">{{ cacheSize }}</text>
					<text class="cell-arrow">›</text>
				</view>
			</view>
		</view>

		<!-- 账号管理 -->
		<view class="section">
			<view class="section-title">账号管理</view>
			<view class="cell-group">
				<view class="cell" @click="handleDeleteAccount">
					<text class="cell-text cell-danger">注销账号</text>
					<text class="cell-arrow">›</text>
				</view>
			</view>
		</view>

		<!-- 关于 -->
		<view class="section">
			<view class="section-title">关于</view>
			<view class="cell-group">
				<view class="cell" @click="showAbout('version')">
					<text class="cell-text">当前版本</text>
					<text class="cell-hint">v1.0.0</text>
				</view>
				<view class="cell" @click="showAbout('agreement')">
					<text class="cell-text">用户协议</text>
					<text class="cell-arrow">›</text>
				</view>
				<view class="cell" @click="showAbout('privacy')">
					<text class="cell-text">隐私政策</text>
					<text class="cell-arrow">›</text>
				</view>
			</view>
		</view>

		<!-- 退出登录 -->
		<view class="logout-wrap">
			<view class="logout-btn" @click="handleLogout">退出登录</view>
		</view>

		<!-- ========== 修改密码弹窗 ========== -->
		<view v-if="showPwdModal" class="modal-mask" @click="showPwdModal = false" @touchmove.stop.prevent>
			<view class="modal-card" @click.stop @touchmove.stop.prevent>
				<text class="modal-title">修改密码</text>
				<input class="modal-input" type="text" password placeholder="请输入旧密码" v-model="pwdForm.oldPwd" />
				<input class="modal-input" type="text" password placeholder="请输入新密码" v-model="pwdForm.newPwd" />
				<input class="modal-input" type="text" password placeholder="请确认新密码" v-model="pwdForm.confirmPwd" />
				<view class="modal-btns">
					<view class="modal-btn modal-cancel" @click="showPwdModal = false">取消</view>
					<view class="modal-btn modal-confirm" @click="submitPassword">确认修改</view>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
	import { updateProfile, deleteUser } from '@/api/user.js'

	export default {
		data() {
			return {
				cacheSize: '0KB',
				showPwdModal: false,
				pwdForm: { oldPwd: '', newPwd: '', confirmPwd: '' },
				submitting: false
			}
		},
		onShow() {
			this.calcCache()
		},
		methods: {
			calcCache() {
				try {
					const res = uni.getStorageInfoSync()
					const kb = Math.ceil(res.currentSize || 0)
					this.cacheSize = kb < 1024 ? kb + 'KB' : (kb / 1024).toFixed(1) + 'MB'
				} catch (e) {
					this.cacheSize = '--'
				}
			},
			clearCache() {
				uni.showModal({
					title: '清除缓存',
					content: '将清除所有本地缓存数据，确定继续吗？',
					success: (res) => {
						if (res.confirm) {
							uni.clearStorageSync()
							this.calcCache()
							uni.showToast({ title: '已清除', icon: 'success' })
						}
					}
				})
			},
			showAbout(type) {
				if (type === 'version') return
				const map = {
					agreement: '用户协议内容暂未上线，敬请期待',
					privacy: '隐私政策内容暂未上线，敬请期待'
				}
				uni.showToast({ title: map[type] || '', icon: 'none' })
			},
			handleLogout() {
				uni.showModal({
					title: '退出登录',
					content: '确定要退出当前账号吗？',
					success: (res) => {
						if (res.confirm) {
							uni.removeStorageSync('user')
							uni.reLaunch({ url: '/pages/my/my' })
						}
					}
				})
			},
			handleDeleteAccount() {
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return
				}
				uni.showModal({
					title: '注销账号',
					content: '注销后所有数据将被永久删除，无法恢复。确定要注销账号吗？',
					confirmText: '确定注销',
					confirmColor: '#e74c3c',
					success: async (res) => {
						if (res.confirm) {
							try {
								await deleteUser(user.id)
								uni.removeStorageSync('user')
								uni.showToast({ title: '账号已注销', icon: 'success' })
								setTimeout(() => {
									uni.reLaunch({ url: '/pages/my/my' })
								}, 1500)
							} catch (e) {
								uni.showToast({ title: '注销失败，请重试', icon: 'none' })
							}
						}
					}
				})
			},
			async submitPassword() {
				const { oldPwd, newPwd, confirmPwd } = this.pwdForm
				if (!oldPwd) return uni.showToast({ title: '请输入旧密码', icon: 'none' })
				if (!newPwd || newPwd.length < 6) return uni.showToast({ title: '新密码至少6位', icon: 'none' })
				if (newPwd !== confirmPwd) return uni.showToast({ title: '两次密码不一致', icon: 'none' })

				const user = uni.getStorageSync('user')
				if (!user || !user.id) return uni.showToast({ title: '请先登录', icon: 'none' })

				if (this.submitting) return
				this.submitting = true
				try {
					await updateProfile({ userId: user.id, password: newPwd })
					uni.showToast({ title: '密码修改成功', icon: 'success' })
					this.showPwdModal = false
					this.pwdForm = { oldPwd: '', newPwd: '', confirmPwd: '' }
				} catch (e) {
					uni.showToast({ title: '修改失败，请重试', icon: 'none' })
				} finally {
					this.submitting = false
				}
			}
		}
	}
</script>

<style>
	.settings-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding-top: 20rpx;
		box-sizing: border-box;
	}

	/* 分组 */
	.section { margin-bottom: 20rpx; }
	.section-title {
		font-size: 24rpx; color: #999;
		padding: 0 30rpx 16rpx;
	}

	/* 单元格 */
	.cell-group { background: #ffffff; }
	.cell {
		display: flex; align-items: center;
		padding: 28rpx 30rpx;
		border-bottom: 1rpx solid #f5f5f5;
	}
	.cell:last-child { border-bottom: none; }
	.cell-text { flex: 1; font-size: 28rpx; color: #333; }
	.cell-danger { color: #e74c3c; }
	.cell-hint { font-size: 24rpx; color: #bbb; margin-right: 8rpx; }
	.cell-arrow { font-size: 32rpx; color: #ccc; }

	/* 退出按钮 */
	.logout-wrap { padding: 60rpx 30rpx; }
	.logout-btn {
		background: #ffffff; border-radius: 16rpx;
		text-align: center; padding: 28rpx 0;
		font-size: 30rpx; color: #e74c3c;
	}

	/* 弹窗 */
	.modal-mask {
		position: fixed; top: 0; left: 0; right: 0; bottom: 0;
		background: rgba(0,0,0,0.45); z-index: 1000;
		display: flex; align-items: center; justify-content: center;
	}
	.modal-card {
		width: 580rpx; background: #ffffff; border-radius: 20rpx;
		padding: 40rpx 36rpx 30rpx;
	}
	.modal-title {
		font-size: 34rpx; color: #333; font-weight: bold;
		display: block; text-align: center; margin-bottom: 36rpx;
	}
	.modal-input {
		width: 100%; height: 80rpx; box-sizing: border-box;
		background: #f5f5f5; border-radius: 12rpx;
		padding: 0 20rpx; font-size: 28rpx; margin-bottom: 20rpx;
	}
	.modal-btns { display: flex; gap: 20rpx; margin-top: 30rpx; }
	.modal-btn {
		flex: 1; text-align: center; padding: 22rpx 0;
		border-radius: 12rpx; font-size: 28rpx;
	}
	.modal-cancel { background: #f5f5f5; color: #666; }
	.modal-confirm { background: linear-gradient(135deg, #3A6341, #4E7D56); color: #ffffff; }
</style>

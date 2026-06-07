<template>
	<view class="edit-page">
		<!-- 头像 -->
		<view class="section-card">
			<view class="edit-row" @click="changeAvatar">
				<text class="edit-label">头像</text>
				<view class="avatar-right">
					<view class="avatar-box">
						<image v-if="form.avatar" class="avatar-img" :src="form.avatar" mode="aspectFill"></image>
						<text v-else class="avatar-emoji">🎓</text>
					</view>
					<text class="row-arrow">›</text>
				</view>
			</view>
		</view>

		<!-- 基本信息 -->
		<view class="section-card">
			<view class="edit-row">
				<text class="edit-label">昵称</text>
				<input
					class="edit-input"
					v-model="form.nickname"
					placeholder="请输入昵称"
					maxlength="16"
				/>
			</view>
			<view class="edit-row" @click="showCampusPicker">
				<text class="edit-label">校区</text>
				<view class="avatar-right">
					<text class="edit-value">{{ form.campus || '未设置' }}</text>
					<text class="row-arrow">›</text>
				</view>
			</view>
		</view>

		<!-- 校区选择弹层 -->
		<view v-if="campusVisible" class="picker-overlay" @click="campusVisible = false">
			<view class="picker-panel" @click.stop>
				<view class="picker-title">选择校区</view>
				<view
					v-for="c in campusList"
					:key="c"
					class="picker-item"
					:class="{ 'picker-active': form.campus === c }"
					@click="selectCampus(c)"
				>{{ c }}</view>
				<view class="picker-cancel" @click="campusVisible = false">取消</view>
			</view>
		</view>

		<!-- 保存按钮 -->
		<view class="save-bar">
			<view class="save-btn" @click="onSave">保存</view>
		</view>
	</view>
</template>

<script>
	import { updateProfile, uploadAvatar } from '@/api/user.js'
	export default {
		data() {
			const user = uni.getStorageSync('user') || {}
			return {
				campusVisible: false,
				campusList: ['东校园', '南校园', '北校园', '珠海校区', '深圳校区'],
				form: {
					avatar: user.avatar || '',
					nickname: user.nickname || '',
					campus: user.campus || ''
				}
			}
		},
		methods: {
			changeAvatar() {
				uni.chooseImage({
					count: 1,
					sizeType: ['compressed'],
					sourceType: ['album', 'camera'],
					success: async (res) => {
						try {
							uni.showLoading({ title: '上传中...' })
							const url = await uploadAvatar(res.tempFilePaths[0])
							this.form.avatar = url
							uni.hideLoading()
						} catch (e) {
							uni.hideLoading()
							uni.showToast({ title: '上传失败', icon: 'none' })
						}
					}
				})
			},
			showCampusPicker() {
				this.campusVisible = true
			},
			selectCampus(c) {
				this.form.campus = c
				this.campusVisible = false
			},
			async onSave() {
				const nickname = (this.form.nickname || '').trim()
				if (!nickname) {
					uni.showToast({ title: '昵称不能为空', icon: 'none' })
					return
				}
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.showToast({ title: '请先登录', icon: 'none' })
					return
				}
				try {
					uni.showLoading({ title: '保存中...' })
					await updateProfile({
						userId: user.id,
						nickname: nickname,
						avatar: this.form.avatar || undefined,
						campus: this.form.campus || undefined
					})
					uni.hideLoading()
					// 更新本地存储
					user.nickname = nickname
					user.avatar = this.form.avatar
					user.campus = this.form.campus
					uni.setStorageSync('user', user)
					uni.showToast({ title: '保存成功', icon: 'none' })
					setTimeout(() => uni.navigateBack(), 800)
				} catch (e) {
					uni.hideLoading()
				}
			}
		}
	}
</script>

<style>
	.edit-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding-top: 20rpx;
		box-sizing: border-box;
	}

	/* ===== 表单卡片 ===== */
	.section-card {
		background: #ffffff;
		margin: 0 20rpx 20rpx;
		border-radius: 20rpx;
		overflow: hidden;
	}
	.edit-row {
		display: flex; align-items: center; justify-content: space-between;
		padding: 28rpx 24rpx;
		border-bottom: 1rpx solid #f5f5f5;
	}
	.edit-row:last-child { border-bottom: none; }
	.edit-label { font-size: 28rpx; color: #333; flex-shrink: 0; margin-right: 24rpx; }

	.avatar-right { display: flex; align-items: center; gap: 12rpx; }
	.avatar-box {
		width: 80rpx; height: 80rpx;
		border-radius: 50%;
		background: #e8f5ee;
		display: flex; align-items: center; justify-content: center;
		overflow: hidden;
	}
	.avatar-img { width: 100%; height: 100%; border-radius: 50%; }
	.avatar-emoji { font-size: 36rpx; color: #375f3e; font-weight: bold; line-height: 1; }
	.row-arrow { font-size: 32rpx; color: #ccc; }

	.edit-input {
		flex: 1; text-align: right;
		font-size: 28rpx; color: #333;
	}
	.edit-value { font-size: 28rpx; color: #666; }

	/* ===== 校区选择器 ===== */
	.picker-overlay {
		position: fixed; top: 0; left: 0; right: 0; bottom: 0;
		background: rgba(0,0,0,0.4);
		z-index: 999;
		display: flex; align-items: flex-end; justify-content: center;
	}
	.picker-panel {
		width: 100%; background: #ffffff;
		border-radius: 24rpx 24rpx 0 0;
		padding: 30rpx 0;
	}
	.picker-title {
		font-size: 30rpx; color: #999; text-align: center;
		padding-bottom: 20rpx; border-bottom: 1rpx solid #f0f0f0;
		margin-bottom: 10rpx;
	}
	.picker-item {
		font-size: 32rpx; color: #333; text-align: center;
		padding: 28rpx 0;
	}
	.picker-item:active { background: #f5f5f5; }
	.picker-active { color: #3A6341; font-weight: bold; }
	.picker-cancel {
		font-size: 30rpx; color: #999; text-align: center;
		padding: 28rpx 0; margin-top: 10rpx;
		border-top: 1rpx solid #f0f0f0;
	}

	/* ===== 保存按钮 ===== */
	.save-bar { padding: 40rpx 20rpx; }
	.save-btn {
		width: 100%; height: 88rpx; line-height: 88rpx;
		text-align: center;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		color: #ffffff; font-size: 32rpx; font-weight: bold;
		border-radius: 44rpx;
	}
	.save-btn:active { opacity: 0.85; }
</style>

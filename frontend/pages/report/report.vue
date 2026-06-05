<template>
	<view class="report-page">
		<!-- 顶部导航 -->
		<view class="report-header" :style="{ paddingTop: statusBarHeight + 'px' }">
			<text class="report-back" @click="goBack">‹</text>
			<text class="report-title">举报用户</text>
			<view class="report-placeholder"></view>
		</view>

		<view class="form-card">
			<view class="form-item">
				<text class="form-label">举报原因</text>
				<view class="reason-list">
					<view
						v-for="r in reasons"
						:key="r.value"
						class="reason-item"
						:class="{ 'reason-active': selectedReason === r.value }"
						@click="selectedReason = r.value"
					>
						<text class="reason-radio">{{ selectedReason === r.value ? '●' : '○' }}</text>
						<text class="reason-text">{{ r.label }}</text>
					</view>
				</view>
			</view>

			<view class="form-item">
				<text class="form-label">详细描述 <text class="label-optional">（选填）</text></text>
				<textarea
					class="form-textarea"
					v-model="description"
					placeholder="请详细描述举报原因，帮助我们更快处理"
					:maxlength="500"
				/>
				<text class="char-count">{{ description.length }}/500</text>
			</view>

			<view class="form-item">
				<text class="form-label">相关截图 <text class="label-optional">（选填）</text></text>
				<view class="upload-row">
					<view
						v-for="(img, idx) in images"
						:key="idx"
						class="upload-preview"
					>
						<image :src="img" mode="aspectFill" class="up-img"></image>
						<text class="up-del" @click="removeImage(idx)">✕</text>
					</view>
					<view v-if="images.length < 4" class="upload-btn" @click="addImage">
						<text class="ub-icon">+</text>
						<text class="ub-text">添加截图</text>
					</view>
				</view>
			</view>

			<button
				class="submit-btn"
				:class="{ 'submit-disabled': !selectedReason }"
				:disabled="!selectedReason"
				@click="onSubmit"
			>
				提交举报
			</button>
		</view>

		<view class="report-hint">
			<text class="hint-text">我们会尽快审核您的举报，违规用户将受到相应处理</text>
		</view>
	</view>
</template>

<script>
	import { createReport } from '../../api/report'

	export default {
		data() {
			return {
				statusBarHeight: 44,
				targetUserId: null,
				reasons: [
					{ label: '发布虚假商品信息', value: 'fake' },
					{ label: '假冒品牌/商品', value: 'counterfeit' },
					{ label: '骚扰/辱骂行为', value: 'harass' },
					{ label: '欺诈行为', value: 'fraud' },
					{ label: '其他违规行为', value: 'other' }
				],
				selectedReason: '',
				description: '',
				images: []
			}
		},
		onLoad(options) {
			this.targetUserId = options && options.targetUserId ? Number(options.targetUserId) : null
			try {
				const sys = uni.getSystemInfoSync()
				this.statusBarHeight = sys.statusBarHeight || 44
			} catch (e) {
				this.statusBarHeight = 44
			}
		},
		methods: {
			addImage() {
				uni.chooseImage({
					count: 4 - this.images.length,
					success: (res) => {
						this.images = this.images.concat(res.tempFilePaths)
					}
				})
			},
			removeImage(idx) {
				this.images.splice(idx, 1)
			},
			onSubmit() {
				if (!this.selectedReason) return
				uni.showModal({
					title: '提交举报',
					content: '确认提交此举报？我们将在24小时内处理',
					success: async (res) => {
						if (res.confirm) {
							const user = uni.getStorageSync('user') || {}
							if (!user.id) {
								uni.showToast({ title: '请先登录', icon: 'none' })
								return
							}
							await createReport({
								reporterId: user.id,
								targetUserId: this.targetUserId,
								reason: this.selectedReason,
								description: this.description,
								imageUrls: this.images
							})
							uni.showToast({ title: '举报已提交', icon: 'success' })
							setTimeout(() => {
								uni.navigateBack()
							}, 1500)
						}
					}
				})
			},
			goBack() {
				uni.navigateBack()
			}
		}
	}
</script>

<style>
	.report-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
	}

	/* ===== 顶部导航 ===== */
	.report-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding-left: 20rpx;
		padding-right: 20rpx;
		padding-bottom: 20rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		box-sizing: border-box;
	}

	.report-back {
		font-size: 48rpx;
		color: #ffffff;
		font-weight: 300;
		width: 60rpx;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.report-title {
		font-size: 36rpx;
		color: #ffffff;
		font-weight: bold;
	}

	.report-placeholder {
		width: 60rpx;
	}

	/* ===== 表单 ===== */
	.form-card {
		background: #ffffff;
		margin: 20rpx;
		border-radius: 20rpx;
		padding: 30rpx;
		box-sizing: border-box;
	}

	.form-item { margin-bottom: 36rpx; }
	.form-label {
		font-size: 28rpx;
		color: #333;
		font-weight: bold;
		margin-bottom: 16rpx;
		display: block;
	}

	.label-optional {
		font-size: 24rpx;
		color: #999;
		font-weight: normal;
	}

	/* ===== 原因列表 ===== */
	.reason-list { display: flex; flex-direction: column; gap: 4rpx; }

	.reason-item {
		display: flex;
		align-items: center;
		padding: 24rpx 20rpx;
		border-radius: 12rpx;
		background: #fafafa;
	}

	.reason-item:active { background: #f0f0f0; }
	.reason-active { background: #e8f5ee; }

	.reason-radio {
		font-size: 32rpx;
		color: #ccc;
		margin-right: 16rpx;
		width: 32rpx;
		text-align: center;
	}

	.reason-active .reason-radio { color: #3A6341; }

	.reason-text {
		font-size: 28rpx;
		color: #333;
	}

	.reason-active .reason-text { color: #3A6341; font-weight: bold; }

	/* ===== 描述 ===== */
	.form-textarea {
		width: 100%;
		height: 200rpx;
		background: #f5f5f5;
		border-radius: 12rpx;
		padding: 20rpx;
		font-size: 26rpx;
		box-sizing: border-box;
	}

	.char-count {
		font-size: 22rpx;
		color: #ccc;
		text-align: right;
		display: block;
		margin-top: 8rpx;
	}

	/* ===== 图片上传 ===== */
	.upload-row {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
	}

	.upload-preview {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		overflow: hidden;
		position: relative;
	}

	.up-img { width: 100%; height: 100%; }
	.up-del {
		position: absolute;
		top: 4rpx;
		right: 4rpx;
		width: 36rpx;
		height: 36rpx;
		background: rgba(0,0,0,0.5);
		color: #ffffff;
		font-size: 22rpx;
		border-radius: 50%;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.upload-btn {
		width: 160rpx;
		height: 160rpx;
		border: 2rpx dashed #ddd;
		border-radius: 12rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		background: #fafafa;
		box-sizing: border-box;
	}

	.ub-icon { font-size: 48rpx; color: #ccc; line-height: 1; }
	.ub-text { font-size: 22rpx; color: #999; margin-top: 8rpx; }

	/* ===== 提交按钮 ===== */
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
		margin-top: 20rpx;
		box-shadow: 0 8rpx 24rpx rgba(0,97,60,0.3);
	}

	.submit-disabled {
		background: #ccc;
		box-shadow: none;
	}

	/* ===== 提示 ===== */
	.report-hint {
		padding: 0 40rpx 40rpx;
		text-align: center;
	}

	.hint-text {
		font-size: 22rpx;
		color: #bbb;
		line-height: 1.6;
	}
</style>

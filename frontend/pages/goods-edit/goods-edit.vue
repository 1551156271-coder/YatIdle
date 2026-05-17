<template>
	<view class="edit-page">
		<!-- 顶部导航 -->
		<view class="edit-header" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="header-back" @click="goBack">
				<text class="back-icon">‹</text>
			</view>
			<text class="header-title">编辑物品信息</text>
			<view class="header-placeholder"></view>
		</view>

		<!-- 表单 -->
		<view class="form-card">
			<!-- 商品图片 -->
			<view class="form-item">
				<text class="form-label">商品图片</text>
				<view class="image-grid">
					<view class="image-item" v-for="(img, idx) in form.images" :key="idx" @click="previewImage(idx)">
						<image class="image-thumb" :src="img" mode="aspectFill" />
						<view class="image-remove" @click.stop="removeImage(idx)">✕</view>
					</view>
					<view class="upload-box" @click="uploadImage" v-if="form.images.length < 9">
						<text class="upload-icon">+</text>
						<text class="upload-text">添加图片</text>
					</view>
				</view>
			</view>

			<!-- 商品名称 -->
			<view class="form-item">
				<text class="form-label">商品名称</text>
				<input class="form-input" v-model="form.title" placeholder="请输入商品名称" />
			</view>

			<!-- 价格 -->
			<view class="form-item">
				<text class="form-label">价格</text>
				<view class="price-input-box">
					<text class="price-unit">¥</text>
					<input class="form-input price-input" type="digit" v-model="form.price" placeholder="0.00" />
				</view>
			</view>

			<!-- 商品分类 -->
			<view class="form-item">
				<text class="form-label">商品分类</text>
				<picker :range="categoryList" @change="onCategoryChange">
					<view class="picker-text">{{ form.category || '请选择分类' }}</view>
				</picker>
			</view>

			<!-- 所在校区 -->
			<view class="form-item">
				<text class="form-label">所在校区</text>
				<picker :range="campusList" @change="onCampusChange">
					<view class="picker-text">{{ form.campus || '请选择校区' }}</view>
				</picker>
			</view>

			<!-- 成色 -->
			<view class="form-item">
				<text class="form-label">成色</text>
				<picker :range="conditionList" @change="onConditionChange">
					<view class="picker-text">{{ form.condition || '请选择商品成色' }}</view>
				</picker>
			</view>

			<!-- 标签 -->
			<view class="form-item">
				<text class="form-label">标签</text>
				<input class="form-input" v-model="form.tags" placeholder="多个标签用空格隔开，如：电子产品 数码" />
			</view>

			<!-- 商品描述 -->
			<view class="form-item">
				<text class="form-label">商品描述</text>
				<textarea class="form-textarea" v-model="form.desc" placeholder="描述一下你的商品吧~" :maxlength="500" />
				<text class="char-count">{{ form.desc.length }}/500</text>
			</view>

			<button class="submit-btn" @click="onSave">保存修改</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				statusBarHeight: 44,
				goodsId: '',
				categoryList: ['数码电子', '书籍教材', '生活用品', '运动户外', '服饰鞋包', '其他'],
				campusList: ['东校园', '南校园', '北校园', '珠海校区', '深圳校区'],
				conditionList: ['全新', '99新', '95新', '90新', '85新', '80新以下'],
				form: {
					title: '',
					price: '',
					category: '',
					campus: '',
					condition: '',
					tags: '',
					desc: '',
					images: []
				}
			}
		},
		onLoad(options) {
			try {
				const sys = uni.getSystemInfoSync()
				this.statusBarHeight = sys.statusBarHeight || 44
			} catch (e) {
				this.statusBarHeight = 44
			}
			if (options.id) {
				this.goodsId = options.id
				this.loadGoodsData(options.id)
			}
		},
		methods: {
			loadGoodsData(id) {
				// TODO: 从API获取商品数据，此处用模拟数据
				this.form = {
					title: '九成新公路自行车，带锁和挡泥板',
					price: '268.00',
					category: '运动户外',
					campus: '东校园',
					condition: '九成新',
					tags: '自行车 出行 东校区自提',
					desc: '去年学期初在校外车行买的，平时只在教学楼和宿舍之间通勤。车架很轻，变速灵敏。离校转手，东校园自提。配件齐全，包括车锁和挡泥板，骑行体验非常好。',
					images: [
						'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop'
					]
				}
			},
			goBack() {
				uni.navigateBack()
			},
			uploadImage() {
				const remain = 9 - this.form.images.length
				uni.chooseImage({
					count: remain,
					success: (res) => {
						this.form.images = this.form.images.concat(res.tempFilePaths)
					}
				})
			},
			previewImage(idx) {
				uni.previewImage({
					current: idx,
					urls: this.form.images
				})
			},
			removeImage(idx) {
				this.form.images.splice(idx, 1)
			},
			onCategoryChange(e) {
				this.form.category = this.categoryList[e.detail.value]
			},
			onCampusChange(e) {
				this.form.campus = this.campusList[e.detail.value]
			},
			onConditionChange(e) {
				this.form.condition = this.conditionList[e.detail.value]
			},
			onSave() {
				if (!this.form.title) {
					uni.showToast({ title: '请输入商品名称', icon: 'none' })
					return
				}
				if (!this.form.price) {
					uni.showToast({ title: '请输入价格', icon: 'none' })
					return
				}
				uni.showModal({
					title: '保存修改',
					content: '确认保存对该商品的修改？',
					success: (res) => {
						if (res.confirm) {
							uni.showToast({ title: '保存成功', icon: 'success' })
							setTimeout(() => {
								uni.navigateBack()
							}, 1500)
						}
					}
				})
			}
		}
	}
</script>

<style>
	.edit-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
		padding-bottom: 40rpx;
	}

	/* ===== 顶部导航 ===== */
	.edit-header {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding-left: 20rpx;
		padding-right: 20rpx;
		padding-bottom: 20rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		box-sizing: border-box;
	}

	.header-back {
		width: 60rpx;
		height: 60rpx;
		display: flex;
		align-items: center;
		justify-content: center;
	}

	.back-icon {
		font-size: 80rpx;
		color: #ffffff;
		font-weight: 300;
		line-height: 0.9;
	}

	.header-title {
		font-size: 36rpx;
		color: #ffffff;
		font-weight: bold;
	}

	.header-placeholder {
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
	.form-label { font-size: 28rpx; color: #333; font-weight: bold; margin-bottom: 16rpx; display: block; }

	/* 图片 */
	.image-grid {
		display: flex;
		flex-wrap: wrap;
		gap: 16rpx;
	}
	.image-item {
		width: 160rpx; height: 160rpx;
		border-radius: 16rpx;
		position: relative;
		overflow: hidden;
	}
	.image-thumb {
		width: 100%; height: 100%;
		border-radius: 16rpx;
	}
	.image-remove {
		position: absolute;
		top: -2rpx; right: -2rpx;
		width: 44rpx; height: 44rpx;
		background: rgba(0,0,0,0.5);
		color: #ffffff;
		font-size: 24rpx;
		border-radius: 0 16rpx 0 16rpx;
		display: flex; align-items: center; justify-content: center;
	}
	.upload-box {
		width: 160rpx; height: 160rpx;
		border: 2rpx dashed #ddd;
		border-radius: 16rpx;
		display: flex; flex-direction: column; align-items: center; justify-content: center;
		background: #fafafa;
	}
	.upload-icon { font-size: 48rpx; color: #ccc; line-height: 1; }
	.upload-text { font-size: 22rpx; color: #999; margin-top: 8rpx; }

	/* 输入框 */
	.form-input {
		width: 100%; height: 80rpx;
		background: #f5f5f5; border-radius: 12rpx;
		padding: 0 20rpx; font-size: 28rpx;
		box-sizing: border-box;
	}

	.price-input-box {
		display: flex; align-items: center;
		background: #f5f5f5; border-radius: 12rpx; padding-left: 20rpx;
		box-sizing: border-box;
	}
	.price-unit { font-size: 32rpx; color: #e74c3c; font-weight: bold; margin-right: 4rpx; }
	.price-input { flex: 1; background: transparent; padding: 0; box-sizing: border-box; }

	.picker-text {
		width: 100%; height: 80rpx; line-height: 80rpx;
		background: #f5f5f5; border-radius: 12rpx;
		padding: 0 20rpx; font-size: 28rpx; color: #333;
		box-sizing: border-box;
	}

	.form-textarea {
		width: 100%; height: 180rpx;
		background: #f5f5f5; border-radius: 12rpx;
		padding: 20rpx; font-size: 28rpx;
		box-sizing: border-box;
	}

	.char-count {
		font-size: 22rpx;
		color: #ccc;
		text-align: right;
		display: block;
		margin-top: 8rpx;
	}

	/* 提交按钮 */
	.submit-btn {
		width: 100%; height: 88rpx; line-height: 88rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		color: #ffffff; font-size: 32rpx; font-weight: bold;
		border-radius: 44rpx; border: none;
		margin-top: 40rpx;
		box-shadow: 0 8rpx 24rpx rgba(0,97,60,0.3);
		box-sizing: border-box;
	}
</style>

<template>
	<view class="publish-page">
		<view class="publish-hero">
			<text class="hero-icon">📸</text>
			<text class="hero-title">发布闲置好物</text>
			<text class="hero-sub">让闲置流动起来，助力绿色校园</text>
		</view>
		<view class="form-card">
			<view class="form-item">
				<text class="form-label">商品图片</text>
				<view class="upload-box" @click="uploadImage">
					<text class="upload-icon">+</text>
					<text class="upload-text">添加图片</text>
				</view>
			</view>
			<view class="form-item">
				<text class="form-label">商品名称</text>
				<input class="form-input" placeholder="请输入商品名称" />
			</view>
			<view class="form-item">
				<text class="form-label">价格</text>
				<view class="price-input-box">
					<text class="price-unit">¥</text>
					<input class="form-input price-input" type="digit" placeholder="0.00" />
				</view>
			</view>
			<view class="form-item">
				<text class="form-label">商品分类</text>
				<picker :range="categoryList" @change="onCategoryChange">
					<view class="picker-text">{{ selectedCategory || '请选择分类' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">所在校区</text>
				<picker :range="campusList" @change="onCampusChange">
					<view class="picker-text">{{ selectedCampus || '请选择校区' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">商品描述</text>
				<textarea class="form-textarea" placeholder="描述一下你的商品吧~" :maxlength="500" />
			</view>
			<button class="submit-btn" @click="onSubmit">发布商品</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				categoryList: ['数码电子', '书籍教材', '生活用品', '运动户外', '服饰鞋包', '其他'],
				campusList: ['东校园', '南校园', '北校园', '珠海校区', '深圳校区'],
				selectedCategory: '',
				selectedCampus: ''
			}
		},
		methods: {
			uploadImage() {
				uni.chooseImage({
					count: 9,
					success: (res) => {
						uni.showToast({ title: '已选择 ' + res.tempFilePaths.length + ' 张', icon: 'none' })
					}
				})
			},
			onCategoryChange(e) {
				this.selectedCategory = this.categoryList[e.detail.value]
			},
			onCampusChange(e) {
				this.selectedCampus = this.campusList[e.detail.value]
			},
			onSubmit() {
				uni.showToast({ title: '发布成功！', icon: 'none' })
			}
		}
	}
</script>

<style>
	.publish-page {
		min-height: 100vh; width: 100%; overflow: hidden; box-sizing: border-box;
		background: #f5f5f5;
	}

	.publish-hero {
		background: linear-gradient(135deg, #00613C, #00804B);
		padding: 40rpx 30rpx 50rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.hero-icon { font-size: 64rpx; margin-bottom: 16rpx; }
	.hero-title { font-size: 36rpx; color: #ffffff; font-weight: bold; margin-bottom: 8rpx; }
	.hero-sub { font-size: 24rpx; color: rgba(255,255,255,0.8); }

	.form-card {
		background: #ffffff;
		margin: 20rpx;
		border-radius: 20rpx;
		padding: 30rpx;
	}

	.form-item { margin-bottom: 36rpx; }
	.form-label { font-size: 28rpx; color: #333; font-weight: bold; margin-bottom: 16rpx; display: block; }

	.upload-box {
		width: 160rpx; height: 160rpx;
		border: 2rpx dashed #ddd;
		border-radius: 16rpx;
		display: flex; flex-direction: column; align-items: center; justify-content: center;
		background: #fafafa;
	}
	.upload-icon { font-size: 48rpx; color: #ccc; line-height: 1; }
	.upload-text { font-size: 22rpx; color: #999; margin-top: 8rpx; }

	.form-input {
		width: 100%; height: 80rpx;
		background: #f5f5f5; border-radius: 12rpx;
		padding: 0 20rpx; font-size: 28rpx;
	}

	.price-input-box {
		display: flex; align-items: center;
		background: #f5f5f5; border-radius: 12rpx; padding-left: 20rpx;
	}
	.price-unit { font-size: 32rpx; color: #e74c3c; font-weight: bold; margin-right: 4rpx; }
	.price-input { flex: 1; background: transparent; padding: 0; }

	.picker-text {
		width: 100%; height: 80rpx; line-height: 80rpx;
		background: #f5f5f5; border-radius: 12rpx;
		padding: 0 20rpx; font-size: 28rpx; color: #999;
	}

	.form-textarea {
		width: 100%; height: 180rpx;
		background: #f5f5f5; border-radius: 12rpx;
		padding: 20rpx; font-size: 28rpx;
	}

	.submit-btn {
		width: 100%; height: 88rpx; line-height: 88rpx;
		background: linear-gradient(135deg, #00613C, #00804B);
		color: #ffffff; font-size: 32rpx; font-weight: bold;
		border-radius: 44rpx; border: none;
		margin-top: 40rpx;
		box-shadow: 0 8rpx 24rpx rgba(0,97,60,0.3);
	}
</style>

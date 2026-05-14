<template>
	<view class="publish-page">
		<view class="publish-hero">
			<text class="hero-icon">{{ publishType === 'sell' ? '📸' : '📋' }}</text>
			<text class="hero-title">{{ publishType === 'sell' ? '发布闲置好物' : '发布求购信息' }}</text>
			<text class="hero-sub">{{ publishType === 'sell' ? '让闲置流动起来，助力绿色校园' : '快速找到你想要的宝贝' }}</text>
		</view>

		<!-- ===== 出售表单 ===== -->
		<view v-if="publishType === 'sell'" class="form-card">
			<view class="form-item">
				<text class="form-label">商品图片</text>
				<view class="upload-box" @click="uploadImage">
					<text class="upload-icon">+</text>
					<text class="upload-text">添加图片</text>
				</view>
			</view>
			<view class="form-item">
				<text class="form-label">商品名称</text>
				<input class="form-input" v-model="sellForm.title" placeholder="请输入商品名称" />
			</view>
			<view class="form-item">
				<text class="form-label">价格</text>
				<view class="price-input-box">
					<text class="price-unit">¥</text>
					<input class="form-input price-input" type="digit" v-model="sellForm.price" placeholder="0.00" />
				</view>
			</view>
			<view class="form-item">
				<text class="form-label">商品分类</text>
				<picker :range="categoryList" @change="onSellCategoryChange">
					<view class="picker-text">{{ sellForm.category || '请选择分类' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">所在校区</text>
				<picker :range="campusList" @change="onSellCampusChange">
					<view class="picker-text">{{ sellForm.campus || '请选择校区' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">成色</text>
				<picker :range="conditionList" @change="onSellConditionChange">
					<view class="picker-text">{{ sellForm.condition || '请选择商品成色' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">标签</text>
				<input class="form-input" v-model="sellForm.tags" placeholder="多个标签用空格隔开，如：电子产品 数码" />
			</view>
			<view class="form-item">
				<text class="form-label">商品描述</text>
				<textarea class="form-textarea" v-model="sellForm.desc" placeholder="描述一下你的商品吧~" :maxlength="500" />
			</view>
			<button class="submit-btn" @click="onSellSubmit">发布商品</button>
		</view>

		<!-- ===== 求购表单 ===== -->
		<view v-else class="form-card">
			<view class="form-item">
				<text class="form-label">求购物品</text>
				<input class="form-input" v-model="buyForm.title" placeholder="你想买什么？" />
			</view>
			<view class="form-item">
				<text class="form-label">预算范围</text>
				<view class="budget-row">
					<view class="price-input-box budget-input">
						<text class="price-unit">¥</text>
						<input class="form-input price-input" type="digit" v-model="buyForm.budgetMin" placeholder="最低" />
					</view>
					<text class="budget-sep">—</text>
					<view class="price-input-box budget-input">
						<text class="price-unit">¥</text>
						<input class="form-input price-input" type="digit" v-model="buyForm.budgetMax" placeholder="最高" />
					</view>
				</view>
				<text v-if="budgetError" class="error-text">最低价格不能高于最高价格</text>
			</view>
			<view class="form-item">
				<text class="form-label">商品分类</text>
				<picker :range="categoryList" @change="onBuyCategoryChange">
					<view class="picker-text">{{ buyForm.category || '请选择分类' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">所在校区</text>
				<picker :range="campusList" @change="onBuyCampusChange">
					<view class="picker-text">{{ buyForm.campus || '请选择校区' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">期望成色</text>
				<picker :range="conditionList" @change="onBuyConditionChange">
					<view class="picker-text">{{ buyForm.condition || '不限' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">补充说明</text>
				<textarea class="form-textarea" v-model="buyForm.desc" placeholder="描述一下你的具体需求，如品牌、型号等" :maxlength="500" />
			</view>
			<button class="submit-btn" @click="onBuySubmit">发布求购</button>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				publishType: 'sell',
				categoryList: ['数码电子', '书籍教材', '生活用品', '运动户外', '服饰鞋包', '其他'],
				campusList: ['东校园', '南校园', '北校园', '珠海校区', '深圳校区'],
				conditionList: ['不限', '全新', '99新', '95新', '90新', '85新', '80新以下'],

				sellForm: {
					title: '',
					price: '',
					category: '',
					campus: '',
					condition: '',
					tags: '',
					desc: '',
					images: []
				},

				buyForm: {
					title: '',
					budgetMin: '',
					budgetMax: '',
					category: '',
					campus: '',
					condition: '',
					desc: ''
				}
			}
		},
		computed: {
			budgetError() {
				const min = parseFloat(this.buyForm.budgetMin)
				const max = parseFloat(this.buyForm.budgetMax)
				if (this.buyForm.budgetMin && this.buyForm.budgetMax && !isNaN(min) && !isNaN(max)) {
					return min > max
				}
				return false
			}
		},
		onLoad(options) {
			if (options.type === 'buy') {
				this.publishType = 'buy'
			}
		},
		methods: {
			uploadImage() {
				uni.chooseImage({
					count: 9,
					success: (res) => {
						this.sellForm.images = this.sellForm.images.concat(res.tempFilePaths)
						uni.showToast({ title: '已选择 ' + res.tempFilePaths.length + ' 张', icon: 'none' })
					}
				})
			},

			onSellCategoryChange(e) {
				this.sellForm.category = this.categoryList[e.detail.value]
			},
			onSellCampusChange(e) {
				this.sellForm.campus = this.campusList[e.detail.value]
			},
			onSellConditionChange(e) {
				const val = this.conditionList[e.detail.value]
				this.sellForm.condition = val === '不限' ? '' : val
			},

			onSellSubmit() {
				if (!this.sellForm.title) {
					uni.showToast({ title: '请输入商品名称', icon: 'none' })
					return
				}
				if (!this.sellForm.price) {
					uni.showToast({ title: '请输入价格', icon: 'none' })
					return
				}
				uni.showToast({ title: '发布成功！', icon: 'success' })
			},

			onBuyCategoryChange(e) {
				this.buyForm.category = this.categoryList[e.detail.value]
			},
			onBuyCampusChange(e) {
				this.buyForm.campus = this.campusList[e.detail.value]
			},
			onBuyConditionChange(e) {
				this.buyForm.condition = this.conditionList[e.detail.value]
			},

			onBuySubmit() {
				if (!this.buyForm.title) {
					uni.showToast({ title: '请输入求购物品', icon: 'none' })
					return
				}
				if (this.budgetError) {
					uni.showToast({ title: '请检查预算范围', icon: 'none' })
					return
				}
				uni.showToast({ title: '求购发布成功！', icon: 'success' })
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
		box-sizing: border-box;
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
		padding: 0 20rpx; font-size: 28rpx; color: #999;
		box-sizing: border-box;
	}

	.form-textarea {
		width: 100%; height: 180rpx;
		background: #f5f5f5; border-radius: 12rpx;
		padding: 20rpx; font-size: 28rpx;
		box-sizing: border-box;
	}

	.submit-btn {
		width: 100%; height: 88rpx; line-height: 88rpx;
		background: linear-gradient(135deg, #00613C, #00804B);
		color: #ffffff; font-size: 32rpx; font-weight: bold;
		border-radius: 44rpx; border: none;
		margin-top: 40rpx;
		box-shadow: 0 8rpx 24rpx rgba(0,97,60,0.3);
		box-sizing: border-box;
	}

	.budget-row {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.budget-input {
		flex: 1;
	}

	.budget-sep {
		font-size: 28rpx;
		color: #ccc;
		flex-shrink: 0;
	}

	.error-text {
		font-size: 24rpx;
		color: #e74c3c;
		margin-top: 10rpx;
		display: block;
	}
</style>

<template>
	<view class="publish-page">
		<view class="publish-hero">
			<text class="hero-icon">{{ publishType === 'sell' ? '📸' : '📋' }}</text>
			<text class="hero-title">{{ isEdit ? '编辑求购信息' : (publishType === 'sell' ? '发布闲置好物' : '发布求购信息') }}</text>
			<text class="hero-sub">{{ isEdit ? '修改你的求购需求' : (publishType === 'sell' ? '让闲置流动起来，助力绿色校园' : '快速找到你想要的宝贝') }}</text>
		</view>

		<!-- ===== 出售表单 ===== -->
		<view v-if="publishType === 'sell'" class="form-card">
			<view class="form-item">
				<text class="form-label">商品图片</text>
				<view class="image-grid">
					<view class="image-item" v-for="(img, idx) in sellForm.images" :key="idx" @click="previewSellImage(idx)">
						<image class="image-thumb" :src="resolveImageUrl(img)" mode="aspectFill" />
						<view class="image-remove" @click.stop="removeSellImage(idx)">✕</view>
					</view>
					<view class="upload-box" @click="uploadSellImage" v-if="sellForm.images.length < 9">
						<text class="upload-icon">+</text>
						<text class="upload-text">添加图片</text>
					</view>
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
				<picker :range="categoryLabels" @change="onSellCategoryChange">
					<view class="picker-text">{{ sellForm.categoryName || '请选择分类' }}</view>
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
				<text class="form-label">商品描述</text>
				<textarea class="form-textarea" v-model="sellForm.desc" placeholder="描述一下你的商品吧~" :maxlength="500" />
			</view>
			<button class="submit-btn" @click="onSellSubmit" :disabled="submitting">发布商品</button>
		</view>

		<!-- ===== 求购表单 ===== -->
		<view v-else class="form-card">
			<view class="form-item">
				<text class="form-label">求购物品</text>
				<input class="form-input" v-model="buyForm.title" placeholder="你想买什么？" :maxlength="50" />
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
				<text v-if="budgetError" class="error-text">最低价格必须低于最高价格</text>
			</view>
			<view class="form-item">
				<text class="form-label">商品分类</text>
				<picker :range="categoryLabels" @change="onBuyCategoryChange">
					<view class="picker-text">{{ buyForm.categoryName || '请选择分类' }}</view>
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
				<picker :range="wantedConditionList" @change="onBuyConditionChange">
					<view class="picker-text">{{ buyForm.condition || '不限' }}</view>
				</picker>
			</view>
			<view class="form-item">
				<text class="form-label">参考图片<text class="form-label-hint">（选填）</text></text>
				<view class="image-grid">
					<view class="image-item" v-for="(img, idx) in buyForm.images" :key="idx" @click="previewBuyImage(idx)">
						<image class="image-thumb" :src="resolveImageUrl(img)" mode="aspectFill" />
						<view class="image-remove" @click.stop="removeBuyImage(idx)">✕</view>
					</view>
					<view class="upload-box" @click="uploadBuyImage" v-if="buyForm.images.length < 9">
						<text class="upload-icon">+</text>
						<text class="upload-text">添加图片</text>
					</view>
				</view>
			</view>
			<view class="form-item">
				<text class="form-label">补充说明</text>
				<textarea class="form-textarea" v-model="buyForm.desc" placeholder="描述一下你的具体需求，如品牌、型号等" :maxlength="500" />
			</view>
			<button class="submit-btn" @click="onBuySubmit">{{ isEdit ? '保存修改' : '发布求购' }}</button>
		</view>
	</view>
</template>

<script>
	import { publishItem, getCategories, uploadImage } from '@/api/item.js'
	import { publishWanted, updateWanted } from '@/api/wanted.js'
	import { resolveImageUrl, resolveUploadStorageUrl } from '@/api/index.js'

	export default {
		data() {
			return {
				publishType: 'sell',
				isEdit: false,
				editId: null,
				submitting: false,
				categories: [],
				categoryLabels: [],
				campusList: ['东校园', '南校园', '北校园', '珠海校区', '深圳校区'],
				conditionList: ['全新', '99新', '95新', '90新', '85新', '80新', '80新以下'],
				wantedConditionList: ['不限', '全新', '99新及以上', '95新及以上', '90新及以上', '85新及以上', '80新及以上'],

				sellForm: {
					title: '',
					price: '',
					categoryId: null,
					categoryName: '',
					campus: '',
					condition: '',
					desc: '',
					images: []
				},

				buyForm: {
					title: '',
					budgetMin: '',
					budgetMax: '',
					categoryId: null,
					categoryName: '',
					campus: '',
					condition: '',
					desc: '',
					images: []
				}
			}
		},
		computed: {
			budgetError() {
				const min = parseFloat(this.buyForm.budgetMin)
				const max = parseFloat(this.buyForm.budgetMax)
				if (this.buyForm.budgetMin && this.buyForm.budgetMax && !isNaN(min) && !isNaN(max)) {
					return min >= max
				}
				return false
			}
		},
		onLoad(options) {
			if (options.type === 'buy') {
				this.publishType = 'buy'
			}
			if (options.edit === '1') {
				this.isEdit = true
				this.editId = parseInt(options.id)
				const data = uni.getStorageSync('editWantedData')
				if (data) {
					this.buyForm = {
						title: data.title || '',
						budgetMin: data.budgetMin || '',
						budgetMax: data.budgetMax || '',
						categoryId: data.categoryId || null,
						categoryName: data.categoryLabel || '',
						campus: data.campus || '',
						condition: data.condition || '',
						desc: data.desc || '',
						images: data.images || []
					}
				}
			}
			this.loadCategories()
		},
		methods: {
			resolveImageUrl,
			async loadCategories() {
				try {
					this.categories = await getCategories()
					this.categoryLabels = this.categories.map(c => c.name)
				} catch (e) {
					this.categories = [
						{ id: 1, name: '数码电子' }, { id: 2, name: '书籍教材' },
						{ id: 3, name: '生活用品' }, { id: 4, name: '运动户外' },
						{ id: 5, name: '服饰鞋包' }, { id: 6, name: '其他' }
					]
					this.categoryLabels = this.categories.map(c => c.name)
				}
			},

			uploadSellImage() {
				uni.chooseImage({
					count: 9 - this.sellForm.images.length,
					sizeType: ['compressed'],
					success: (res) => {
						this.sellForm.images = this.sellForm.images.concat(res.tempFilePaths)
					}
				})
			},
			previewSellImage(idx) {
				uni.previewImage({ current: idx, urls: this.sellForm.images.map(resolveImageUrl) })
			},
			removeSellImage(idx) {
				this.sellForm.images.splice(idx, 1)
			},

			onSellCategoryChange(e) {
				const idx = e.detail.value
				this.sellForm.categoryName = this.categoryLabels[idx]
				this.sellForm.categoryId = this.categories[idx].id
			},
			onSellCampusChange(e) {
				this.sellForm.campus = this.campusList[e.detail.value]
			},
			onSellConditionChange(e) {
				const val = this.conditionList[e.detail.value]
				this.sellForm.condition = val
			},

			async onSellSubmit() {
				if (!this.sellForm.title) {
					uni.showToast({ title: '请输入商品名称', icon: 'none' })
					return
				}
				if (!this.sellForm.price) {
					uni.showToast({ title: '请输入价格', icon: 'none' })
					return
				}
				if (!this.sellForm.categoryId) {
					uni.showToast({ title: '请选择分类', icon: 'none' })
					return
				}
				const user = uni.getStorageSync('user')
				if (!user || !user.id) {
					uni.navigateTo({ url: '/pages/login/login' })
					return
				}
				this.submitting = true
				try {
					uni.showLoading({ title: '上传图片中...' })
					const imageUrls = []
					for (const img of this.sellForm.images) {
						if (img.includes('/uploads/')) {
							imageUrls.push(resolveUploadStorageUrl(img))
						} else {
							const url = await uploadImage(img)
							imageUrls.push(url)
						}
					}
					uni.hideLoading()
					const payload = {
						userId: user.id,
						title: this.sellForm.title,
						price: Number(this.sellForm.price),
						categoryId: this.sellForm.categoryId,
						campus: this.sellForm.campus || undefined,
						conditionLevel: this.sellForm.condition || undefined,
						description: this.sellForm.desc || undefined,
						imageUrls
					}
					const data = await publishItem(payload)
					uni.setStorageSync('needRefreshHomeGoods',true)
					uni.showToast({ title: '发布成功！', icon: 'success' })
					setTimeout(() => {
						uni.switchTab({
							url: '/pages/index/index',
							success: () => {
								setTimeout(() => {
									uni.navigateTo({ url: '/pages/goods-detail/goods-detail?id=' + data.id })
								}, 150)
							}
						})
					}, 1200)
					this.submitting = false
				} catch (e) {
					uni.hideLoading()
					uni.showToast({
						title: e.message || '图片上传失败',
						icon: 'none'
					})
					this.submitting = false
				}
			},

			onBuyCategoryChange(e) {
				const idx = e.detail.value
				this.buyForm.categoryName = this.categoryLabels[idx]
				this.buyForm.categoryId = this.categories[idx].id
			},
			onBuyCampusChange(e) {
				this.buyForm.campus = this.campusList[e.detail.value]
			},
			onBuyConditionChange(e) {
				const val = this.wantedConditionList[e.detail.value]
				this.buyForm.condition = val === '不限' ? '' : val
			},

			uploadBuyImage() {
				const remain = 9 - this.buyForm.images.length
				uni.chooseImage({
					count: remain,
					sizeType: ['compressed'],
					success: (res) => {
						this.buyForm.images = this.buyForm.images.concat(res.tempFilePaths)
					}
				})
			},
			previewBuyImage(idx) {
				uni.previewImage({ current: idx, urls: this.buyForm.images.map(resolveImageUrl) })
			},
			removeBuyImage(idx) {
				this.buyForm.images.splice(idx, 1)
			},

		async onBuySubmit() {
			if (!this.buyForm.title) {
				uni.showToast({ title: '请输入求购物品', icon: 'none' })
				return
			}
			if(this.buyForm.title.length > 50) {
				uni.showToast({ title: '求购标题不能超过50字', icon: 'none'})
				return
			}
			if (this.budgetError) {
				uni.showToast({ title: '请检查预算范围', icon: 'none' })
				return
			}
			const user = uni.getStorageSync('user')
			if (!user || !user.id) {
				uni.navigateTo({ url: '/pages/login/login' })
				return
			}
			this.submitting = true
			try {
				uni.showLoading({ title: '发布中...' })
				const imageUrls = []
				for (const img of this.buyForm.images) {
					if (img.includes('/uploads/')) {
						imageUrls.push(resolveUploadStorageUrl(img))
					} else {
						const url = await uploadImage(img)
						imageUrls.push(url)
					}
				}
				uni.hideLoading()
				const payload = {
					userId: user.id,
					title: this.buyForm.title,
					budgetMin: Number(this.buyForm.budgetMin) || 0,
					budgetMax: Number(this.buyForm.budgetMax) || 0,
					categoryId: this.buyForm.categoryId || undefined,
					campus: this.buyForm.campus || undefined,
					conditionLevel: this.buyForm.condition && this.buyForm.condition !== '不限' ? this.buyForm.condition : undefined,
					description: this.buyForm.desc || undefined,
					imageUrls
				}
				if (this.isEdit) {
					await updateWanted(this.editId, payload)
					uni.showToast({ title: '修改成功！', icon: 'success' })
					setTimeout(() => {
						uni.switchTab({
							url: '/pages/wanted/wanted'
						})
					}, 1200)
				} else {
					const data = await publishWanted(payload)
					uni.showToast({ title: '发布成功！', icon: 'success' })
					setTimeout(() => {
						uni.switchTab({
							url: '/pages/wanted/wanted'
						})
					}, 1200)
				}
				this.submitting = false
			} catch (e) {
				uni.hideLoading()
				uni.showToast({
					title: e.message || '图片上传失败',
					icon: 'none'
				})
				this.submitting = false
			}
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
		background: linear-gradient(135deg, #3A6341, #4E7D56);
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
	.form-label-hint { font-size: 24rpx; color: #999; font-weight: normal; }

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

	.submit-btn {
		width: 100%; height: 88rpx; line-height: 88rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
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

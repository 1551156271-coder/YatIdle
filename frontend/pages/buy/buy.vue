<template>
	<view class="buy-page">
		<!-- 顶部导航 -->
		<view class="buy-header" :style="{ paddingTop: statusBarHeight + 'px' }">
			<view class="header-back" @click="goBack">
				<text class="back-icon">‹</text>
			</view>
			<text class="header-title">确认购买</text>
			<view class="header-placeholder"></view>
		</view>

		<!-- 商品信息 -->
		<view class="section-card">
			<view class="section-title">商品信息</view>
			<view class="goods-row">
				<image class="goods-img" :src="goods.image" mode="aspectFill"></image>
				<view class="goods-info">
					<text class="goods-title">{{ goods.title }}</text>
					<view class="goods-tags">
						<text class="g-tag">{{ goods.condition }}</text>
						<text class="g-tag">{{ goods.campus }}</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 卖家信息 -->
		<view class="section-card">
			<view class="section-title">卖家信息</view>
			<view class="seller-row">
				<view class="seller-avatar">
					<image v-if="seller.avatar" class="seller-avatar-img" :src="seller.avatar" mode="aspectFill"></image>
					<text v-else class="seller-avatar-emoji">{{ seller.defaultAvatar }}</text>
				</view>
				<view class="seller-info">
					<text class="seller-name">{{ seller.name }}</text>
					<text class="seller-credit">信用分 {{ seller.credit }}</text>
				</view>
			</view>
		</view>

		<!-- 交易方式 -->
		<view class="section-card">
			<view class="section-title">交易方式</view>
			<view class="trade-option trade-active">
				<view class="trade-left">
					<text class="trade-icon">🤝</text>
					<view class="trade-text">
						<text class="trade-name">校内面交/自提</text>
						<text class="trade-desc">与卖家协商见面交易</text>
					</view>
				</view>
			</view>
		</view>

		<!-- 价格明细 -->
		<view class="section-card">
			<view class="section-title">价格明细</view>
			<view class="price-row">
				<text class="price-label">商品价格</text>
				<text class="price-value">¥{{ goods.price }}</text>
			</view>
			<view class="price-divider"></view>
			<view class="price-row price-total">
				<text class="total-label">合计</text>
				<text class="total-value">¥{{ goods.price }}</text>
			</view>
		</view>

		<!-- 备注 -->
		<view class="section-card">
			<view class="section-title">买家留言 <text class="section-hint">（选填）</text></view>
			<textarea
				class="remark-input"
				v-model="remark"
				placeholder="如：什么时间方便见面？"
				:maxlength="200"
			/>
			<text class="remark-count">{{ remark.length }}/200</text>
		</view>

		<!-- 底部按钮 -->
		<view class="bottom-bar">
			<view class="bottom-price">
				<text class="bp-label">合计：</text>
				<text class="bp-value">¥{{ goods.price }}</text>
			</view>
			<button class="submit-btn" @click="onSubmit">确认购买</button>
		</view>

		<!-- 安全提示 -->
		<view class="safety-tip">
			<text class="safety-icon">🛡️</text>
			<text class="safety-text">交易安全提示：建议在校内公共场所见面交易，当面验货后再付款</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				statusBarHeight: 44,
				remark: '',
				goods: {
					id: 1,
					image: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop',
					title: '九成新公路自行车，带锁和挡泥板',
					price: '268.00',
					condition: '九成新',
					campus: '东校园'
				},
				seller: {
					id: 2,
					avatar: '', defaultAvatar: '🎓',
					name: '中大在校生',
					credit: 98
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
			// 接收商品参数
			if (options.id) {
				// TODO: 根据id请求商品详情
				this.goods.id = options.id
			}
		},
		methods: {
			goBack() {
				uni.navigateBack()
			},
			onSubmit() {
				uni.showModal({
					title: '确认购买',
					content: '确认购买该商品？下单后请等待卖家确认。',
					success: (res) => {
						if (res.confirm) {
							uni.showToast({ title: '下单成功', icon: 'success' })
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
	.buy-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
		padding-bottom: 160rpx;
	}

	/* ===== 顶部导航 ===== */
	.buy-header {
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

	/* ===== 卡片 ===== */
	.section-card {
		background: #ffffff;
		margin: 20rpx;
		border-radius: 20rpx;
		padding: 30rpx;
		box-sizing: border-box;
	}

	.section-title {
		font-size: 28rpx;
		color: #3A6341;
		font-weight: bold;
		margin-bottom: 20rpx;
	}

	.section-hint {
		font-size: 24rpx;
		color: #999;
		font-weight: normal;
	}

	/* ===== 商品信息 ===== */
	.goods-row {
		display: flex;
		gap: 20rpx;
	}

	.goods-img {
		width: 160rpx;
		height: 160rpx;
		border-radius: 12rpx;
		flex-shrink: 0;
		background: #f0f0f0;
	}

	.goods-info {
		flex: 1;
		display: flex;
		flex-direction: column;
		justify-content: space-between;
	}

	.goods-title {
		font-size: 30rpx;
		color: #333;
		line-height: 1.4;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
	}

	.goods-tags {
		display: flex;
		gap: 12rpx;
	}

	.g-tag {
		font-size: 22rpx;
		color: #3A6341;
		background: #e8f5ee;
		padding: 6rpx 16rpx;
		border-radius: 6rpx;
	}

	/* ===== 卖家信息 ===== */
	.seller-row {
		display: flex;
		align-items: center;
		gap: 20rpx;
	}

	.seller-avatar {
	width: 80rpx; height: 80rpx;
	background: #e8f5ee;
	border-radius: 50%;
	display: flex;
	align-items: center;
	justify-content: center;
	font-size: 36rpx;
	flex-shrink: 0; overflow: hidden;
}
.seller-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
.seller-avatar-emoji { font-size: 36rpx; }

	.seller-info {
		display: flex;
		flex-direction: column;
		gap: 8rpx;
	}

	.seller-name {
		font-size: 28rpx;
		color: #333;
		font-weight: bold;
	}

	.seller-credit {
		font-size: 24rpx;
		color: #999;
	}

	/* ===== 交易方式 ===== */
	.trade-option {
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 24rpx 20rpx;
		border-radius: 12rpx;
		background: #fafafa;
		margin-bottom: 12rpx;
	}

	.trade-option:last-child {
		margin-bottom: 0;
	}

	.trade-active {
		background: #e8f5ee;
	}

	.trade-left {
		display: flex;
		align-items: center;
		gap: 16rpx;
	}

	.trade-icon {
		font-size: 40rpx;
	}

	.trade-text {
		display: flex;
		flex-direction: column;
		gap: 4rpx;
	}

	.trade-name {
		font-size: 28rpx;
		color: #333;
		font-weight: bold;
	}

	.trade-desc {
		font-size: 22rpx;
		color: #999;
	}

	.trade-radio {
		font-size: 32rpx;
		color: #ccc;
	}

	.trade-active .trade-radio {
		color: #3A6341;
	}

	/* ===== 价格明细 ===== */
	.price-row {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 16rpx;
	}

	.price-label {
		font-size: 26rpx;
		color: #666;
	}

	.price-value {
		font-size: 28rpx;
		color: #333;
		font-weight: bold;
	}

	.price-hint {
		color: #999;
		font-weight: normal;
	}

	.price-divider {
		height: 1rpx;
		background: #f0f0f0;
		margin: 16rpx 0;
	}

	.price-total {
		margin-bottom: 0;
	}

	.total-label {
		font-size: 28rpx;
		color: #333;
		font-weight: bold;
	}

	.total-value {
		font-size: 36rpx;
		color: #e74c3c;
		font-weight: bold;
	}

	/* ===== 备注 ===== */
	.remark-input {
		width: 100%;
		height: 160rpx;
		background: #f5f5f5;
		border-radius: 12rpx;
		padding: 20rpx;
		font-size: 26rpx;
		box-sizing: border-box;
	}

	.remark-count {
		font-size: 22rpx;
		color: #ccc;
		text-align: right;
		display: block;
		margin-top: 8rpx;
	}

	/* ===== 底部栏 ===== */
	.bottom-bar {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		height: 120rpx;
		background: #ffffff;
		display: flex;
		align-items: center;
		justify-content: space-between;
		padding: 0 30rpx;
		border-top: 1rpx solid #f0f0f0;
		box-shadow: 0 -4rpx 16rpx rgba(0,0,0,0.05);
		box-sizing: border-box;
		z-index: 100;
	}

	.bottom-price {
		display: flex;
		align-items: baseline;
	}

	.bp-label {
		font-size: 26rpx;
		color: #666;
	}

	.bp-value {
		font-size: 40rpx;
		color: #e74c3c;
		font-weight: bold;
	}

	.submit-btn {
		width: 260rpx;
		height: 80rpx;
		line-height: 80rpx;
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		color: #ffffff;
		font-size: 30rpx;
		font-weight: bold;
		border-radius: 40rpx;
		border: none;
		margin: 0;
		box-shadow: 0 8rpx 24rpx rgba(58,99,65,0.3);
	}

	/* ===== 安全提示 ===== */
	.safety-tip {
		display: flex;
		align-items: flex-start;
		gap: 12rpx;
		padding: 0 40rpx 40rpx;
	}

	.safety-icon {
		font-size: 28rpx;
		flex-shrink: 0;
		margin-top: 2rpx;
	}

	.safety-text {
		font-size: 22rpx;
		color: #bbb;
		line-height: 1.6;
	}
</style>

<script>
	export default {
		onLaunch: function() {
			console.log('App Launch')
			uni.hideTabBar()
		},
		onShow: function() {
			console.log('App Show')
		},
		onHide: function() {
			console.log('App Hide')
		}
	}
</script>

<style>
	/*每个页面公共css */
	@import './static/iconfont/iconfont.css';

	page {
		min-height: 100%;
		background: #f5f7f6;
	}

	button::after {
		border: none;
	}

	.network-error {
		margin: 120rpx 48rpx 0;
		padding: 40rpx 32rpx;
		border-radius: 20rpx;
		background: #ffffff;
		box-shadow: 0 8rpx 28rpx rgba(25, 49, 35, 0.08);
		display: flex;
		flex-direction: column;
		align-items: center;
		gap: 16rpx;
		box-sizing: border-box;
	}

	.network-title {
		font-size: 32rpx;
		font-weight: 700;
		color: #17202a;
	}

	.network-desc {
		font-size: 24rpx;
		color: #718096;
		line-height: 1.6;
		text-align: center;
	}

	.retry-btn {
		margin-top: 8rpx;
		height: 64rpx;
		line-height: 64rpx;
		padding: 0 36rpx;
		border-radius: 32rpx;
		color: #fff;
		background: #2f7d51;
		font-size: 26rpx;
	}
</style>

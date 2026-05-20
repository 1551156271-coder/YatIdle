<template>
	<view class="my-tab-bar">
		<view
			v-for="(item, index) in list"
			:key="index"
			class="tb-item"
			@click="onTabClick(item, index)"
		>
			<!-- 选中顶部指示条 -->
			<view v-if="current === index" class="tb-indicator"></view>
			<text
				class="tb-icon iconfont"
				:class="current === index && item.selectedIcon ? item.selectedIcon : item.icon"
				:style="{ color: current === index ? selectedColor : color }"
			></text>
			<text
				class="tb-text"
				:class="{ 'tb-text-active': current === index }"
				:style="{ color: current === index ? selectedColor : color }"
			>{{ item.text }}</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				color: '#676767',
				selectedColor: '#3A6341',
				current: 0,
				list: [
					{ pagePath: '/pages/index/index', text: '首页', icon: 'icon-shouye1', selectedIcon: 'icon-shouye' },
					{ pagePath: '/pages/wanted/wanted', text: '求购', icon: 'icon-remenqiugou' },
					{ pagePath: '/pages/publish/publish', text: '发布', icon: 'icon-fabu-', selectedIcon: 'icon-fabudianjizhuangtai-' },
					{ pagePath: '/pages/messages/messages', text: '消息', icon: 'icon-xiaoxi', selectedIcon: 'icon-xiaoxi2' },
					{ pagePath: '/pages/my/my', text: '我的', icon: 'icon-wode-copy', selectedIcon: 'icon-wode' }
				]
			}
		},
		created() {
			const pages = getCurrentPages()
			const page = pages[pages.length - 1]
			if (page) {
				const idx = this.list.findIndex(t => t.pagePath === '/' + page.route)
				if (idx !== -1) this.current = idx
			}
		},
		methods: {
			onTabClick(item, index) {
				if (this.current === index) return
				uni.switchTab({ url: item.pagePath })
			}
		}
	}
</script>

<style>
	.my-tab-bar {
		position: fixed;
		bottom: 0;
		left: 0;
		right: 0;
		display: flex;
		align-items: center;
		justify-content: space-around;
		height: 100rpx;
		background: #ffffff;
		border-top: 1rpx solid #f0f0f0;
		padding-bottom: env(safe-area-inset-bottom);
		z-index: 999;
	}

	.tb-item {
		display: flex;
		flex-direction: column;
		align-items: center;
		justify-content: center;
		flex: 1;
		height: 100%;
		gap: 4rpx;
		position: relative;
	}

	/* 选中顶部指示条 */
	.tb-indicator {
		position: absolute;
		top: 0;
		left: 50%;
		transform: translateX(-50%);
		width: 48rpx;
		height: 6rpx;
		background: #3A6341;
		border-radius: 0 0 6rpx 6rpx;
	}

	.tb-icon {
		font-size: 50rpx;
		line-height: 1;
	}

	.tb-text {
		font-size: 20rpx;
		line-height: 1;
	}

	.tb-text-active {
		font-weight: bold;
	}
</style>

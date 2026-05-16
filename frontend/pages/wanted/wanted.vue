<template>
	<view class="wanted-page">
		<view class="wanted-hero">
			<text class="hero-icon">📋</text>
			<text class="hero-title">校园求购</text>
			<text class="hero-sub">看看同学们都在求购什么</text>
		</view>

		<view class="wanted-list">
			<view
				v-for="item in wantedList"
				:key="item.id"
				class="wanted-card"
				@click="goToDetail(item)"
			>
				<view class="wanted-top">
					<text class="wanted-emoji">📋</text>
					<text class="wanted-title">{{ item.title }}</text>
				</view>
				<view class="wanted-meta">
					<text class="wanted-budget">预算 ¥{{ item.budgetMin }} — ¥{{ item.budgetMax }}</text>
					<text class="wanted-campus">{{ item.campus }}</text>
				</view>
				<view class="wanted-tags">
					<text class="wanted-tag">{{ item.condition }}</text>
					<text class="wanted-tag">{{ item.categoryLabel }}</text>
				</view>
				<text class="wanted-desc">{{ item.desc }}</text>
				<view class="wanted-footer">
					<text class="wanted-user">{{ item.username }}</text>
					<text class="wanted-time">{{ item.time }}</text>
				</view>
			</view>
		</view>

		<view v-if="wantedList.length === 0" class="empty-state">
			<text class="empty-icon">📋</text>
			<text class="empty-text">暂无求购信息</text>
		</view>
	</view>
</template>

<script>
	export default {
		data() {
			return {
				wantedList: [
					{ id: 101, title: '求购一台二手笔记本电脑', budgetMin: '2000', budgetMax: '3500', campus: '东校园', condition: '85新以上', categoryLabel: '数码电子', desc: '女生自用，主要用于写论文和看视频，电池续航好一些的', username: '小橙子', time: '10分钟前' },
					{ id: 102, title: '收高数下册+习题集', budgetMin: '15', budgetMax: '30', campus: '南校园', condition: '不限', categoryLabel: '书籍教材', desc: '下学期要用，有笔记也可以，价格好商量', username: '数学苦手', time: '1小时前' },
					{ id: 103, title: '二手电动车代步用', budgetMin: '600', budgetMax: '1200', campus: '珠海校区', condition: '90新以上', categoryLabel: '生活用品', desc: '校区太大走路太累，求购一辆二手电动车，续航好一点的', username: '骑车上学', time: '3小时前' },
					{ id: 104, title: '收一双42码跑鞋', budgetMin: '100', budgetMax: '250', campus: '北校园', condition: '95新以上', categoryLabel: '运动户外', desc: '体育课需要，穿不了几次所以不想买全新的', username: '运动达人', time: '昨天' }
				]
			}
		},
		methods: {
			goToDetail(item) {
				uni.navigateTo({
						url: '/pages/wanted-detail/wanted-detail?id=' + item.id
					})
			}
		}
	}
</script>

<style>
	.wanted-page {
		min-height: 100vh;
		width: 100%;
		background: #f5f5f5;
		overflow: hidden;
		box-sizing: border-box;
	}

	.wanted-hero {
		background: linear-gradient(135deg, #00613C, #00804B);
		padding: 40rpx 30rpx 50rpx;
		display: flex;
		flex-direction: column;
		align-items: center;
	}

	.hero-icon { font-size: 64rpx; margin-bottom: 16rpx; }
	.hero-title { font-size: 36rpx; color: #ffffff; font-weight: bold; margin-bottom: 8rpx; }
	.hero-sub { font-size: 24rpx; color: rgba(255,255,255,0.8); }

	/* ===== 求购列表 ===== */
	.wanted-list {
		padding: 20rpx 20rpx 0;
		box-sizing: border-box;
	}

	.wanted-card {
		width: 100%;
		background: #ffffff;
		border-radius: 16rpx;
		padding: 24rpx 24rpx 20rpx;
		margin-bottom: 20rpx;
		box-shadow: 0 2rpx 12rpx rgba(0, 0, 0, 0.04);
		box-sizing: border-box;
		border-left: 8rpx solid #00613C;
	}

	.wanted-card:active {
		opacity: 0.9;
	}

	.wanted-top {
		display: flex;
		align-items: center;
		margin-bottom: 12rpx;
	}

	.wanted-emoji {
		font-size: 32rpx;
		margin-right: 12rpx;
	}

	.wanted-title {
		font-size: 30rpx;
		color: #333;
		font-weight: bold;
		flex: 1;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 1;
		overflow: hidden;
	}

	.wanted-meta {
		display: flex;
		justify-content: space-between;
		align-items: center;
		margin-bottom: 12rpx;
	}

	.wanted-budget {
		font-size: 26rpx;
		color: #00613C;
		font-weight: bold;
	}

	.wanted-campus {
		font-size: 22rpx;
		color: #999;
	}

	.wanted-tags {
		display: flex;
		gap: 10rpx;
		margin-bottom: 12rpx;
	}

	.wanted-tag {
		font-size: 22rpx;
		color: #00613C;
		background: #e8f5ee;
		padding: 4rpx 16rpx;
		border-radius: 20rpx;
	}

	.wanted-desc {
		font-size: 26rpx;
		color: #666;
		line-height: 1.5;
		display: -webkit-box;
		-webkit-box-orient: vertical;
		-webkit-line-clamp: 2;
		overflow: hidden;
		margin-bottom: 16rpx;
	}

	.wanted-footer {
		display: flex;
		justify-content: space-between;
		align-items: center;
	}

	.wanted-user {
		font-size: 24rpx;
		color: #999;
	}

	.wanted-time {
		font-size: 22rpx;
		color: #ccc;
	}

	/* ===== 空状态 ===== */
	.empty-state {
		display: flex;
		flex-direction: column;
		align-items: center;
		padding-top: 160rpx;
	}

	.empty-icon { font-size: 100rpx; margin-bottom: 20rpx; }
	.empty-text { font-size: 28rpx; color: #999; }
</style>

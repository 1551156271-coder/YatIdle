<template>
	<view class="messages-page">
		<view class="chat-list">
			<view v-for="item in chatList" :key="item.id" class="chat-item" @click="openChat(item)">
				<view class="chat-avatar">
					<image v-if="item.avatar" class="chat-avatar-img" :src="item.avatar" mode="aspectFill"></image>
					<text v-else class="chat-avatar-emoji">{{ item.defaultAvatar }}</text>
				</view>
				<view class="chat-content">
					<view class="chat-top">
						<text class="chat-name">{{ item.name }}</text>
						<text class="chat-time">{{ item.time }}</text>
					</view>
					<view class="chat-bottom">
						<text class="chat-msg">{{ item.lastMsg }}</text>
						<view v-if="item.unread > 0" class="unread-badge">{{ item.unread > 99 ? '99+' : item.unread }}</view>
					</view>
				</view>
			</view>
		</view>

		<view v-if="chatList.length === 0" class="empty-state">
			<text class="empty-icon">💬</text>
			<text class="empty-text">暂无消息</text>
		</view>
	</view>
	<tab-bar />
</template>

<script>
	import TabBar from '@/components/tab-bar.vue'
	export default {
		components: { TabBar },
		data() {
			return {
				chatList: [
					{ id: 1, avatar: '', defaultAvatar: '🎓', name: '中大二手交易助手', lastMsg: '欢迎来到闲鸭蛋！', time: '昨天', unread: 1 },
					{ id: 2, avatar: '', defaultAvatar: '🤝', name: '张三（卖家）', lastMsg: '好的，明天东校食堂门口见', time: '2小时前', unread: 2 },
					{ id: 3, avatar: '', defaultAvatar: '📚', name: '李四（买家）', lastMsg: '这本教材还有吗？', time: '3天前', unread: 0 }
				]
			}
		},
		onShow() {
			uni.hideTabBar()
		},
		methods: {
			openChat(item) {
				uni.navigateTo({
					url: '/pages/chat/chat?id=' + item.id
				})
			}
		}
	}
</script>

<style>
	.messages-page { min-height: 100vh; width: 100%; background: #f5f5f5; overflow: hidden; box-sizing: border-box; }

	.chat-list { background: #ffffff; margin: 20rpx; border-radius: 16rpx; overflow: hidden; }

	.chat-item {
		display: flex; align-items: center;
		padding: 28rpx 24rpx; border-bottom: 1rpx solid #f5f5f5;
	}
	.chat-item:last-child { border-bottom: none; }

	.chat-avatar {
	width: 96rpx; height: 96rpx;
	background: #e8f5ee; border-radius: 50%;
	display: flex; align-items: center; justify-content: center;
	font-size: 44rpx; margin-right: 20rpx; flex-shrink: 0; overflow: hidden;
}
.chat-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
.chat-avatar-emoji { font-size: 44rpx; }

	.chat-content { flex: 1; overflow: hidden; }
	.chat-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
	.chat-name { font-size: 30rpx; color: #333; font-weight: 500; }
	.chat-time { font-size: 22rpx; color: #ccc; }
	.chat-bottom { display: flex; justify-content: space-between; align-items: center; }
	.chat-msg { font-size: 26rpx; color: #999; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; flex: 1; }

	.unread-badge {
		min-width: 36rpx; height: 36rpx; line-height: 36rpx;
		background: #e74c3c; color: #ffffff; font-size: 20rpx;
		border-radius: 18rpx; padding: 0 8rpx; text-align: center;
	}

	.empty-state { display: flex; flex-direction: column; align-items: center; padding-top: 200rpx; }
	.empty-icon { font-size: 100rpx; margin-bottom: 20rpx; }
	.empty-text { font-size: 28rpx; color: #999; }
</style>

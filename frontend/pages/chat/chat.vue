<template>
	<view class="chat-page" :style="{ paddingTop: statusBarHeight + 'px' }">
		<!-- 顶部联系人栏 -->
		<view class="chat-header" :style="{ top: statusBarHeight + 'px', paddingRight: headerRightPad + 'px' }">
			<view class="header-back" @click="goBack">
				<text class="back-icon">‹</text>
			</view>
			<view class="header-info">
				<view class="header-avatar">
					<text class="header-avatar-emoji">{{ contactInfo.defaultAvatar }}</text>
				</view>
				<view class="header-text">
					<text class="header-name">{{ contactInfo.name }}</text>
				</view>
			</view>
		</view>

		<!-- 消息区域 -->
		<scroll-view
			class="msg-scroll"
			scroll-y
			:scroll-into-view="scrollToId"
			:scroll-with-animation="scrollAnimated"
		>
			<view v-if="msgs.length === 0" class="empty-chat">
				<text class="empty-text">开始聊天吧~</text>
			</view>

			<block v-for="msg in msgs" :key="msg.id">
				<view :id="'msg-' + msg.id">
					<view v-if="msg.showTime" class="time-divider">
						<text class="time-text">{{ msg.timeLabel }}</text>
					</view>

					<view v-if="msg.type === 'system'" class="system-msg">
						<text class="system-text">{{ msg.content }}</text>
					</view>

					<view v-else class="msg-row" :class="{ 'msg-self': msg.fromMe }">
						<view v-if="!msg.fromMe" class="msg-avatar">
							<text class="msg-avatar-emoji">{{ contactInfo.defaultAvatar }}</text>
						</view>

						<view class="msg-bubble-wrap">
							<!-- 文本 -->
							<view v-if="msg.type === 'text'" class="bubble bubble-text" :class="{ 'bubble-self': msg.fromMe }">
								<text class="msg-text">{{ msg.content }}</text>
							</view>

							<!-- 图片 -->
							<view v-else-if="msg.type === 'image'" class="bubble bubble-media" @click="previewImage(msg)">
								<image class="msg-image" :src="msg.content" mode="aspectFill" :style="msg.imgStyle"></image>
							</view>

							<!-- 语音 -->
							<view v-else-if="msg.type === 'voice'" class="bubble bubble-voice" :class="{ 'bubble-self': msg.fromMe }" @click="playVoice(msg)">
								<text class="voice-icon iconfont icon-yuyin" :class="{ 'voice-playing': msg.playing }"></text>
								<text class="voice-dur">{{ msg.duration }}"</text>
							</view>

							<!-- 位置 -->
							<view v-else-if="msg.type === 'location'" class="bubble bubble-card" @click="openLocation(msg)">
								<view class="loc-card">
									<text class="loc-icon">&#x1F4CD;</text>
									<view class="loc-info">
										<text class="loc-name">{{ msg.locationData.name }}</text>
										<text class="loc-addr">{{ msg.locationData.address }}</text>
									</view>
								</view>
							</view>
						</view>

						<view v-if="msg.fromMe" class="msg-avatar msg-avatar-self">
							<text class="msg-avatar-emoji">🎓</text>
						</view>
					</view>
				</view>
			</block>

			<view class="scroll-anchor" id="msg-bottom"></view>
		</scroll-view>

		<!-- 录音浮层 -->
		<view v-if="recording" class="record-overlay" @touchmove.prevent @touchend="stopRecord">
			<view class="record-box">
				<view class="record-wave">
					<view class="wave-bar" v-for="i in 7" :key="i" :style="{ animationDelay: i * 0.12 + 's' }"></view>
				</view>
				<text class="record-hint">↑ 上滑取消</text>
				<text class="record-time">{{ recordTime }}"</text>
			</view>
		</view>

		<!-- 底部输入区 -->
		<view class="input-area">
			<view class="input-row">
				<view class="mode-switch" @click="switchInputMode">
					<text v-if="inputMode === 'text'" class="mode-icon iconfont icon-huatong"></text>
					<text v-else class="mode-icon iconfont icon-jianpan"></text>
				</view>

				<view class="input-box">
					<input
						v-if="inputMode === 'text'"
						class="text-input"
						v-model="inputText"
						placeholder="说点什么..."
						confirm-type="send"
						@confirm="sendText"
					/>
					<view
						v-else
						class="voice-btn"
						@touchstart="startRecord"
						@touchmove.prevent
						@touchend="stopRecord"
					>按住 说话</view>
				</view>

				<view class="plus-btn" :class="{ 'plus-active': toolbarOpen }" @click="toggleToolbar">
					<text class="plus-icon iconfont icon-fabu"></text>
				</view>
			</view>

			<!-- 扩展面板 -->
			<view v-if="toolbarOpen" class="toolbar-panel">
				<view class="tb-item" @click="pickImage">
					<view class="tb-icon" style="background:#e3f2fd;"><text class="tb-emoji">🖼</text></view>
					<text class="tb-label">相册</text>
				</view>
				<view class="tb-item" @click="takePhoto">
					<view class="tb-icon" style="background:#e8f5e9;"><text class="tb-emoji">📷</text></view>
					<text class="tb-label">拍摄</text>
				</view>
				<view class="tb-item" @click="sendLocation">
					<view class="tb-icon" style="background:#e0f2f1;"><text class="tb-emoji">📍</text></view>
					<text class="tb-label">位置</text>
				</view>
				<view class="tb-item" @click="featurePending('发送订单')">
					<view class="tb-icon" style="background:#fff3e0;"><text class="tb-emoji">📋</text></view>
					<text class="tb-label">订单</text>
				</view>
				<view class="tb-item" @click="featurePending('发送商品')">
					<view class="tb-icon" style="background:#fce4ec;"><text class="tb-emoji">📦</text></view>
					<text class="tb-label">商品</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
import { getMessages, sendMessage, markRead } from '@/api/chat.js'

export default {
	data() {
		return {
			statusBarHeight: 0,
			headerRightPad: 20,
			scrollAnimated: false,
			contactInfo: { id: null, avatar: '', defaultAvatar: '🤝', name: '聊天' },
			msgs: [],
			inputText: '',
			inputMode: 'text',
			toolbarOpen: false,
			recording: false,
			recordTime: 0,
			recordTimer: null,
			scrollToId: '',
			sessionId: null,
			userId: null,
			pollTimer: null
		}
	},
	onLoad(options) {
		const sysInfo = uni.getSystemInfoSync()
		this.statusBarHeight = sysInfo.statusBarHeight || 20
		const menuButton = uni.getMenuButtonBoundingClientRect()
		this.headerRightPad = sysInfo.windowWidth - menuButton.left + 12

		const user = uni.getStorageSync('user')
		if (user && user.id) {
			this.userId = user.id
		}
		if (options.id) {
			this.sessionId = options.id
			this.loadMessages()
		}
		this.$nextTick(() => this.scrollToBottom())
		setTimeout(() => { this.scrollAnimated = true }, 500)
	},
	onUnload() {
		clearInterval(this.recordTimer)
		clearInterval(this.pollTimer)
	},
	methods: {
		goBack() { uni.navigateBack() },

		async loadMessages() {
			if (!this.sessionId || !this.userId) return
			try {
				// 标记已读
				await markRead(this.sessionId, this.userId).catch(() => {})
				const result = await getMessages(this.sessionId, this.userId); const list = (result && result.records) || result || []
				this.msgs = list.map(m => ({
					id: m.id,
					fromMe: m.senderId === this.userId,
					type: m.messageType === 'IMAGE' ? 'image' : 'text',
					content: m.content,
					time: new Date(m.createTime).getTime(),
					showTime: false
				}))
				this.addTimeDividers()
				this.$nextTick(() => this.scrollToBottom())
			} catch (e) {
				this.msgs = []
			}
		},

		addTimeDividers() {
			for (let i = 0; i < this.msgs.length; i++) {
				const msg = this.msgs[i]
				const prev = this.msgs[i - 1]
				if (!prev || (msg.time - prev.time > 5 * 60 * 1000)) {
					msg.showTime = true
					msg.timeLabel = this.formatTime(msg.time)
				}
			}
		},

		async sendText() {
			const t = this.inputText.trim()
			if (!t) return
			if (!this.userId || !this.sessionId) {
				uni.showToast({ title: '请先登录', icon: 'none' })
				return
			}
			this.inputText = ''
			this.toolbarOpen = false
			try {
				await sendMessage(this.userId, { sessionId: Number(this.sessionId), content: t })
				// 重新加载消息以获取服务端数据
				await this.loadMessages()
			} catch (e) {
				uni.showToast({ title: '发送失败', icon: 'none' })
			}
		},

		pickImage() {
			uni.chooseImage({ count: 9, sizeType: ['compressed'], sourceType: ['album'], success: (res) => {
				uni.showToast({ title: '图片消息后端暂未支持', icon: 'none' })
				this.toolbarOpen = false
			}})
		},
		takePhoto() {
			uni.chooseImage({ count: 1, sourceType: ['camera'], success: (res) => {
				uni.showToast({ title: '图片消息后端暂未支持', icon: 'none' })
				this.toolbarOpen = false
			}})
		},

		switchInputMode() {
			this.inputMode = this.inputMode === 'text' ? 'voice' : 'text'
			this.toolbarOpen = false
		},
		startRecord() {
			this.recording = true
			this.recordTime = 0
			this.recordTimer = setInterval(() => { this.recordTime++; if (this.recordTime >= 60) this.stopRecord() }, 1000)
			const rm = uni.getRecorderManager()
			rm.start({ format: 'mp3' })
			this._recorder = rm
			this._recorder.onStop((res) => {
				if (this.recordTime >= 1) {
					uni.showToast({ title: '语音消息后端暂未支持', icon: 'none' })
				}
			})
		},
		stopRecord() {
			if (!this.recording) return
			this.recording = false
			clearInterval(this.recordTimer)
			if (this._recorder) this._recorder.stop()
			if (this.recordTime < 1) uni.showToast({ title: '说话时间太短', icon: 'none' })
		},
		playVoice() {
			uni.showToast({ title: '语音播放', icon: 'none' })
		},

		sendLocation() {
			uni.chooseLocation({ success: (res) => {
				uni.showToast({ title: '位置消息后端暂未支持', icon: 'none' })
				this.toolbarOpen = false
			}})
		},
		featurePending(name) {
			uni.showToast({ title: name + '功能后端暂未支持', icon: 'none' })
			this.toolbarOpen = false
		},
		openLocation(msg) {
			if (msg.locationData) {
				uni.openLocation({ latitude: msg.locationData.lat, longitude: msg.locationData.lng, name: msg.locationData.name, address: msg.locationData.address })
			}
		},

		previewImage(msg) {
			uni.previewImage({ current: msg.content, urls: this.msgs.filter(m => m.type === 'image').map(m => m.content) })
		},

		toggleToolbar() {
			if (this.inputMode === 'voice') this.inputMode = 'text'
			this.toolbarOpen = !this.toolbarOpen
			if (this.toolbarOpen) this.$nextTick(() => this.scrollToBottom())
		},

		formatTime(ts) {
			const d = new Date(ts), now = new Date()
			const pad = n => String(n).padStart(2, '0')
			const hm = pad(d.getHours()) + ':' + pad(d.getMinutes())
			if (d.toDateString() === now.toDateString()) return '今天 ' + hm
			const y = new Date(now); y.setDate(now.getDate() - 1)
			if (d.toDateString() === y.toDateString()) return '昨天 ' + hm
			return (d.getMonth()+1) + '/' + d.getDate() + ' ' + hm
		},

		scrollToBottom() {
			this.scrollToId = ''
			this.$nextTick(() => { this.scrollToId = 'msg-bottom' })
		}
	}
}
</script>

<style>
/* ===== 页面整体 ===== */
.chat-page {
	height: 100vh;
	display: flex;
	flex-direction: column;
	background: #f0f0f0;
	box-sizing: border-box;
	overflow: hidden;
}

/* ===== 顶部栏 ===== */
.chat-header {
	height: 88rpx;
	background: #ffffff;
	display: flex;
	align-items: center;
	padding: 0 24rpx;
	border-bottom: 1rpx solid #eee;
	position: fixed;
	left: 0;
	right: 0;
	z-index: 10;
	box-sizing: border-box;
}
.header-back { width: 60rpx; display: flex; align-items: center; justify-content: center; margin-left: 8rpx; }
.back-icon { font-size: 80rpx; color: #000; font-weight: 300; line-height: 0.9; }
.header-info { flex: 1; display: flex; align-items: center; gap: 16rpx; overflow: hidden; }
.header-avatar {
	width: 80rpx; height: 80rpx; background: #e8f5ee; border-radius: 50%;
	display: flex; align-items: center; justify-content: center; font-size: 36rpx; flex-shrink: 0; overflow: hidden;
}
.header-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
.header-avatar-emoji { font-size: 36rpx; }
.header-text { display: flex; flex-direction: column; overflow: hidden; }
.header-name { font-size: 34rpx; color: #333; font-weight: bold; }

/* ===== 消息区 ===== */
.msg-scroll { flex: 1; padding: 20rpx 20rpx 0; overflow-y: auto; box-sizing: border-box; }
.scroll-anchor { height: 1rpx; }
.time-divider { text-align: center; padding: 24rpx 0 16rpx; }
.time-text { font-size: 22rpx; color: #bbb; background: #f0f0f0; padding: 4rpx 20rpx; border-radius: 4rpx; }
.system-msg { text-align: center; padding: 16rpx 0; }
.system-text { font-size: 22rpx; color: #ccc; }

/* ===== 消息行 ===== */
.msg-row { display: flex; align-items: flex-start; margin-bottom: 24rpx; box-sizing: border-box; }
.msg-self { justify-content: flex-end; }

.msg-avatar {
	width: 80rpx; height: 80rpx; background: #e8f5ee; border-radius: 50%;
	display: flex; align-items: center; justify-content: center; font-size: 36rpx; flex-shrink: 0; overflow: hidden;
}
.msg-avatar-img { width: 100%; height: 100%; border-radius: 50%; }
.msg-avatar-emoji { font-size: 36rpx; }
.msg-row:not(.msg-self) .msg-avatar { margin-right: 16rpx; }
.msg-self .msg-avatar { margin-left: 16rpx; background: #e3f2fd; }

.msg-bubble-wrap { max-width: 72%; box-sizing: border-box; }

/* ===== 气泡 ===== */
.bubble { padding: 24rpx 28rpx; border-radius: 20rpx; word-break: break-all; position: relative; box-sizing: border-box; }
.msg-row:not(.msg-self) .bubble { background: #ffffff; border-top-left-radius: 6rpx; }
.bubble-self { background: #b3e5c8; border-top-right-radius: 6rpx; }
.msg-text { font-size: 32rpx; color: #333; line-height: 1.6; }

/* 图片 */
.bubble-media { padding: 0; overflow: hidden; position: relative; border-radius: 16rpx; line-height: 0; }
.msg-image { display: block; border-radius: 16rpx; max-width: 100%; }

/* 语音 */
.bubble-voice { display: flex; align-items: center; gap: 10rpx; min-width: 120rpx; }
.voice-icon { font-size: 34rpx; }
.voice-playing { animation: vB 0.6s infinite alternate; }
@keyframes vB { from { opacity: 0.3; } to { opacity: 1; } }
.voice-dur { font-size: 26rpx; color: #666; }

/* ===== 位置 ===== */
.bubble-card {
	padding: 0; border-radius: 16rpx; overflow: hidden; width: 420rpx;
	max-width: 100%; background: #ffffff; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05); box-sizing: border-box;
}
.loc-card { padding: 24rpx; display: flex; align-items: flex-start; gap: 16rpx; }
.loc-icon { font-size: 40rpx; flex-shrink: 0; margin-top: 4rpx; }
.loc-info { flex: 1; overflow: hidden; }
.loc-name { font-size: 28rpx; color: #333; font-weight: bold; display: block; margin-bottom: 6rpx; }
.loc-addr { font-size: 24rpx; color: #999; line-height: 1.4; }

/* ===== 空状态 ===== */
.empty-chat { display: flex; align-items: center; justify-content: center; padding-top: 300rpx; }
.empty-text { font-size: 28rpx; color: #ccc; }

/* ===== 录音浮层 ===== */
.record-overlay {
	position: fixed; top: 0; left: 0; right: 0; bottom: 0;
	background: rgba(0,0,0,0.6); display: flex; align-items: center; justify-content: center; z-index: 100;
}
.record-box {
	width: 320rpx; height: 320rpx; background: rgba(0,0,0,0.85); border-radius: 24rpx;
	display: flex; flex-direction: column; align-items: center; justify-content: center; gap: 20rpx;
}
.record-wave { display: flex; align-items: center; gap: 8rpx; height: 80rpx; }
.wave-bar {
	width: 8rpx; background: #4cd964; border-radius: 4rpx;
	animation: wA 1.2s infinite ease-in-out;
}
@keyframes wA { 0%,100% { height: 20rpx; } 50% { height: 80rpx; } }
.record-hint { font-size: 22rpx; color: rgba(255,255,255,0.6); }
.record-time { font-size: 40rpx; color: #fff; font-weight: bold; }

/* ===== 底部输入区 ===== */
.input-area { background: #ffffff; border-top: 1rpx solid #eee; box-sizing: border-box; }
.input-row { display: flex; align-items: center; padding: 16rpx 16rpx; gap: 12rpx; box-sizing: border-box; }

.mode-switch { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.mode-icon { font-size: 44rpx; color: #999; line-height: 1; }

.input-box { flex: 1; }
.text-input {
	width: 100%; height: 72rpx; background: #f5f5f5; border-radius: 8rpx;
	padding: 0 24rpx; font-size: 28rpx; box-sizing: border-box;
}
.voice-btn {
	width: 100%; height: 72rpx; line-height: 72rpx; text-align: center;
	background: #f5f5f5; border-radius: 8rpx; font-size: 28rpx; color: #666;
	box-sizing: border-box;
}

.plus-btn {
	width: 56rpx; height: 56rpx;
	display: flex; align-items: center; justify-content: center;
	flex-shrink: 0; transition: all 0.2s;
}
.plus-icon { font-size: 48rpx; color: #999; line-height: 1; }
.plus-active { background: #f5f5f5; border-color: #bbb; }

/* ===== 扩展面板 ===== */
.toolbar-panel {
	display: flex; flex-wrap: wrap; padding: 24rpx 20rpx 40rpx;
	border-top: 1rpx solid #f5f5f5; background: #fafafa; box-sizing: border-box;
}
.tb-item { width: 25%; display: flex; flex-direction: column; align-items: center; gap: 12rpx; margin-bottom: 30rpx; }
.tb-icon {
	width: 100rpx; height: 100rpx; border-radius: 24rpx;
	display: flex; align-items: center; justify-content: center;
}
.tb-emoji { font-size: 44rpx; }
.tb-label { font-size: 22rpx; color: #666; }
</style>

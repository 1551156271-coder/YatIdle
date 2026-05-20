<template>
	<view class="chat-page" :style="{ paddingTop: statusBarHeight + 'px' }">
		<!-- 顶部联系人栏 -->
		<view class="chat-header" :style="{ top: statusBarHeight + 'px', paddingRight: headerRightPad + 'px' }">
			<view class="header-back" @click="goBack">
				<text class="back-icon">‹</text>
			</view>
			<view class="header-info" @click="goToProfile">
				<view class="header-avatar">{{ contactInfo.avatar }}</view>
				<view class="header-text">
					<text class="header-name">{{ contactInfo.name }}</text>
				</view>
			</view>
			<view class="header-actions" @click="showMoreActions">
				<text class="header-action-icon">⋯</text>
			</view>
		</view>

		<!-- 消息区域 -->
		<scroll-view
			class="msg-scroll"
			scroll-y
			:scroll-into-view="scrollToId"
			:scroll-with-animation="scrollAnimated"
			@scrolltoupper="loadMoreHistory"
			:upper-threshold="50"
		>
			<view v-if="loadingHistory" class="history-tip">加载中...</view>
			<view v-else-if="!hasMoreHistory && msgs.length > 0" class="history-tip history-end">— 以上为历史消息 —</view>

			<block v-for="msg in msgs" :key="msg.id">
				<view :id="'msg-' + msg.id">
					<view v-if="msg.showTime" class="time-divider">
						<text class="time-text">{{ msg.timeLabel }}</text>
					</view>

					<view v-if="msg.type === 'system'" class="system-msg">
						<text class="system-text">{{ msg.content }}</text>
					</view>

					<view v-else class="msg-row" :class="{ 'msg-self': msg.fromMe }">
						<view v-if="!msg.fromMe" class="msg-avatar" @click="goToProfile">{{ contactInfo.avatar }}</view>

						<view class="msg-bubble-wrap">
							<!-- 文本 -->
							<view v-if="msg.type === 'text'" class="bubble bubble-text" :class="{ 'bubble-self': msg.fromMe }">
								<text class="msg-text">{{ msg.content }}</text>
							</view>

							<!-- 图片 -->
							<view v-else-if="msg.type === 'image'" class="bubble bubble-media" @click="previewImage(msg)">
								<image class="msg-image" :src="msg.content" mode="aspectFill" :style="msg.imgStyle"></image>
							</view>

							<!-- 视频 -->
							<view v-else-if="msg.type === 'video'" class="bubble bubble-media" @click="previewVideo(msg)">
								<image class="msg-image" :src="msg.thumb" mode="aspectFill" :style="msg.imgStyle"></image>
								<view class="play-icon-wrap"><text class="play-icon">▶</text></view>
								<text class="video-duration">{{ msg.duration }}</text>
							</view>

							<!-- 语音 -->
							<view v-else-if="msg.type === 'voice'" class="bubble bubble-voice" :class="{ 'bubble-self': msg.fromMe }" @click="playVoice(msg)">
								<text class="voice-icon" :class="{ 'voice-playing': msg.playing }">&#x1F50A;</text>
								<text class="voice-dur">{{ msg.duration }}"</text>
								<view v-if="msg.fromMe && msg.unread" class="voice-dot"></view>
							</view>

							<!-- 订单卡片 -->
							<view v-else-if="msg.type === 'order'" class="bubble bubble-card" @click="viewOrder(msg)">
								<view class="card-top">
									<text class="card-label">订单信息</text>
									<text class="card-status" :class="msg.orderData.statusClass">{{ msg.orderData.status }}</text>
								</view>
								<image class="card-thumb" :src="msg.orderData.image" mode="aspectFill"></image>
								<view class="card-foot">
									<text class="card-title">{{ msg.orderData.title }}</text>
									<view class="card-row">
										<text class="card-price">¥{{ msg.orderData.price }}</text>
										<text class="card-tag">{{ msg.orderData.campus }}</text>
									</view>
								</view>
							</view>

							<!-- 商品卡片 -->
							<view v-else-if="msg.type === 'goods'" class="bubble bubble-card" @click="viewGoods(msg)">
								<image class="card-thumb" :src="msg.goodsData.image" mode="aspectFill"></image>
								<view class="card-foot">
									<text class="card-title">{{ msg.goodsData.title }}</text>
									<view class="card-row">
										<text class="card-price">¥{{ msg.goodsData.price }}</text>
										<text class="card-tag">{{ msg.goodsData.campus }}</text>
									</view>
								</view>
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

						<view v-if="msg.fromMe" class="msg-avatar msg-avatar-self">🎓</view>
					</view>
				</view>
			</block>

			<view v-if="msgs.length === 0 && !loadingHistory" class="empty-chat">
				<text class="empty-text">开始聊天吧~</text>
			</view>

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
					<text v-if="inputMode === 'text'" class="mode-icon mode-mic"></text>
					<text v-else class="mode-icon mode-keyboard"></text>
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
					<text class="plus-text">{{ toolbarOpen ? '✕' : '+' }}</text>
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
				<view class="tb-item" @click="recordVideo">
					<view class="tb-icon" style="background:#fff3e0;"><text class="tb-emoji">🎬</text></view>
					<text class="tb-label">视频</text>
				</view>
				<view class="tb-item" @click="shareOrder">
					<view class="tb-icon" style="background:#fce4ec;"><text class="tb-emoji">📋</text></view>
					<text class="tb-label">订单</text>
				</view>
				<view class="tb-item" @click="shareGoods">
					<view class="tb-icon" style="background:#f3e5f5;"><text class="tb-emoji">🛍</text></view>
					<text class="tb-label">商品</text>
				</view>
				<view class="tb-item" @click="sendLocation">
					<view class="tb-icon" style="background:#e0f2f1;"><text class="tb-emoji">📍</text></view>
					<text class="tb-label">位置</text>
				</view>
				<view class="tb-item" @click="startVideoCall">
					<view class="tb-icon" style="background:#ede7f6;"><text class="tb-emoji">📹</text></view>
					<text class="tb-label">视频通话</text>
				</view>
				<view class="tb-item" @click="startVoiceCall">
					<view class="tb-icon" style="background:#e1f5fe;"><text class="tb-emoji">📞</text></view>
					<text class="tb-label">语音通话</text>
				</view>
			</view>
		</view>
	</view>
</template>

<script>
export default {
	data() {
		return {
			statusBarHeight: 0,
			headerRightPad: 20,
			scrollAnimated: false,
			contactInfo: { id: 2, avatar: '🤝', name: '张三（卖家）' },
			msgs: [],
			inputText: '',
			inputMode: 'text',
			toolbarOpen: false,
			recording: false,
			recordTime: 0,
			recordTimer: null,
			scrollToId: '',
			loadingHistory: false,
			hasMoreHistory: true,
			msgIdCounter: 100
		}
	},
	onLoad(options) {
		const sysInfo = uni.getSystemInfoSync()
		this.statusBarHeight = sysInfo.statusBarHeight || 20
		// 避开微信胶囊按钮
		const menuButton = uni.getMenuButtonBoundingClientRect()
		this.headerRightPad = sysInfo.windowWidth - menuButton.left + 12
		if (options.id) {
			this.loadContactInfo(options.id)
		}
		this.loadMockMessages()
		this.$nextTick(() => this.scrollToBottom())
		// 延迟启用滚动动画，避免初始加载时可见的滚动效果
		setTimeout(() => { this.scrollAnimated = true }, 500)
	},
	onUnload() {
		clearInterval(this.recordTimer)
	},
	methods: {
		goBack() { uni.navigateBack() },
		goToProfile() { uni.navigateTo({ url: '/pages/profile/profile?id=' + this.contactInfo.id }) },
		showMoreActions() {
			uni.showActionSheet({
				itemList: ['举报', '拉黑', '清空聊天记录'],
				success: (res) => {
					if (res.tapIndex === 2) {
						this.msgs = []
						uni.showToast({ title: '已清空', icon: 'none' })
					} else {
						uni.showToast({ title: '功能即将上线', icon: 'none' })
					}
				}
			})
		},

		loadContactInfo(id) {
			const m = { '1': { avatar: '🎓', name: '中大二手交易助手' }, '2': { avatar: '🤝', name: '张三（卖家）' }, '3': { avatar: '📚', name: '李四（买家）' } }
			if (m[id]) this.contactInfo = m[id]
		},

		loadMockMessages() {
			const now = Date.now(), m = 60 * 1000
			this.msgs = [
				{ id: 1, fromMe: false, type: 'text', content: '你好，我对你在闲鸭蛋上发布的公路自行车很感兴趣', time: now - 45*m, showTime: true, timeLabel: '今天 16:05' },
				{ id: 2, fromMe: true,  type: 'text', content: '你好！车子还在的，你想了解什么？', time: now - 43*m, showTime: false },
				{ id: 3, fromMe: false, type: 'text', content: '能看一下更多实拍图吗？变速器是什么牌子的？', time: now - 40*m, showTime: false },
				{ id: 4, fromMe: true,  type: 'image', content: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=600&auto=format&fit=crop', width: 400, height: 300, time: now - 38*m, showTime: false },
				{ id: 5, fromMe: true,  type: 'text', content: '这是刚刚拍的细节图，禧玛诺入门级变速，用着很顺滑', time: now - 37*m, showTime: false },
				{ id: 6, fromMe: false, type: 'voice', content: '', duration: 12, unread: false, time: now - 30*m, showTime: false },
				{ id: 7, fromMe: true,  type: 'voice', content: '', duration: 8, unread: true, time: now - 28*m, showTime: false },
				{ id: 8, fromMe: true,  type: 'text', content: '最低 240 出，送你一个新买的挡泥板', time: now - 27*m, showTime: false },
				{ id: 9, fromMe: false, type: 'location', locationData: { name: '东校园第三食堂', address: '广州市番禺区大学城外环东路132号', lat: 23.05, lng: 113.39 }, time: now - 20*m, showTime: false },
				{ id: 10, fromMe: false, type: 'text', content: '明天中午 12 点，三饭门口见？', time: now - 19*m, showTime: false },
				{ id: 11, fromMe: true,  type: 'order', orderData: { id: '20240509123456', image: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop', title: '九成新公路自行车', price: '240.00', campus: '东校园', status: '待确认', statusClass: 'status-pending' }, time: now - 15*m, showTime: false },
				{ id: 12, fromMe: false, type: 'text', content: '好的，明天见！😄', time: now - 5*m, showTime: false }
			]
			// Pre-compute image styles
			this.msgs.forEach(msg => {
				if (msg.type === 'image' || msg.type === 'video') {
					msg.imgStyle = this.computeImageStyle(msg)
				}
			})
		},

		sendText() {
			const t = this.inputText.trim()
			if (!t) return
			this.addMsg({ type: 'text', content: t, fromMe: true })
			this.inputText = ''
			this.toolbarOpen = false
		},
		pickImage() {
			uni.chooseImage({ count: 9, sizeType: ['compressed'], sourceType: ['album'], success: (res) => {
				res.tempFilePaths.forEach(p => this.addMsg({ type: 'image', content: p, fromMe: true, width: 300, height: 400 }))
				this.toolbarOpen = false
			}})
		},
		takePhoto() {
			uni.chooseImage({ count: 1, sourceType: ['camera'], success: (res) => {
				this.addMsg({ type: 'image', content: res.tempFilePaths[0], fromMe: true, width: 300, height: 400 })
				this.toolbarOpen = false
			}})
		},
		recordVideo() {
			uni.chooseVideo({ sourceType: ['album','camera'], maxDuration: 60, success: (res) => {
				const d = Math.floor(res.duration)
				const ds = String(Math.floor(d/60)).padStart(2,'0') + ':' + String(d%60).padStart(2,'0')
				this.addMsg({ type: 'video', content: res.tempFilePath, thumb: res.thumbTempFilePath || res.tempFilePath, duration: ds, width: res.width||300, height: res.height||400, fromMe: true })
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
				if (this.recordTime >= 1) this.addMsg({ type: 'voice', content: res.tempFilePath, duration: this.recordTime, unread: false, fromMe: true })
			})
		},
		stopRecord() {
			if (!this.recording) return
			this.recording = false
			clearInterval(this.recordTimer)
			if (this._recorder) this._recorder.stop()
			if (this.recordTime < 1) uni.showToast({ title: '说话时间太短', icon: 'none' })
		},
		playVoice(msg) {
			const audio = uni.createInnerAudioContext()
			audio.src = msg.content
			audio.play()
			msg.playing = true
			audio.onEnded(() => { msg.playing = false })
			audio.onStop(() => { msg.playing = false })
		},

		shareOrder() {
			this.addMsg({ type: 'order', fromMe: true, orderData: { id: '20240509'+String(Date.now()).slice(-6), image: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop', title: '九成新公路自行车', price: '240.00', campus: '东校园', status: '待确认', statusClass: 'status-pending' }})
			this.toolbarOpen = false
		},
		viewOrder() { uni.showToast({ title: '订单详情即将上线', icon: 'none' }) },
		shareGoods() {
			this.addMsg({ type: 'goods', fromMe: true, goodsData: { image: 'https://images.unsplash.com/photo-1485965120184-e220f721d03e?q=80&w=400&auto=format&fit=crop', title: '九成新公路自行车，带锁和挡泥板', price: '268.00', campus: '东校园' }})
			this.toolbarOpen = false
		},
		viewGoods() { uni.navigateTo({ url: '/pages/goods-detail/goods-detail?id=1' }) },

		sendLocation() {
			uni.chooseLocation({ success: (res) => {
				this.addMsg({ type: 'location', fromMe: true, locationData: { name: res.name || '已选位置', address: res.address || '', lat: res.latitude, lng: res.longitude }})
				this.toolbarOpen = false
			}})
		},
		openLocation(msg) {
			uni.openLocation({ latitude: msg.locationData.lat, longitude: msg.locationData.lng, name: msg.locationData.name, address: msg.locationData.address })
		},

		startVoiceCall() { uni.showToast({ title: '语音通话即将上线', icon: 'none' }); this.toolbarOpen = false },
		startVideoCall() { uni.showToast({ title: '视频通话即将上线', icon: 'none' }); this.toolbarOpen = false },

		previewImage(msg) {
			uni.previewImage({ current: msg.content, urls: this.msgs.filter(m => m.type === 'image').map(m => m.content) })
		},
		previewVideo() { uni.showToast({ title: '视频播放即将上线', icon: 'none' }) },

		computeImageStyle(msg) {
			const maxW = 340, maxH = 440
			let w = msg.width || 300, h = msg.height || 400
			if (w > maxW) { h = h * (maxW / w); w = maxW }
			if (h > maxH) { w = w * (maxH / h); h = maxH }
			return { width: Math.round(w) + 'rpx', height: Math.round(h) + 'rpx' }
		},

		toggleToolbar() {
			if (this.inputMode === 'voice') this.inputMode = 'text'
			this.toolbarOpen = !this.toolbarOpen
			if (this.toolbarOpen) this.$nextTick(() => this.scrollToBottom())
		},

		addMsg(data) {
			const msg = { id: ++this.msgIdCounter, fromMe: data.fromMe, type: data.type, content: data.content || '', time: Date.now(), showTime: false }
			const last = this.msgs[this.msgs.length - 1]
			if (!last || (msg.time - last.time > 5 * 60 * 1000)) {
				msg.showTime = true
				msg.timeLabel = this.formatTime(msg.time)
			}
			if (data.type === 'image' || data.type === 'video') {
				msg.width = data.width; msg.height = data.height
				msg.imgStyle = this.computeImageStyle({ width: data.width, height: data.height })
			}
			if (data.type === 'video') { msg.thumb = data.thumb; msg.duration = data.duration }
			if (data.type === 'voice') { msg.duration = data.duration; msg.unread = data.unread }
			if (data.type === 'order') msg.orderData = data.orderData
			if (data.type === 'goods') msg.goodsData = data.goodsData
			if (data.type === 'location') msg.locationData = data.locationData
			this.msgs.push(msg)
			this.$nextTick(() => this.scrollToBottom())
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
		},

		loadMoreHistory() {
			if (this.loadingHistory || !this.hasMoreHistory) return
			this.loadingHistory = true
			setTimeout(() => { this.hasMoreHistory = false; this.loadingHistory = false; uni.showToast({ title: '没有更多了', icon: 'none' }) }, 800)
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
	padding: 0 20rpx;
	border-bottom: 1rpx solid #eee;
	position: fixed;
	left: 0;
	right: 0;
	z-index: 10;
	box-sizing: border-box;
}
.header-back { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.back-icon { font-size: 48rpx; color: #333; font-weight: 300; }
.header-info { flex: 1; display: flex; align-items: center; gap: 16rpx; overflow: hidden; }
.header-avatar {
	width: 80rpx; height: 80rpx; background: #e8f5ee; border-radius: 50%;
	display: flex; align-items: center; justify-content: center; font-size: 36rpx; flex-shrink: 0;
}
.header-text { display: flex; flex-direction: column; overflow: hidden; }
.header-name { font-size: 34rpx; color: #333; font-weight: bold; }
.header-actions { width: 60rpx; height: 60rpx; display: flex; align-items: center; justify-content: center; }
.header-action-icon { font-size: 44rpx; color: #666; font-weight: bold; }

/* ===== 消息区 ===== */
.msg-scroll { flex: 1; padding: 20rpx 20rpx 0; overflow-y: auto; box-sizing: border-box; }
.scroll-anchor { height: 1rpx; }
.history-tip { text-align: center; padding: 20rpx; font-size: 22rpx; color: #ccc; }
.history-end { color: #ddd; }
.time-divider { text-align: center; padding: 24rpx 0 16rpx; }
.time-text { font-size: 22rpx; color: #bbb; background: #f0f0f0; padding: 4rpx 20rpx; border-radius: 4rpx; }
.system-msg { text-align: center; padding: 16rpx 0; }
.system-text { font-size: 22rpx; color: #ccc; }

/* ===== 消息行 ===== */
.msg-row { display: flex; align-items: flex-start; margin-bottom: 24rpx; box-sizing: border-box; }
.msg-self { justify-content: flex-end; }

.msg-avatar {
	width: 80rpx; height: 80rpx; background: #e8f5ee; border-radius: 50%;
	display: flex; align-items: center; justify-content: center; font-size: 36rpx; flex-shrink: 0;
}
.msg-row:not(.msg-self) .msg-avatar { margin-right: 16rpx; }
.msg-self .msg-avatar { margin-left: 16rpx; background: #e3f2fd; }

.msg-bubble-wrap { max-width: 72%; box-sizing: border-box; }

/* ===== 气泡 ===== */
.bubble { padding: 24rpx 28rpx; border-radius: 20rpx; word-break: break-all; position: relative; box-sizing: border-box; }
.msg-row:not(.msg-self) .bubble { background: #ffffff; border-top-left-radius: 6rpx; }
.bubble-self { background: #b3e5c8; border-top-right-radius: 6rpx; }
.msg-text { font-size: 32rpx; color: #333; line-height: 1.6; }

/* 图片 / 视频 */
.bubble-media { padding: 0; overflow: hidden; position: relative; border-radius: 16rpx; line-height: 0; }
.msg-image { display: block; border-radius: 16rpx; max-width: 100%; }
.play-icon-wrap {
	position: absolute; top: 50%; left: 50%; transform: translate(-50%, -50%);
	width: 80rpx; height: 80rpx; background: rgba(0,0,0,0.5); border-radius: 50%;
	display: flex; align-items: center; justify-content: center;
}
.play-icon { font-size: 30rpx; color: #fff; }
.video-duration {
	position: absolute; bottom: 12rpx; right: 12rpx;
	background: rgba(0,0,0,0.6); color: #fff; font-size: 20rpx;
	padding: 4rpx 12rpx; border-radius: 6rpx;
}

/* 语音 */
.bubble-voice { display: flex; align-items: center; gap: 10rpx; min-width: 120rpx; }
.voice-icon { font-size: 34rpx; }
.voice-playing { animation: vB 0.6s infinite alternate; }
@keyframes vB { from { opacity: 0.3; } to { opacity: 1; } }
.voice-dur { font-size: 26rpx; color: #666; }
.voice-dot {
	width: 12rpx; height: 12rpx; background: #e74c3c; border-radius: 50%;
	position: absolute; top: -4rpx; right: -4rpx;
}

/* ===== 卡片 ===== */
.bubble-card {
	padding: 0; border-radius: 16rpx; overflow: hidden; width: 420rpx;
	max-width: 100%; background: #ffffff; box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.05); box-sizing: border-box;
}
.card-top { display: flex; justify-content: space-between; align-items: center; padding: 20rpx 20rpx 0; }
.card-label { font-size: 24rpx; color: #999; }
.card-status { font-size: 22rpx; padding: 4rpx 16rpx; border-radius: 20rpx; }
.status-pending { color: #f0ad4e; background: #fcf8e3; }
.card-thumb { width: 100%; height: 260rpx; background: #f5f5f5; display: block; }
.card-foot { padding: 16rpx 20rpx 20rpx; }
.card-title {
	font-size: 28rpx; color: #333; line-height: 1.5;
	display: -webkit-box; -webkit-box-orient: vertical; -webkit-line-clamp: 2;
	overflow: hidden; margin-bottom: 12rpx;
}
.card-row { display: flex; justify-content: space-between; align-items: center; }
.card-price { font-size: 32rpx; color: #e74c3c; font-weight: bold; }
.card-tag { font-size: 22rpx; color: #999; background: #f5f5f5; padding: 4rpx 12rpx; border-radius: 6rpx; }

/* 位置 */
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

/* 语音 / 键盘切换（左侧） */
.mode-switch { width: 56rpx; height: 56rpx; display: flex; align-items: center; justify-content: center; flex-shrink: 0; }
.mode-icon { display: block; }
.mode-mic {
	width: 28rpx; height: 36rpx; border: 3rpx solid #999; border-radius: 14rpx;
	position: relative; box-sizing: border-box;
}
.mode-mic::after {
	content: '';
	position: absolute; bottom: -10rpx; left: 50%; transform: translateX(-50%);
	width: 3rpx; height: 8rpx; background: #999; border-radius: 0 0 2rpx 2rpx;
}
.mode-keyboard {
	width: 36rpx; height: 28rpx; border: 3rpx solid #999; border-radius: 4rpx;
	position: relative; box-sizing: border-box;
}
.mode-keyboard::before {
	content: '';
	position: absolute; top: 4rpx; left: 6rpx; right: 6rpx;
	height: 3rpx; background: #999; border-radius: 2rpx;
}
.mode-keyboard::after {
	content: '';
	position: absolute; bottom: 4rpx; left: 6rpx; right: 6rpx;
	height: 3rpx; background: #999; border-radius: 2rpx;
}

/* 输入框 */
.input-box { flex: 1; }
.text-input {
	width: 100%; height: 72rpx; background: #f5f5f5; border-radius: 8rpx;
	padding: 0 20rpx; font-size: 28rpx; box-sizing: border-box;
}
.voice-btn {
	width: 100%; height: 72rpx; line-height: 72rpx; text-align: center;
	background: #f5f5f5; border-radius: 8rpx; font-size: 28rpx; color: #666;
	box-sizing: border-box;
}

/* + 按钮（右侧，圆形） */
.plus-btn {
	width: 56rpx; height: 56rpx;
	border: 2rpx solid #ccc; border-radius: 50%;
	display: flex; align-items: center; justify-content: center;
	flex-shrink: 0; transition: all 0.2s;
}
.plus-btn:active { background: #f5f5f5; }
.plus-text { font-size: 38rpx; color: #999; line-height: 1; font-weight: 300; }
.plus-active { background: #f5f5f5; border-color: #bbb; }
.plus-active .plus-text { font-size: 26rpx; color: #666; }

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

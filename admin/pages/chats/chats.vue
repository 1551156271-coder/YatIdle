<template>
  <admin-layout title="聊天审计" active="/pages/chats/chats">
    <view class="toolbar">
      <input v-model.number="query.userId" placeholder="用户ID" @confirm="search" />
      <input v-model.number="query.itemId" placeholder="商品ID" @confirm="search" />
      <input v-model.number="query.wantedId" placeholder="求购ID" @confirm="search" />
      <button class="primary" @click="search">查询</button>
      <button @click="reset">重置</button>
    </view>

    <view class="split">
      <view>
        <view v-if="loading" class="loading">正在加载会话...</view>
        <view v-else-if="records.length === 0" class="empty">暂无聊天会话</view>
        <view v-else class="table">
          <view class="tr th"><text>ID</text><text>商品</text><text>求购</text><text>买家</text><text>卖家</text><text>最近消息</text></view>
          <view v-for="s in records" :key="s.id" :class="['tr', selected && selected.id === s.id ? 'selected' : '']" @click="open(s)">
            <text>{{ s.id }}</text><text>{{ s.itemId || '-' }}</text><text>{{ s.wantedId || '-' }}</text><text>{{ s.buyerId }}</text><text>{{ s.sellerId }}</text><text>{{ s.lastMessage || '-' }}</text>
          </view>
        </view>
        <view class="pager">
          <button :disabled="query.page <= 1" @click="goPage(query.page - 1)">上一页</button>
          <text>第 {{ query.page }} / {{ pages }} 页，共 {{ total }} 条</text>
          <button :disabled="query.page >= pages" @click="goPage(query.page + 1)">下一页</button>
        </view>
      </view>

      <view class="messages">
        <view class="panel-title">消息记录</view>
        <view v-if="!selected" class="muted">请选择一个会话</view>
        <view v-else-if="messageLoading" class="muted">正在加载消息...</view>
        <view v-else-if="messages.length === 0" class="muted">暂无消息</view>
        <view v-else>
          <view v-for="m in messages" :key="m.id" class="message">
            <text>{{ m.senderId }} -> {{ m.receiverId }} · {{ m.messageType || 'TEXT' }}</text>
            <view v-if="messageImages(m).length" class="chat-image-list">
              <image v-for="(img, index) in messageImages(m)" :key="img + index" :src="img" mode="aspectFit" class="chat-preview-img" @click="previewImages(messageImages(m), index)"></image>
            </view>
            <view v-else>{{ m.content }}</view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="imageViewerVisible" class="image-viewer-mask" @click="closeImageViewer">
      <view class="image-viewer" @click.stop>
        <view class="image-viewer-header">
          <text>{{ imageViewerIndex + 1 }} / {{ imageViewerImages.length }}</text>
          <button @click="closeImageViewer">关闭</button>
        </view>
        <view class="image-viewer-body">
          <button v-if="imageViewerImages.length > 1" class="viewer-nav viewer-prev" @click="prevImage">上一张</button>
          <image v-if="currentViewerImage" :src="currentViewerImage" mode="aspectFit" class="viewer-img"></image>
          <button v-if="imageViewerImages.length > 1" class="viewer-nav viewer-next" @click="nextImage">下一张</button>
        </view>
      </view>
    </view>
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import { listSessions, listMessages } from '../../api/chats'

export default {
  components: { AdminLayout },
  data() {
    return {
      query: { userId: '', itemId: '', wantedId: '', page: 1, size: 10 },
      records: [],
      total: 0,
      pages: 1,
      loading: false,
      selected: null,
      messages: [],
      messageLoading: false,
      imageViewerVisible: false,
      imageViewerImages: [],
      imageViewerIndex: 0
    }
  },
  onShow() { this.load() },
  computed: {
    currentViewerImage() {
      return this.imageViewerImages[this.imageViewerIndex] || ''
    }
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const params = { ...this.query }
        if (params.userId === '') delete params.userId
        if (params.itemId === '') delete params.itemId
        if (params.wantedId === '') delete params.wantedId
        const data = await listSessions(params)
        this.records = data.records || []
        this.total = Number(data.total || 0)
        this.pages = Math.max(1, Number(data.pages || Math.ceil(this.total / this.query.size) || 1))
      } finally { this.loading = false }
    },
    search() { this.query.page = 1; this.load() },
    reset() { this.query = { userId: '', itemId: '', wantedId: '', page: 1, size: 10 }; this.selected = null; this.messages = []; this.load() },
    goPage(page) { if (page >= 1 && page <= this.pages) { this.query.page = page; this.load() } },
    async open(session) {
      this.selected = session
      this.messageLoading = true
      try { this.messages = await listMessages(session.id) || [] } finally { this.messageLoading = false }
    },
    messageImages(message) {
      const content = String(message.content || '').trim()
      if (!content) return []
      if ((message.messageType || '').toUpperCase() === 'IMAGE') return [this.resolveAssetUrl(content)]
      if (this.looksLikeImageUrl(content)) return [this.resolveAssetUrl(content)]
      try {
        const parsed = JSON.parse(content)
        const values = Array.isArray(parsed) ? parsed : [parsed.url || parsed.imageUrl || parsed.src]
        return values.filter(Boolean)
          .filter(value => this.looksLikeImageUrl(value))
          .map(value => this.resolveAssetUrl(value))
      } catch (e) {
        return []
      }
    },
    looksLikeImageUrl(value) {
      return /^https?:\/\//i.test(value || '') || /^\/?uploads\//i.test(value || '') || /\.(png|jpe?g|gif|webp)(\?.*)?$/i.test(value || '')
    },
    resolveAssetUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      if (url.startsWith('/')) return 'http://127.0.0.1:8080' + url
      return 'http://127.0.0.1:8080/' + url
    },
    previewImages(images, index) {
      const urls = (images || []).filter(Boolean)
      if (!urls.length) return
      this.imageViewerImages = urls
      this.imageViewerIndex = Math.min(Math.max(Number(index) || 0, 0), urls.length - 1)
      this.imageViewerVisible = true
    },
    closeImageViewer() {
      this.imageViewerVisible = false
      this.imageViewerImages = []
      this.imageViewerIndex = 0
    },
    prevImage() {
      if (!this.imageViewerImages.length) return
      this.imageViewerIndex = (this.imageViewerIndex + this.imageViewerImages.length - 1) % this.imageViewerImages.length
    },
    nextImage() {
      if (!this.imageViewerImages.length) return
      this.imageViewerIndex = (this.imageViewerIndex + 1) % this.imageViewerImages.length
    }
  }
}
</script>

<style scoped>
.split { display: grid; grid-template-columns: minmax(0, 1fr) 380px; gap: 16px; align-items: start; }
.tr { display: grid; grid-template-columns: 70px 90px 90px 90px 90px 1fr; align-items: center; min-height: 50px; padding: 0 14px; border-bottom: 1px solid #edf1f5; font-size: 14px; cursor: pointer; }
.th { background: #f8fafc; font-weight: 700; color: #4a5568; cursor: default; }
.selected { background: #eef8f2; }
.messages { min-height: 420px; padding: 16px; background: #fff; border: 1px solid #dfe5ec; border-radius: 8px; box-sizing: border-box; }
.panel-title { margin-bottom: 12px; font-weight: 700; }
.message { padding: 10px 0; border-bottom: 1px solid #edf1f5; }
.message text { color: #718096; font-size: 12px; }
.message view { margin-top: 6px; color: #17202a; line-height: 1.5; word-break: break-all; }
.chat-image-list { display: grid; grid-template-columns: repeat(auto-fill, 96px); gap: 10px; }
.chat-preview-img { width: 96px; height: 96px; display: block; border: 1px solid #dfe5ec; border-radius: 6px; background: #f8fafc; object-fit: contain; cursor: zoom-in; }
.chat-preview-img > div, .chat-preview-img img, .chat-preview-img .uni-image-img, .viewer-img > div, .viewer-img img, .viewer-img .uni-image-img { width: 100% !important; height: 100% !important; object-fit: contain !important; background-size: contain !important; background-position: center !important; background-repeat: no-repeat !important; }
::v-deep .chat-preview-img > div, ::v-deep .chat-preview-img img, ::v-deep .chat-preview-img .uni-image-img, ::v-deep .viewer-img > div, ::v-deep .viewer-img img, ::v-deep .viewer-img .uni-image-img { width: 100% !important; height: 100% !important; object-fit: contain !important; background-size: contain !important; background-position: center !important; background-repeat: no-repeat !important; }
.image-viewer-mask { position: fixed; z-index: 1200; left: 0; top: 0; right: 0; bottom: 0; display: flex; align-items: center; justify-content: center; padding: 24px; background: rgba(15, 23, 42, 0.76); }
.image-viewer { width: min(920px, calc(100vw - 48px)); max-height: calc(100vh - 48px); display: flex; flex-direction: column; border-radius: 8px; background: #111827; overflow: hidden; box-shadow: 0 18px 50px rgba(15, 23, 42, 0.35); }
.image-viewer-header { height: 48px; padding: 0 14px; display: flex; align-items: center; justify-content: space-between; color: #f8fafc; font-size: 14px; }
.image-viewer-header button { width: auto; min-width: 64px; height: 32px; color: #1f2937; background: #fff; }
.image-viewer-body { position: relative; min-height: 320px; max-height: calc(100vh - 112px); display: flex; align-items: center; justify-content: center; padding: 16px; overflow: hidden; background: #0f172a; }
.viewer-img { width: 100%; height: min(72vh, 680px); display: block; object-fit: contain; }
.viewer-nav { position: absolute; top: 50%; z-index: 1; width: auto; min-width: 72px; height: 36px; transform: translateY(-50%); color: #1f2937; background: rgba(255, 255, 255, 0.92); }
.viewer-prev { left: 16px; }
.viewer-next { right: 16px; }
@media (max-width: 1100px) { .split { grid-template-columns: 1fr; } }
</style>

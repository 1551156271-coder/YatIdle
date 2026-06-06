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
            <view>{{ m.content }}</view>
          </view>
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
  data() { return { query: { userId: '', itemId: '', wantedId: '', page: 1, size: 10 }, records: [], total: 0, pages: 1, loading: false, selected: null, messages: [], messageLoading: false } },
  onShow() { this.load() },
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
@media (max-width: 1100px) { .split { grid-template-columns: 1fr; } }
</style>

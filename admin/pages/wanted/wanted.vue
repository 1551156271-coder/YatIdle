<template>
  <admin-layout title="求购管理" active="/pages/wanted/wanted">
    <view class="toolbar">
      <input v-model="query.keyword" placeholder="求购标题" @confirm="search" />
      <view class="segmented">
        <text :class="{ active: query.status === '' }" @click="setFilter('status', '')">全部状态</text>
        <text :class="{ active: query.status === 'pending' }" @click="setFilter('status', 'pending')">待定</text>
        <text :class="{ active: query.status === 'active' }" @click="setFilter('status', 'active')">有效</text>
        <text :class="{ active: query.status === 'closed' }" @click="setFilter('status', 'closed')">关闭</text>
      </view>
      <view class="segmented category-filter">
        <text :class="{ active: query.categoryId === '' }" @click="setFilter('categoryId', '')">全部分类</text>
        <text v-for="c in categories" :key="c.id" :class="{ active: query.categoryId === c.id }" @click="setFilter('categoryId', c.id)">{{ c.name }}</text>
      </view>
      <button class="primary" @click="search">查询</button>
      <button @click="reset">重置</button>
    </view>

    <view v-if="loading" class="loading">正在加载求购...</view>
    <view v-else-if="records.length === 0" class="empty">暂无求购数据</view>
    <view v-else class="table">
      <view class="tr th"><text>ID</text><text>标题</text><text>分类</text><text>预算</text><text>状态</text><text>操作</text></view>
      <view v-for="w in records" :key="w.id" class="tr">
        <text>{{ w.id }}</text><text>{{ w.title || '-' }}</text><text>{{ categoryName(w.categoryId) }}</text>
        <text>{{ money(w.budgetMin) }} - {{ money(w.budgetMax) }}</text><text>{{ wantedStatusText(w.status) }}</text>
        <view class="ops">
          <button size="mini" @click="openDetail(w)">详情</button>
          <button size="mini" @click="openAction(w, 'closed')">关闭</button>
          <button size="mini" @click="openAction(w, 'active')">恢复</button>
          <button size="mini" class="danger" @click="openAction(w, 'delete')">删除</button>
        </view>
      </view>
    </view>

    <view class="pager">
      <button :disabled="query.page <= 1" @click="goPage(query.page - 1)">上一页</button>
      <text>第 {{ query.page }} / {{ pages }} 页，共 {{ total }} 条</text>
      <button :disabled="query.page >= pages" @click="goPage(query.page + 1)">下一页</button>
    </view>

    <view v-if="detailVisible" class="modal-mask" @click="closeDetail">
      <view class="modal" @click.stop>
        <view class="modal-header"><text class="modal-title">求购详情</text><button @click="closeDetail">关闭</button></view>
        <view class="modal-body">
          <view class="detail-grid">
            <view class="detail-item"><text class="detail-label">ID</text><text class="detail-value">{{ detail.id }}</text></view>
            <view class="detail-item"><text class="detail-label">标题</text><text class="detail-value">{{ detail.title || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">发布用户</text><text class="detail-value">{{ detail.userId || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">分类</text><text class="detail-value">{{ categoryName(detail.categoryId) }}</text></view>
            <view class="detail-item"><text class="detail-label">预算</text><text class="detail-value">{{ money(detail.budgetMin) }} - {{ money(detail.budgetMax) }}</text></view>
            <view class="detail-item"><text class="detail-label">状态</text><text class="detail-value">{{ wantedStatusText(detail.status) }}</text></view>
            <view class="detail-item wide"><text class="detail-label">描述</text><text class="detail-value">{{ detail.description || '-' }}</text></view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="actionVisible" class="modal-mask" @click="closeAction">
      <view class="modal" @click.stop>
        <view class="modal-header"><text class="modal-title">{{ actionTitle }}</text><button @click="closeAction">关闭</button></view>
        <view class="modal-body">
          <view class="detail-item wide"><text class="detail-label">操作对象</text><text class="detail-value">#{{ current.id }} {{ current.title }}</text></view>
          <textarea v-model="reason" placeholder="请输入操作原因" />
        </view>
        <view class="modal-footer"><button @click="closeAction">取消</button><button class="danger" :disabled="submitting" @click="submitAction">确认执行</button></view>
      </view>
    </view>
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import { listCategories } from '../../api/categories'
import { listWanted, getWantedDetail, updateWantedStatus, deleteWanted } from '../../api/wanted'

export default {
  components: { AdminLayout },
  data() {
    return {
      query: { keyword: '', categoryId: '', status: '', page: 1, size: 10 },
      categories: [], records: [], total: 0, pages: 1, loading: false,
      detailVisible: false, detail: {}, actionVisible: false, current: {}, nextStatus: '', reason: '', submitting: false
    }
  },
  onShow() { this.bootstrap() },
  computed: {
    actionTitle() {
      if (this.nextStatus === 'delete') return '删除求购'
      return this.nextStatus === 'closed' ? '关闭求购' : '恢复求购'
    }
  },
  methods: {
    async bootstrap() {
      if (!this.categories.length) this.categories = await listCategories() || []
      this.load()
    },
    async load() {
      this.loading = true
      try {
        const params = { ...this.query }
        if (params.categoryId === '') delete params.categoryId
        const data = await listWanted(params)
        this.records = data.records || []
        this.total = Number(data.total || 0)
        this.pages = Math.max(1, Number(data.pages || Math.ceil(this.total / this.query.size) || 1))
      } finally { this.loading = false }
    },
    search() { this.query.page = 1; this.load() },
    reset() { this.query = { keyword: '', categoryId: '', status: '', page: 1, size: 10 }; this.load() },
    setFilter(key, value) { this.query[key] = value; this.search() },
    goPage(page) { if (page >= 1 && page <= this.pages) { this.query.page = page; this.load() } },
    async openDetail(row) { this.detail = await getWantedDetail(row.id); this.detailVisible = true },
    closeDetail() { this.detailVisible = false; this.detail = {} },
    openAction(row, status) { this.current = row; this.nextStatus = status; this.reason = ''; this.actionVisible = true },
    closeAction() { this.actionVisible = false; this.current = {}; this.nextStatus = ''; this.reason = '' },
    async submitAction() {
      if (!this.reason.trim()) return uni.showToast({ title: '请填写操作原因', icon: 'none' })
      this.submitting = true
      try {
        const ok = await this.confirmTwice(this.actionTitle)
        if (!ok) return
        if (this.nextStatus === 'delete') await deleteWanted(this.current.id, { reason: this.reason })
        else await updateWantedStatus(this.current.id, { status: this.nextStatus, reason: this.reason })
        this.closeAction()
        await this.load()
      } finally { this.submitting = false }
    },
    confirmTwice(content) {
      return new Promise(resolve => uni.showModal({ title: '二次确认', content: `确认${content}？`, confirmColor: '#b42318', success: r => resolve(r.confirm), fail: () => resolve(false) }))
    },
    categoryName(id) { const hit = this.categories.find(c => c.id === id); return hit ? hit.name : (id ? `#${id}` : '-') },
    wantedStatusText(status) { return ({ pending: '待定', active: '有效', closed: '已关闭', sold: '已成交' }[status] || status || '-') },
    money(value) { return value == null || value === '' ? '-' : `¥${Number(value).toFixed(2)}` }
  }
}
</script>

<style scoped>
.category-filter { max-width: 520px; overflow-x: auto; }
.tr { display: grid; grid-template-columns: 76px 1fr 130px 180px 110px 300px; align-items: center; min-height: 50px; padding: 0 14px; border-bottom: 1px solid #edf1f5; font-size: 14px; }
.th { background: #f8fafc; font-weight: 700; color: #4a5568; }
</style>

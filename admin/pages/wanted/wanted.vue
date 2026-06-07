<template>
  <admin-layout title="求购管理" active="/pages/wanted/wanted">
    <view class="toolbar">
      <view class="toolbar-row search-row">
        <input v-model="query.keyword" placeholder="求购标题" @confirm="search" />
        <button class="primary" @click="search">查询</button>
        <button @click="reset">重置</button>
      </view>
      <view class="toolbar-row filter-row">
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
      </view>
    </view>

    <view v-if="loading" class="loading">正在加载求购...</view>
    <view v-else-if="records.length === 0" class="empty">暂无求购数据</view>
    <view v-else class="table">
      <view class="tr th"><text>ID</text><text>图片</text><text>标题</text><text>分类</text><text>预算</text><text>状态</text><text>操作</text></view>
      <view v-for="w in records" :key="w.id" class="tr">
        <text>{{ w.id }}</text>
        <view class="cover-cell">
          <image v-if="wantedCover(w) && !brokenWantedImages[w.id]" :src="wantedCover(w)" mode="aspectFit" class="cover-img" @error="markWantedImageBroken(w.id)" @click="previewImages([wantedCover(w)], 0)"></image>
          <text v-else-if="wantedCover(w)" class="cover-empty">图片加载失败</text>
          <text v-else class="cover-empty">无图</text>
        </view>
        <text>{{ w.title || '-' }}</text><text>{{ categoryName(w.categoryId) }}</text>
        <text>{{ money(w.budgetMin) }} - {{ money(w.budgetMax) }}</text><text>{{ wantedStatusText(w.status) }}</text>
        <view class="ops">
          <button size="mini" @click="openDetail(w)">详情</button>
          <button v-if="w.status !== 'closed'" size="mini" @click="openAction(w, 'closed')">关闭</button>
          <button v-else size="mini" @click="openAction(w, 'active')">恢复</button>
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
            <view class="detail-item wide">
              <text class="detail-label">图片</text>
              <view v-if="detailImages.length" class="image-list">
                <image v-for="(img, index) in detailImages" :key="img" :src="img" mode="aspectFit" class="preview-img" @click="previewImages(detailImages, index)"></image>
              </view>
              <text v-else class="detail-value muted">暂无图片</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <danger-action-modal
      :visible="actionVisible"
      :title="actionTitle"
      :object-text="actionObjectText"
      :impact="actionImpact"
      :images="actionImages"
      :submit="performAction"
      @close="closeAction"
      @success="onActionSuccess"
    />
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import DangerActionModal from '../../components/danger-action-modal.vue'
import { listCategories } from '../../api/categories'
import { listWanted, getWantedDetail, updateWantedStatus, deleteWanted } from '../../api/wanted'

export default {
  components: { AdminLayout, DangerActionModal },
  data() {
    return {
      query: { keyword: '', categoryId: '', status: '', page: 1, size: 10 },
      categories: [],
      records: [],
      total: 0,
      pages: 1,
      loading: false,
      detailVisible: false,
      detail: {},
      actionVisible: false,
      current: {},
      nextStatus: '',
      brokenWantedImages: {}
    }
  },
  onShow() {
    this.bootstrap()
  },
  computed: {
    detailImages() {
      return this.detail.images || []
    },
    actionImages() {
      return this.detail && this.detail.id === this.current.id && this.detailImages.length
        ? this.detailImages
        : []
    },
    actionTitle() {
      if (this.nextStatus === 'delete') return '删除求购'
      return this.nextStatus === 'closed' ? '关闭求购' : '恢复求购'
    },
    actionObjectText() {
      if (!this.current.id) return ''
      return `#${this.current.id} ${this.current.title || ''}`
    },
    actionImpact() {
      if (this.nextStatus === 'delete') return '删除后求购信息将从后台可见列表中移除，请确认该内容确实违规。'
      if (this.nextStatus === 'closed') return '关闭后求购将不再对用户展示，请填写明确原因。'
      return '恢复后求购会重新进入有效状态。'
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
      } finally {
        this.loading = false
      }
    },
    search() {
      this.query.page = 1
      this.load()
    },
    reset() {
      this.query = { keyword: '', categoryId: '', status: '', page: 1, size: 10 }
      this.load()
    },
    setFilter(key, value) {
      this.query[key] = value
      this.search()
    },
    goPage(page) {
      if (page >= 1 && page <= this.pages) {
        this.query.page = page
        this.load()
      }
    },
    async openDetail(row) {
      this.detail = await getWantedDetail(row.id)
      this.detailVisible = true
    },
    closeDetail() {
      this.detailVisible = false
      this.detail = {}
    },
    async openAction(row, status) {
      this.current = row
      this.nextStatus = status
      this.actionVisible = true
      try {
        this.detail = await getWantedDetail(row.id)
      } catch (e) {
        this.detail = {}
      }
    },
    closeAction() {
      this.actionVisible = false
      this.current = {}
      this.nextStatus = ''
    },
    async performAction(reason) {
      if (this.nextStatus === 'delete') await deleteWanted(this.current.id, { reason })
      else await updateWantedStatus(this.current.id, { status: this.nextStatus, reason })
    },
    async onActionSuccess() {
      await this.load()
    },
    previewImages(images, index) {
      const urls = (images || []).filter(Boolean)
      if (!urls.length) return
      uni.previewImage({ urls, current: urls[index] || urls[0] })
    },
    wantedCover(wanted) {
      const images = Array.isArray(wanted.images) ? wanted.images : []
      return this.resolveAssetUrl(images[0] || wanted.imageUrl || '')
    },
    resolveAssetUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      if (url.startsWith('/')) return 'http://127.0.0.1:8080' + url
      return 'http://127.0.0.1:8080/' + url
    },
    markWantedImageBroken(id) {
      this.brokenWantedImages = { ...this.brokenWantedImages, [id]: true }
    },
    categoryName(id) {
      const hit = this.categories.find(c => c.id === id)
      return hit ? hit.name : (id ? `#${id}` : '-')
    },
    wantedStatusText(status) {
      return ({ pending: '待定', active: '有效', closed: '已关闭', sold: '已成交' }[status] || status || '-')
    },
    money(value) {
      return value == null || value === '' ? '-' : `¥${Number(value).toFixed(2)}`
    }
  }
}
</script>

<style scoped>
.toolbar {
  align-items: stretch;
  flex-direction: column;
}

.toolbar-row {
  display: flex;
  align-items: center;
  gap: 10px;
  min-width: 0;
}

.search-row,
.filter-row {
  flex-wrap: wrap;
}

.category-filter {
  max-width: 720px;
  overflow-x: auto;
}

.tr {
  display: grid;
  grid-template-columns: 76px 92px 1fr 130px 180px 110px 300px;
  align-items: center;
  min-height: 76px;
  padding: 6px 14px;
  border-bottom: 1px solid #edf1f5;
  font-size: 14px;
}

.th {
  min-height: 44px;
  padding-top: 0;
  padding-bottom: 0;
  background: #f8fafc;
  font-weight: 700;
  color: #4a5568;
}

.cover-cell {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 70px;
  height: 64px;
  overflow: hidden;
}

.cover-img {
  width: 64px;
  height: 64px;
  display: block;
  border-radius: 6px;
  border: 1px solid #dfe5ec;
  background: #f8fafc;
  object-fit: contain;
  cursor: pointer;
}

.cover-img > div,
.cover-img img,
.cover-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

::v-deep .cover-img > div,
::v-deep .cover-img img,
::v-deep .cover-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

.cover-empty {
  color: #9aa5b1;
  font-size: 12px;
}
</style>

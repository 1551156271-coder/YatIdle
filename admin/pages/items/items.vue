<template>
  <admin-layout title="商品管理" active="/pages/items/items">
    <view class="toolbar">
      <view class="toolbar-row search-row">
        <input v-model="query.keyword" placeholder="商品标题" @confirm="search" />
        <input v-model="query.campus" placeholder="校区" @confirm="search" />
        <button class="primary" @click="search">查询</button>
        <button @click="reset">重置</button>
      </view>
      <view class="toolbar-row filter-row">
        <view class="segmented">
          <text :class="{ active: query.status === '' }" @click="setFilter('status', '')">全部状态</text>
          <text :class="{ active: query.status === 'ON_SALE' }" @click="setFilter('status', 'ON_SALE')">在售</text>
          <text :class="{ active: query.status === 'SOLD' }" @click="setFilter('status', 'SOLD')">已售</text>
          <text :class="{ active: query.status === 'REMOVED' }" @click="setFilter('status', 'REMOVED')">下架</text>
        </view>
        <view class="segmented category-filter">
          <text :class="{ active: query.categoryId === '' }" @click="setFilter('categoryId', '')">全部分类</text>
          <text v-for="c in categories" :key="c.id" :class="{ active: query.categoryId === c.id }" @click="setFilter('categoryId', c.id)">
            {{ c.name }}
          </text>
        </view>
      </view>
    </view>

    <view v-if="loading" class="loading">正在加载商品...</view>
    <view v-else-if="records.length === 0" class="empty">暂无商品数据</view>
    <view v-else class="table">
      <view class="tr th">
        <text>ID</text><text>图片</text><text>标题</text><text>分类</text><text>校区</text><text>价格</text><text>状态</text><text>操作</text>
      </view>
      <view v-for="item in records" :key="item.id" class="tr" :class="{ removed: item.status === 'REMOVED' }">
        <text>{{ item.id }}</text>
        <view class="cover-cell">
          <image v-if="item.imageUrl && !brokenImages[item.id]" :src="item.imageUrl" mode="aspectFit" class="cover-img" @error="markImageBroken(item.id)" @click="previewImages([item.imageUrl], 0)"></image>
          <text v-else-if="item.imageUrl" class="cover-empty">图片加载失败</text>
          <text v-else class="cover-empty">无图</text>
        </view>
        <text>{{ item.title || '-' }}</text>
        <text>{{ categoryName(item.categoryId) }}</text>
        <text>{{ item.campus || '-' }}</text>
        <text>{{ money(item.price) }}</text>
        <text class="status-pill" :class="statusClass(item.status)">{{ itemStatusText(item.status) }}</text>
        <view class="ops">
          <button size="mini" @click="openDetail(item)">详情</button>
          <button v-if="item.status === 'REMOVED'" size="mini" class="restore-action" @click="openAction(item, 'restore')">
            恢复上架
          </button>
          <button v-else size="mini" class="offline-action" @click="openAction(item, 'remove')">
            下架
          </button>
          <button size="mini" class="danger" @click="openAction(item, 'delete')">删除</button>
        </view>
      </view>
    </view>

    <view class="pager">
      <button :disabled="query.page <= 1" @click="goPage(query.page - 1)">上一页</button>
      <text>第 {{ query.page }} / {{ pages }} 页，共 {{ total }} 条</text>
      <button :disabled="query.page >= pages" @click="goPage(query.page + 1)">下一页</button>
    </view>

    <view v-if="detailVisible" class="modal-mask" @click="closeDetail">
      <view class="modal large" @click.stop>
        <view class="modal-header">
          <text class="modal-title">商品详情</text>
          <button @click="closeDetail">关闭</button>
        </view>
        <view class="modal-body">
          <view class="detail-grid">
            <view class="detail-item"><text class="detail-label">ID</text><text class="detail-value">{{ detailItem.id }}</text></view>
            <view class="detail-item"><text class="detail-label">标题</text><text class="detail-value">{{ detailItem.title || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">卖家</text><text class="detail-value">{{ sellerName }}</text></view>
            <view class="detail-item"><text class="detail-label">分类</text><text class="detail-value">{{ categoryName(detailItem.categoryId) }}</text></view>
            <view class="detail-item"><text class="detail-label">校区</text><text class="detail-value">{{ detailItem.campus || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">成色</text><text class="detail-value">{{ detailItem.conditionLevel || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">价格</text><text class="detail-value">{{ money(detailItem.price) }}</text></view>
            <view class="detail-item"><text class="detail-label">状态</text><text class="detail-value">{{ itemStatusText(detailItem.status) }}</text></view>
            <view class="detail-item wide"><text class="detail-label">描述</text><text class="detail-value">{{ detailItem.description || '-' }}</text></view>
            <view class="detail-item wide">
              <text class="detail-label">图片</text>
              <view v-if="detailImages.length" class="image-list detail-image-list">
                <image v-for="(img, index) in detailImages" :key="img" :src="img" mode="aspectFit" class="preview-img detail-preview-img" @click="previewImages(detailImages, index)"></image>
              </view>
              <text v-else class="detail-value muted">暂无图片</text>
            </view>
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
import { listItems, getItemDetail, updateItemStatus, deleteItem } from '../../api/items'

export default {
  components: { AdminLayout, DangerActionModal },
  data() {
    return {
      query: { keyword: '', categoryId: '', status: '', campus: '', page: 1, size: 10 },
      categories: [],
      records: [],
      total: 0,
      pages: 1,
      loading: false,
      detailVisible: false,
      detail: {},
      actionVisible: false,
      actionType: '',
      currentItem: {},
      brokenImages: {},
      imageViewerVisible: false,
      imageViewerImages: [],
      imageViewerIndex: 0
    }
  },
  onShow() {
    this.bootstrap()
  },
  computed: {
    detailItem() {
      return this.detail.item || {}
    },
    detailImages() {
      return this.detail.images || []
    },
    sellerName() {
      const seller = this.detail.seller || {}
      return seller.username ? `${seller.username} (#${seller.id})` : '-'
    },
    actionImages() {
      return this.detailItem && this.detailItem.id === this.currentItem.id && this.detailImages.length
        ? this.detailImages
        : (this.currentItem.imageUrl ? [this.currentItem.imageUrl] : [])
    },
    actionTitle() {
      if (this.actionType === 'delete') return '删除违规商品'
      if (this.actionType === 'restore') return '恢复商品'
      return '下架商品'
    },
    actionObjectText() {
      if (!this.currentItem.id) return ''
      return `#${this.currentItem.id} ${this.currentItem.title || ''}`
    },
    actionImpact() {
      if (this.actionType === 'delete') return '删除后商品将从后台可见列表中移除，请确认该商品确实违规。'
      if (this.actionType === 'restore') return '恢复后商品会重新进入在售状态，请确认图片和描述可正常展示。'
      return '下架后商品将不再对用户展示，请填写明确原因。'
    },
    currentViewerImage() {
      return this.imageViewerImages[this.imageViewerIndex] || ''
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
        const data = await listItems(params)
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
      this.query = { keyword: '', categoryId: '', status: '', campus: '', page: 1, size: 10 }
      this.load()
    },
    setFilter(key, value) {
      this.query[key] = value
      this.search()
    },
    goPage(page) {
      if (page < 1 || page > this.pages) return
      this.query.page = page
      this.load()
    },
    async openDetail(item) {
      this.detail = await getItemDetail(item.id)
      this.detailVisible = true
    },
    closeDetail() {
      this.detailVisible = false
      this.detail = {}
      this.closeImageViewer()
    },
    async openAction(item, type) {
      this.currentItem = item
      this.actionType = type
      this.actionVisible = true
      try {
        this.detail = await getItemDetail(item.id)
      } catch (e) {
        this.detail = {}
      }
    },
    closeAction() {
      this.actionVisible = false
      this.currentItem = {}
      this.actionType = ''
    },
    async performAction(reason) {
      if (this.actionType === 'delete') {
        await deleteItem(this.currentItem.id, { reason })
      } else {
        await updateItemStatus(this.currentItem.id, {
          status: this.actionType === 'restore' ? 'ON_SALE' : 'REMOVED',
          reason
        })
      }
    },
    async onActionSuccess() {
      await this.load()
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
    },
    categoryName(id) {
      const hit = this.categories.find(c => c.id === id)
      return hit ? hit.name : (id ? `#${id}` : '-')
    },
    itemStatusText(status) {
      const map = { ON_SALE: '在售', SOLD: '已售', REMOVED: '已下架' }
      return map[status] || status || '-'
    },
    statusClass(status) {
      return {
        'status-on-sale': status === 'ON_SALE',
        'status-sold': status === 'SOLD',
        'status-removed': status === 'REMOVED'
      }
    },
    money(value) {
      if (value == null || value === '') return '-'
      return `¥${Number(value).toFixed(2)}`
    },
    markImageBroken(id) {
      this.brokenImages = { ...this.brokenImages, [id]: true }
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

.search-row {
  flex-wrap: wrap;
}

.filter-row {
  flex-wrap: wrap;
}

.category-filter {
  max-width: 720px;
  overflow-x: auto;
}

.tr {
  display: grid;
  grid-template-columns: 76px 92px 1fr 130px 130px 110px 110px 260px;
  align-items: center;
  width: 1240px;
  min-height: 76px;
  padding: 6px 14px;
  border-bottom: 1px solid #edf1f5;
  font-size: 14px;
}

.tr.removed {
  position: relative;
  background: #fff7f6;
}

.tr.removed::before {
  content: "";
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: #b42318;
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

.detail-image-list {
  grid-template-columns: repeat(auto-fill, 128px);
  align-items: start;
}

.detail-preview-img {
  width: 128px;
  height: 128px;
}

.detail-preview-img > div,
.detail-preview-img img,
.detail-preview-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

::v-deep .detail-preview-img > div,
::v-deep .detail-preview-img img,
::v-deep .detail-preview-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

.image-viewer-mask {
  position: fixed;
  z-index: 1200;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.76);
}

.image-viewer {
  width: min(920px, calc(100vw - 48px));
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  background: #111827;
  overflow: hidden;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.35);
}

.image-viewer-header {
  height: 48px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #f8fafc;
  font-size: 14px;
}

.image-viewer-header button {
  width: auto;
  min-width: 64px;
  height: 32px;
  color: #1f2937;
  background: #fff;
}

.image-viewer-body {
  position: relative;
  min-height: 320px;
  max-height: calc(100vh - 112px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  overflow: hidden;
  background: #0f172a;
}

.viewer-img {
  width: 100%;
  height: min(72vh, 680px);
  display: block;
  object-fit: contain;
}

.viewer-img > div,
.viewer-img img,
.viewer-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

::v-deep .viewer-img > div,
::v-deep .viewer-img img,
::v-deep .viewer-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

.viewer-nav {
  position: absolute;
  top: 50%;
  z-index: 1;
  width: auto;
  min-width: 72px;
  height: 36px;
  transform: translateY(-50%);
  color: #1f2937;
  background: rgba(255, 255, 255, 0.92);
}

.viewer-prev {
  left: 16px;
}

.viewer-next {
  right: 16px;
}

.status-pill {
  width: fit-content;
  min-width: 56px;
  padding: 4px 8px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 600;
  text-align: center;
}

.status-on-sale {
  color: #166534;
  background: #dcfce7;
}

.status-sold {
  color: #4a5568;
  background: #edf1f5;
}

.status-removed {
  color: #b42318;
  background: #fee4e2;
}

.restore-action {
  border-color: #7cc79b;
  color: #166534;
  background: #f0fdf4;
}

.offline-action {
  border-color: #f3c46b;
  color: #92400e;
  background: #fffbeb;
}
</style>

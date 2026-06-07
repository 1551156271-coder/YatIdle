<template>
  <admin-layout title="举报管理" active="/pages/reports/reports">
    <view class="toolbar">
      <input v-model="query.reason" placeholder="举报原因" @confirm="search" />
      <view class="segmented">
        <text :class="{ active: query.status === '' }" @click="setFilter('status', '')">全部状态</text>
        <text :class="{ active: query.status === 'PENDING' }" @click="setFilter('status', 'PENDING')">待处理</text>
        <text :class="{ active: query.status === 'HANDLED' }" @click="setFilter('status', 'HANDLED')">已处理</text>
        <text :class="{ active: query.status === 'REJECTED' }" @click="setFilter('status', 'REJECTED')">已驳回</text>
      </view>
      <button class="primary" @click="search">查询</button>
      <button @click="reset">重置</button>
    </view>

    <view v-if="loading" class="loading">正在加载举报...</view>
    <view v-else-if="records.length === 0" class="empty">暂无举报数据</view>
    <view v-else class="table">
      <view class="tr th">
        <text>举报</text><text>原因</text><text>举报人</text><text>被举报用户</text><text>关联对象</text><text>处理</text><text>操作</text>
      </view>
      <view v-for="r in records" :key="r.id" class="tr">
        <text>#{{ r.id }}</text>
        <text>{{ reasonText(r.reason) }}</text>
        <text>{{ namedUser(r.reporterUsername, r.reporterId) }}</text>
        <text>{{ namedUser(r.targetUserUsername, r.targetUserId) }}</text>
        <text>{{ reportTargetText(r) }}</text>
        <text>{{ reportStatusText(r.status) }}<text v-if="r.handlerUsername" class="subtext"> · {{ r.handlerUsername }}</text></text>
        <view class="ops">
          <button v-if="canRestore(r)" size="mini" class="restore-action" @click="openRestore(r)">{{ restoreButtonText(r) }}</button>
          <button size="mini" @click="openDetail(r)">详情</button>
          <button v-if="r.status === 'PENDING'" size="mini" @click="openHandle(r, 'HANDLED')">处理</button>
          <button v-if="r.status === 'PENDING'" size="mini" @click="openHandle(r, 'REJECTED')">驳回</button>
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
          <text class="modal-title">举报详情</text>
          <button @click="closeDetail">关闭</button>
        </view>
        <view class="modal-body">
          <view class="detail-grid">
            <view class="detail-item"><text class="detail-label">举报ID</text><text class="detail-value">{{ report.id }}</text></view>
            <view class="detail-item"><text class="detail-label">状态</text><text class="detail-value">{{ reportStatusText(report.status) }}</text></view>
            <view class="detail-item"><text class="detail-label">举报原因</text><text class="detail-value">{{ reasonText(report.reason) }}</text></view>
            <view class="detail-item"><text class="detail-label">举报人</text><text class="detail-value">{{ namedUser(report.reporterUsername, report.reporterId) }}</text></view>
            <view class="detail-item"><text class="detail-label">被举报用户</text><text class="detail-value">{{ namedUser(report.targetUserUsername, report.targetUserId) }}</text></view>
            <view class="detail-item"><text class="detail-label">关联商品</text><text class="detail-value">{{ namedEntity(report.itemTitle, report.itemId, '商品') }}</text></view>
            <view class="detail-item"><text class="detail-label">关联求购</text><text class="detail-value">{{ namedEntity(report.wantedTitle, report.wantedId, '求购') }}</text></view>
            <view class="detail-item"><text class="detail-label">关联订单</text><text class="detail-value">{{ namedEntity(report.orderNo, report.orderId, '订单') }}</text></view>
            <view class="detail-item"><text class="detail-label">聊天会话</text><text class="detail-value">{{ report.chatSessionId || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">处理人</text><text class="detail-value">{{ namedUser(report.handlerUsername, report.handlerId) }}</text></view>
            <view class="detail-item"><text class="detail-label">处理时间</text><text class="detail-value">{{ report.handleTime || '-' }}</text></view>
            <view class="detail-item wide"><text class="detail-label">处理结果</text><text class="detail-value">{{ report.handleResult || '尚未处理' }}</text></view>
            <view class="detail-item wide"><text class="detail-label">详细描述</text><text class="detail-value">{{ report.description || '-' }}</text></view>
            <view class="detail-item wide">
              <text class="detail-label">截图</text>
              <view v-if="reportImages.length" class="image-list">
                <view v-for="(img, index) in reportImages" :key="img + index" class="report-image-tile" @click="canPreviewImage(img) && previewImages(reportImages, index)">
                  <image v-if="canPreviewImage(img)" :src="img" mode="aspectFit" class="preview-img" @error="markReportImageBroken(img)"></image>
                  <text v-else class="image-unavailable">{{ isTemporaryImage(img) ? '临时图片不可预览' : '图片加载失败' }}</text>
                </view>
              </view>
              <text v-else class="detail-value muted">暂无截图</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <danger-action-modal
      :visible="handleVisible"
      :title="handleTitle"
      :object-text="handleObjectText"
      :impact="handleImpact"
      :submit="performHandle"
      reason-placeholder="请输入处理结果，结果会写入操作日志"
      submit-text="确认提交"
      @close="closeHandle"
      @success="onHandleSuccess"
    >
      <template v-slot:extra>
        <view v-if="handleStatus === 'HANDLED'" class="action-options">
          <text class="detail-label">联动操作</text>
          <view class="segmented">
            <text :class="{ active: actionType === '' }" @click="actionType = ''">仅处理</text>
            <text :class="{ active: actionType === 'BAN_USER' }" @click="actionType = 'BAN_USER'">封禁用户</text>
            <text :class="{ active: actionType === 'OFFLINE_ITEM' }" @click="actionType = 'OFFLINE_ITEM'">下架商品</text>
          </view>
        </view>
      </template>
    </danger-action-modal>

    <danger-action-modal
      :visible="restoreVisible"
      :title="restoreTitle"
      :object-text="restoreObjectText"
      :impact="restoreImpact"
      :submit="performRestore"
      reason-placeholder="请输入恢复原因，原因会写入操作日志"
      submit-text="确认恢复"
      @close="closeRestore"
      @success="onRestoreSuccess"
    />

    <view v-if="imageViewerVisible" class="image-viewer-mask" @click="closeImageViewer">
      <view class="image-viewer" @click.stop>
        <view class="image-viewer-header">
          <text>{{ imageViewerIndex + 1 }} / {{ imageViewerImages.length }}</text>
          <button @click="closeImageViewer">关闭</button>
        </view>
        <view class="image-viewer-body">
          <button v-if="imageViewerImages.length > 1" class="viewer-nav viewer-prev" @click="prevImage">上一张</button>
          <image v-if="currentViewerImage && !imageViewerError" :src="currentViewerImage" mode="aspectFit" class="viewer-img" @error="imageViewerError = true"></image>
          <text v-else class="viewer-empty">图片无法加载</text>
          <button v-if="imageViewerImages.length > 1" class="viewer-nav viewer-next" @click="nextImage">下一张</button>
        </view>
      </view>
    </view>
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import DangerActionModal from '../../components/danger-action-modal.vue'
import { listReports, getReportDetail, handleReport, restoreReportAction } from '../../api/reports'

export default {
  components: { AdminLayout, DangerActionModal },
  data() {
    return {
      query: { status: '', reason: '', page: 1, size: 10 },
      records: [],
      total: 0,
      pages: 1,
      loading: false,
      detailVisible: false,
      detail: {},
      handleVisible: false,
      restoreVisible: false,
      currentReport: {},
      restoreReport: {},
      handleStatus: 'HANDLED',
      actionType: '',
      imageViewerVisible: false,
      imageViewerImages: [],
      imageViewerIndex: 0,
      imageViewerError: false,
      brokenReportImages: {}
    }
  },
  onShow() {
    this.load()
  },
  computed: {
    report() {
      return this.detail || {}
    },
    reportImages() {
      const raw = this.report.imageUrls
      if (!raw) return []
      if (Array.isArray(raw)) return raw.map(this.resolveAssetUrl)
      try {
        return (JSON.parse(raw) || []).map(this.resolveAssetUrl)
      } catch (e) {
        return String(raw).split(',').map(x => x.trim()).filter(Boolean).map(this.resolveAssetUrl)
      }
    },
    handleTitle() {
      return this.handleStatus === 'HANDLED' ? '处理举报' : '驳回举报'
    },
    handleObjectText() {
      if (!this.currentReport.id) return ''
      return `#${this.currentReport.id} ${this.reasonText(this.currentReport.reason)} · ${this.reportTargetText(this.currentReport)}`
    },
    handleImpact() {
      if (this.handleStatus === 'REJECTED') return '驳回后举报会记录处理结果，请说明驳回依据。'
      if (this.actionType === 'BAN_USER') return '处理举报时会联动封禁被举报用户，请确认处罚对象正确。'
      if (this.actionType === 'OFFLINE_ITEM') return '处理举报时会联动下架关联商品，请确认商品确实违规。'
      return '处理结果会写入操作日志，请填写可追溯说明。'
    },
    restoreTitle() {
      return this.restoreReport.actionType === 'BAN_USER' ? '恢复账号与内容' : '恢复商品'
    },
    restoreObjectText() {
      if (!this.restoreReport.id) return ''
      return `#${this.restoreReport.id} ${this.reportTargetText(this.restoreReport)}`
    },
    restoreImpact() {
      if (this.restoreReport.actionType === 'BAN_USER') return '将解封被举报用户，并恢复该用户可恢复的下架商品和关闭求购。'
      return '将恢复举报关联商品为在售状态。'
    },
    currentViewerImage() {
      return this.imageViewerImages[this.imageViewerIndex] || ''
    }
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const data = await listReports({ ...this.query })
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
      this.query = { status: '', reason: '', page: 1, size: 10 }
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
    async openDetail(report) {
      this.detail = await getReportDetail(report.id)
      this.detailVisible = true
    },
    closeDetail() {
      this.detailVisible = false
      this.detail = {}
    },
    openHandle(report, status) {
      this.currentReport = report
      this.handleStatus = status
      this.actionType = ''
      this.handleVisible = true
    },
    closeHandle() {
      this.handleVisible = false
      this.currentReport = {}
      this.actionType = ''
    },
    async performHandle(result) {
      await handleReport(this.currentReport.id, {
        status: this.handleStatus,
        result,
        actionType: this.handleStatus === 'HANDLED' ? this.actionType : ''
      })
    },
    async onHandleSuccess() {
      await this.load()
    },
    canRestore(report) {
      return report && report.status === 'HANDLED' && ['BAN_USER', 'OFFLINE_ITEM'].includes(report.actionType)
    },
    restoreButtonText(report) {
      return report.actionType === 'BAN_USER' ? '恢复账号与内容' : '恢复商品'
    },
    openRestore(report) {
      this.restoreReport = report || {}
      this.restoreVisible = true
    },
    closeRestore() {
      this.restoreVisible = false
      this.restoreReport = {}
    },
    async performRestore(result) {
      await restoreReportAction(this.restoreReport.id, { result })
    },
    async onRestoreSuccess() {
      await this.load()
      if (this.detailVisible && this.report.id) {
        this.detail = await getReportDetail(this.report.id)
      }
    },
    resolveAssetUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      if (url.startsWith('/')) return 'http://127.0.0.1:8080' + url
      return 'http://127.0.0.1:8080/' + url
    },
    previewImages(images, index) {
      const source = images || []
      const urls = source.filter(this.canPreviewImage)
      if (!urls.length) return
      this.imageViewerImages = urls
      const current = source[index]
      const safeIndex = Math.max(urls.indexOf(current), 0)
      this.imageViewerIndex = Math.min(safeIndex, urls.length - 1)
      this.imageViewerError = false
      this.imageViewerVisible = true
    },
    closeImageViewer() {
      this.imageViewerVisible = false
      this.imageViewerImages = []
      this.imageViewerIndex = 0
      this.imageViewerError = false
    },
    prevImage() {
      if (!this.imageViewerImages.length) return
      this.imageViewerIndex = (this.imageViewerIndex + this.imageViewerImages.length - 1) % this.imageViewerImages.length
      this.imageViewerError = false
    },
    nextImage() {
      if (!this.imageViewerImages.length) return
      this.imageViewerIndex = (this.imageViewerIndex + 1) % this.imageViewerImages.length
      this.imageViewerError = false
    },
    isTemporaryImage(url) {
      return /^https?:\/\/tmp\//i.test(url || '') || /^wxfile:\/\//i.test(url || '')
    },
    isReportImageBroken(url) {
      return !!this.brokenReportImages[url]
    },
    canPreviewImage(url) {
      return !!url && !this.isTemporaryImage(url) && !this.isReportImageBroken(url)
    },
    markReportImageBroken(url) {
      this.brokenReportImages = { ...this.brokenReportImages, [url]: true }
    },
    reasonText(reason) {
      const map = { fake: '虚假商品', counterfeit: '假冒商品', harass: '骚扰辱骂', fraud: '欺诈行为', other: '其他违规' }
      return map[reason] || reason || '-'
    },
    reportStatusText(status) {
      const map = { PENDING: '待处理', HANDLED: '已处理', REJECTED: '已驳回' }
      return map[status] || status || '-'
    },
    reportTargetText(report) {
      if (report.itemTitle || report.itemId) return this.namedEntity(report.itemTitle, report.itemId, '商品')
      if (report.wantedTitle || report.wantedId) return this.namedEntity(report.wantedTitle, report.wantedId, '求购')
      if (report.orderNo || report.orderId) return this.namedEntity(report.orderNo, report.orderId, '订单')
      if (report.chatSessionId) return `聊天 #${report.chatSessionId}`
      return '-'
    },
    namedUser(name, id) {
      if (name && id) return `${name} (#${id})`
      if (name) return name
      return id ? `用户 #${id}` : '-'
    },
    namedEntity(name, id, fallback) {
      if (name && id) return `${name} (#${id})`
      if (name) return name
      return id ? `${fallback} #${id}` : '-'
    }
  }
}
</script>

<style scoped>
.tr {
  display: grid;
  grid-template-columns: 90px 130px 180px 180px 260px 160px 240px;
  align-items: center;
  width: 1240px;
  min-height: 50px;
  padding: 0 14px;
  border-bottom: 1px solid #edf1f5;
  font-size: 14px;
}

.th {
  background: #f8fafc;
  font-weight: 700;
  color: #4a5568;
}

.action-options {
  margin: 14px 0;
}

.subtext {
  color: #718096;
}

.restore-action {
  color: #0f7a45;
  background: #e8f5ee;
}

.image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, 96px);
  gap: 10px;
  width: 100%;
}

.preview-img {
  width: 96px;
  height: 96px;
  display: block;
  border: 1px solid #dfe5ec;
  border-radius: 6px;
  background: #f8fafc;
  object-fit: contain;
  cursor: zoom-in;
}

.preview-img > div,
.preview-img img,
.preview-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

::v-deep .preview-img > div,
::v-deep .preview-img img,
::v-deep .preview-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

.report-image-tile {
  width: 96px;
  height: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
}

.image-unavailable {
  width: 96px;
  height: 96px;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 8px;
  border: 1px dashed #cbd5e1;
  border-radius: 6px;
  color: #718096;
  background: #f8fafc;
  font-size: 12px;
  text-align: center;
}

.image-viewer-mask {
  position: fixed;
  z-index: 1400;
  inset: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.76);
}

.image-viewer {
  width: min(880px, calc(100vw - 48px));
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  background: #111827;
  overflow: hidden;
}

.image-viewer-header {
  height: 48px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #f8fafc;
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
  min-height: 300px;
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
  height: min(72vh, 640px);
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

.viewer-empty {
  color: #cbd5e1;
  font-size: 14px;
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
</style>

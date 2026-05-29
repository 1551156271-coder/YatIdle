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
        <text>ID</text><text>原因</text><text>举报人</text><text>被举报用户</text><text>商品</text><text>状态</text><text>操作</text>
      </view>
      <view v-for="r in records" :key="r.id" class="tr">
        <text>{{ r.id }}</text>
        <text>{{ reasonText(r.reason) }}</text>
        <text>{{ r.reporterId || '-' }}</text>
        <text>{{ r.targetUserId || '-' }}</text>
        <text>{{ r.itemId || '-' }}</text>
        <text>{{ reportStatusText(r.status) }}</text>
        <view class="ops">
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
            <view class="detail-item"><text class="detail-label">处理结果</text><text class="detail-value">{{ report.handleResult || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">举报人</text><text class="detail-value">{{ userName(detail.reporter) }}</text></view>
            <view class="detail-item"><text class="detail-label">被举报用户</text><text class="detail-value">{{ userName(detail.targetUser) }}</text></view>
            <view class="detail-item"><text class="detail-label">关联商品</text><text class="detail-value">{{ itemName(detail.item) }}</text></view>
            <view class="detail-item"><text class="detail-label">关联求购</text><text class="detail-value">{{ wantedName(detail.wanted) }}</text></view>
            <view class="detail-item"><text class="detail-label">关联订单</text><text class="detail-value">{{ orderName(detail.order) }}</text></view>
            <view class="detail-item"><text class="detail-label">聊天会话</text><text class="detail-value">{{ report.chatSessionId || '-' }}</text></view>
            <view class="detail-item wide"><text class="detail-label">详细描述</text><text class="detail-value">{{ report.description || '-' }}</text></view>
            <view class="detail-item wide">
              <text class="detail-label">截图</text>
              <view v-if="reportImages.length" class="image-list">
                <image v-for="img in reportImages" :key="img" :src="img" mode="aspectFill" class="preview-img"></image>
              </view>
              <text v-else class="detail-value muted">暂无截图</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="handleVisible" class="modal-mask" @click="closeHandle">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ handleStatus === 'HANDLED' ? '处理举报' : '驳回举报' }}</text>
          <button @click="closeHandle">关闭</button>
        </view>
        <view class="modal-body">
          <view class="detail-item wide">
            <text class="detail-label">举报对象</text>
            <text class="detail-value">#{{ currentReport.id }} {{ reasonText(currentReport.reason) }}</text>
          </view>
          <view v-if="handleStatus === 'HANDLED'" class="action-options">
            <text class="detail-label">联动操作</text>
            <view class="segmented">
              <text :class="{ active: actionType === '' }" @click="actionType = ''">仅处理</text>
              <text :class="{ active: actionType === 'BAN_USER' }" @click="actionType = 'BAN_USER'">封禁用户</text>
              <text :class="{ active: actionType === 'OFFLINE_ITEM' }" @click="actionType = 'OFFLINE_ITEM'">下架商品</text>
            </view>
          </view>
          <textarea v-model="handleResult" placeholder="请输入处理结果，结果会写入操作日志" />
        </view>
        <view class="modal-footer">
          <button @click="closeHandle">取消</button>
          <button class="danger" :disabled="submitting" @click="submitHandle">确认提交</button>
        </view>
      </view>
    </view>
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import { listReports, getReportDetail, handleReport } from '../../api/reports'

export default {
  components: { AdminLayout },
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
      currentReport: {},
      handleStatus: 'HANDLED',
      actionType: '',
      handleResult: '',
      submitting: false
    }
  },
  onShow() {
    this.load()
  },
  computed: {
    report() {
      return this.detail.report || {}
    },
    reportImages() {
      const raw = this.report.imageUrls
      if (!raw) return []
      if (Array.isArray(raw)) return raw
      try {
        return JSON.parse(raw) || []
      } catch (e) {
        return String(raw).split(',').map(x => x.trim()).filter(Boolean)
      }
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
      this.handleResult = ''
      this.handleVisible = true
    },
    closeHandle() {
      this.handleVisible = false
      this.currentReport = {}
      this.handleResult = ''
      this.actionType = ''
    },
    async submitHandle() {
      if (!this.handleResult.trim()) {
        uni.showToast({ title: '请填写处理结果', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const ok = await this.confirmTwice(this.handleStatus === 'HANDLED' ? '处理该举报' : '驳回该举报')
        if (!ok) return
        await handleReport(this.currentReport.id, {
          status: this.handleStatus,
          result: this.handleResult,
          actionType: this.handleStatus === 'HANDLED' ? this.actionType : ''
        })
        this.closeHandle()
        await this.load()
      } finally {
        this.submitting = false
      }
    },
    confirmTwice(content) {
      return new Promise(resolve => {
        uni.showModal({
          title: '二次确认',
          content: `确认${content}？`,
          confirmColor: '#b42318',
          success: res => resolve(res.confirm),
          fail: () => resolve(false)
        })
      })
    },
    reasonText(reason) {
      const map = { fake: '虚假商品', counterfeit: '假冒商品', harass: '骚扰辱骂', fraud: '欺诈行为', other: '其他违规' }
      return map[reason] || reason || '-'
    },
    reportStatusText(status) {
      const map = { PENDING: '待处理', HANDLED: '已处理', REJECTED: '已驳回' }
      return map[status] || status || '-'
    },
    userName(user) {
      return user && user.id ? `${user.username || user.nickname || '用户'} (#${user.id})` : '-'
    },
    itemName(item) {
      return item && item.id ? `${item.title || '商品'} (#${item.id})` : '-'
    },
    wantedName(wanted) {
      return wanted && wanted.id ? `${wanted.title || '求购'} (#${wanted.id})` : '-'
    },
    orderName(order) {
      return order && order.id ? `${order.orderNo || '订单'} (#${order.id})` : '-'
    }
  }
}
</script>

<style scoped>
.tr {
  display: grid;
  grid-template-columns: 76px 150px 110px 130px 110px 110px 240px;
  align-items: center;
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
</style>

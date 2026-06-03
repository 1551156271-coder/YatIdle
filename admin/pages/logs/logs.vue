<template>
  <admin-layout title="操作日志" active="/pages/logs/logs">
    <view class="toolbar">
      <input v-model.number="query.adminId" placeholder="管理员ID" @confirm="search" />
      <view class="segmented">
        <text :class="{ active: query.targetType === '' }" @click="setTarget('')">全部对象</text>
        <text v-for="t in targetTypes" :key="t.value" :class="{ active: query.targetType === t.value }" @click="setTarget(t.value)">{{ t.label }}</text>
      </view>
      <button class="primary" @click="search">查询</button>
      <button @click="reset">重置</button>
    </view>

    <view v-if="loading" class="loading">正在加载日志...</view>
    <view v-else-if="records.length === 0" class="empty">暂无操作日志</view>
    <view v-else class="table">
      <view class="tr th"><text>ID</text><text>管理员</text><text>操作</text><text>目标</text><text>状态变化</text><text>备注</text><text>时间</text></view>
      <view v-for="l in records" :key="l.id" class="tr" @click="openDetail(l)">
        <text>#{{ l.id }}</text><text>{{ namedUser(l.adminUsername, l.adminId) }}</text><text>{{ l.actionText || actionText(l.action) }}</text><text>{{ targetText(l) }}</text><text>{{ statusChange(l) }}</text><text>{{ l.remark || '-' }}</text><text>{{ l.createTime || '-' }}</text>
      </view>
    </view>

    <view class="pager">
      <button :disabled="query.page <= 1" @click="goPage(query.page - 1)">上一页</button>
      <text>第 {{ query.page }} / {{ pages }} 页，共 {{ total }} 条</text>
      <button :disabled="query.page >= pages" @click="goPage(query.page + 1)">下一页</button>
    </view>

    <view v-if="detailVisible" class="modal-mask" @click="detailVisible = false">
      <view class="modal" @click.stop>
        <view class="modal-header"><text class="modal-title">日志详情</text><button @click="detailVisible = false">关闭</button></view>
        <view class="modal-body"><view class="detail-grid">
          <view class="detail-item"><text class="detail-label">ID</text><text class="detail-value">{{ detail.id }}</text></view>
          <view class="detail-item"><text class="detail-label">管理员</text><text class="detail-value">{{ namedUser(detail.adminUsername, detail.adminId) }}</text></view>
          <view class="detail-item"><text class="detail-label">操作</text><text class="detail-value">{{ detail.actionText || actionText(detail.action) }}</text></view>
          <view class="detail-item"><text class="detail-label">目标</text><text class="detail-value">{{ targetText(detail) }}</text></view>
          <view class="detail-item"><text class="detail-label">操作前</text><text class="detail-value">{{ statusText(detail.beforeStatus, detail) || detail.beforeStatusText || detail.beforeStatus || '-' }}</text></view>
          <view class="detail-item"><text class="detail-label">操作后</text><text class="detail-value">{{ statusText(detail.afterStatus, detail) || detail.afterStatusText || detail.afterStatus || '-' }}</text></view>
          <view class="detail-item wide"><text class="detail-label">备注/原因</text><text class="detail-value">{{ detail.remark || '-' }}</text></view>
          <view class="detail-item wide"><text class="detail-label">时间</text><text class="detail-value">{{ detail.createTime || '-' }}</text></view>
        </view></view>
      </view>
    </view>
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import { listLogs } from '../../api/logs'

export default {
  components: { AdminLayout },
  data() {
    return {
      query: { adminId: '', targetType: '', page: 1, size: 10 },
      targetTypes: [
        { value: 'USER', label: '用户' },
        { value: 'ITEM', label: '商品' },
        { value: 'REPORT', label: '举报' },
        { value: 'WANTED', label: '求购' },
        { value: 'ORDER', label: '订单' },
        { value: 'REVIEW', label: '评价' },
        { value: 'CATEGORY', label: '分类' }
      ],
      records: [],
      total: 0,
      pages: 1,
      loading: false,
      detailVisible: false,
      detail: {}
    }
  },
  onShow() { this.load() },
  methods: {
    async load() {
      this.loading = true
      try {
        const params = { ...this.query }
        if (params.adminId === '') delete params.adminId
        const data = await listLogs(params)
        this.records = data.records || []
        this.total = Number(data.total || 0)
        this.pages = Math.max(1, Number(data.pages || Math.ceil(this.total / this.query.size) || 1))
      } finally { this.loading = false }
    },
    search() { this.query.page = 1; this.load() },
    reset() { this.query = { adminId: '', targetType: '', page: 1, size: 10 }; this.load() },
    setTarget(type) { this.query.targetType = type; this.search() },
    goPage(page) { if (page >= 1 && page <= this.pages) { this.query.page = page; this.load() } },
    openDetail(row) { this.detail = row; this.detailVisible = true },
    actionText(action) {
      return ({
        UPDATE_USER_STATUS: '修改用户状态',
        UPDATE_USER_ROLE: '修改用户角色',
        UPDATE_ITEM_STATUS: '修改商品状态',
        DELETE_ITEM: '删除商品',
        HANDLE_REPORT: '处理举报',
        UPDATE_WANTED_STATUS: '修改求购状态',
        DELETE_WANTED: '删除求购',
        CANCEL_ORDER: '取消订单',
        DELETE_REVIEW: '删除评价',
        CREATE_CATEGORY: '新增分类',
        UPDATE_CATEGORY: '修改分类',
        DELETE_CATEGORY: '删除分类'
      }[action] || action || '-')
    },
    targetText(log) {
      const label = log.targetTypeText || log.targetType || '对象'
      const name = log.targetName && log.targetName !== '-' ? log.targetName : ''
      if (name && log.targetId) return `${label}：${name} (#${log.targetId})`
      if (name) return `${label}：${name}`
      return log.targetId ? `${label} #${log.targetId}` : label
    },
    statusChange(log) {
      const before = this.statusText(log.beforeStatus, log) || log.beforeStatusText || log.beforeStatus || '-'
      const after = this.statusText(log.afterStatus, log) || log.afterStatusText || log.afterStatus || '-'
      return `${before} -> ${after}`
    },
    statusText(status, log) {
      if (status == null || status === '') return ''
      if (log.action === 'UPDATE_USER_ROLE') return ({ 0: '普通用户', 1: '管理员' }[status] || status)
      if (log.action === 'UPDATE_CATEGORY' || log.targetType === 'CATEGORY') return ({ 0: '禁用', 1: '启用', DELETED: '已删除' }[status] || status)
      if (log.action === 'UPDATE_USER_STATUS') return ({ active: '正常', inactive: '封禁' }[status] || status)
      if (log.action === 'UPDATE_ITEM_STATUS' || log.targetType === 'ITEM') return ({ ON_SALE: '在售', REMOVED: '已下架', SOLD: '已售', DELETED: '已删除' }[status] || status)
      if (log.action === 'UPDATE_WANTED_STATUS' || log.targetType === 'WANTED') return ({ pending: '待定', active: '有效', closed: '已关闭', sold: '已成交', DELETED: '已删除' }[status] || status)
      return ''
    },
    namedUser(name, id) {
      if (name && id) return `${name} (#${id})`
      if (name) return name
      return id ? `管理员 #${id}` : '-'
    }
  }
}
</script>

<style scoped>
.tr { display: grid; grid-template-columns: 80px 180px 160px 260px 200px 260px 180px; align-items: center; width: 1320px; min-height: 50px; padding: 0 14px; border-bottom: 1px solid #edf1f5; font-size: 14px; cursor: pointer; }
.th { background: #f8fafc; font-weight: 700; color: #4a5568; cursor: default; }
</style>

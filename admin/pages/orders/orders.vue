<template>
  <admin-layout title="订单管理" active="/pages/orders/orders">
    <view class="toolbar">
      <input v-model.number="query.userId" placeholder="买家/卖家ID" @confirm="search" />
      <input v-model.number="query.itemId" placeholder="商品ID" @confirm="search" />
      <view class="segmented">
        <text :class="{ active: query.status === '' }" @click="setStatus('')">全部状态</text>
        <text :class="{ active: query.status === 'PENDING' }" @click="setStatus('PENDING')">待交易</text>
        <text :class="{ active: query.status === 'COMPLETED' }" @click="setStatus('COMPLETED')">已完成</text>
        <text :class="{ active: query.status === 'CANCELLED' }" @click="setStatus('CANCELLED')">已取消</text>
      </view>
      <button class="primary" @click="search">查询</button>
      <button @click="reset">重置</button>
    </view>

    <view v-if="loading" class="loading">正在加载订单...</view>
    <view v-else-if="records.length === 0" class="empty">暂无订单数据</view>
    <view v-else class="table">
      <view class="tr th"><text>ID</text><text>订单号</text><text>商品</text><text>买家</text><text>卖家</text><text>金额</text><text>状态</text><text>操作</text></view>
      <view v-for="o in records" :key="o.id" class="tr">
        <text>{{ o.id }}</text><text>{{ o.orderNo || '-' }}</text><text>{{ o.itemId || '-' }}</text><text>{{ o.buyerId || '-' }}</text><text>{{ o.sellerId || '-' }}</text>
        <text>{{ money(o.price) }}</text><text>{{ orderStatusText(o.status) }}</text>
        <view class="ops"><button size="mini" @click="openDetail(o)">详情</button><button v-if="o.status !== 'CANCELLED'" size="mini" class="danger" @click="openCancel(o)">取消</button></view>
      </view>
    </view>

    <view class="pager">
      <button :disabled="query.page <= 1" @click="goPage(query.page - 1)">上一页</button>
      <text>第 {{ query.page }} / {{ pages }} 页，共 {{ total }} 条</text>
      <button :disabled="query.page >= pages" @click="goPage(query.page + 1)">下一页</button>
    </view>

    <view v-if="detailVisible" class="modal-mask" @click="closeDetail">
      <view class="modal large" @click.stop>
        <view class="modal-header"><text class="modal-title">订单详情</text><button @click="closeDetail">关闭</button></view>
        <view class="modal-body">
          <view class="detail-grid">
            <view class="detail-item"><text class="detail-label">订单号</text><text class="detail-value">{{ detail.orderNo || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">状态</text><text class="detail-value">{{ orderStatusText(detail.status) }}</text></view>
            <view class="detail-item"><text class="detail-label">商品ID</text><text class="detail-value">{{ detail.itemId || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">金额</text><text class="detail-value">{{ money(detail.price) }}</text></view>
            <view class="detail-item"><text class="detail-label">买家</text><text class="detail-value">{{ detail.buyerId || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">卖家</text><text class="detail-value">{{ detail.sellerId || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">交易地点</text><text class="detail-value">{{ detail.tradeLocation || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">取消原因</text><text class="detail-value">{{ detail.cancelReason || '-' }}</text></view>
            <view class="detail-item wide"><text class="detail-label">备注</text><text class="detail-value">{{ detail.remark || '-' }}</text></view>
            <view class="detail-item wide">
              <text class="detail-label">订单状态日志</text>
              <view v-if="logs.length" class="log-list">
                <view v-for="log in logs" :key="log.id" class="log-line">{{ log.action }}：{{ log.beforeStatus || '-' }} -> {{ log.afterStatus || '-' }}，操作人 {{ log.operatorId }}</view>
              </view>
              <text v-else class="detail-value muted">暂无日志</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="cancelVisible" class="modal-mask" @click="closeCancel">
      <view class="modal" @click.stop>
        <view class="modal-header"><text class="modal-title">取消异常订单</text><button @click="closeCancel">关闭</button></view>
        <view class="modal-body"><view class="detail-item wide"><text class="detail-label">订单</text><text class="detail-value">{{ current.orderNo || current.id }}</text></view><textarea v-model="reason" placeholder="请输入取消原因" /></view>
        <view class="modal-footer"><button @click="closeCancel">取消</button><button class="danger" :disabled="submitting" @click="submitCancel">确认取消</button></view>
      </view>
    </view>
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import { listOrders, getOrderDetail, listOrderLogs, cancelOrder } from '../../api/orders'

export default {
  components: { AdminLayout },
  data() {
    return { query: { status: '', userId: '', itemId: '', page: 1, size: 10 }, records: [], total: 0, pages: 1, loading: false, detailVisible: false, detail: {}, logs: [], cancelVisible: false, current: {}, reason: '', submitting: false }
  },
  onShow() { this.load() },
  methods: {
    async load() {
      this.loading = true
      try {
        const params = { ...this.query }
        if (params.userId === '') delete params.userId
        if (params.itemId === '') delete params.itemId
        const data = await listOrders(params)
        this.records = data.records || []
        this.total = Number(data.total || 0)
        this.pages = Math.max(1, Number(data.pages || Math.ceil(this.total / this.query.size) || 1))
      } finally { this.loading = false }
    },
    search() { this.query.page = 1; this.load() },
    reset() { this.query = { status: '', userId: '', itemId: '', page: 1, size: 10 }; this.load() },
    setStatus(status) { this.query.status = status; this.search() },
    goPage(page) { if (page >= 1 && page <= this.pages) { this.query.page = page; this.load() } },
    async openDetail(row) { this.detail = await getOrderDetail(row.id); this.logs = await listOrderLogs(row.id) || []; this.detailVisible = true },
    closeDetail() { this.detailVisible = false; this.detail = {}; this.logs = [] },
    openCancel(row) { this.current = row; this.reason = ''; this.cancelVisible = true },
    closeCancel() { this.cancelVisible = false; this.current = {}; this.reason = '' },
    async submitCancel() {
      if (!this.reason.trim()) return uni.showToast({ title: '请填写取消原因', icon: 'none' })
      this.submitting = true
      try {
        const ok = await new Promise(resolve => uni.showModal({ title: '二次确认', content: '确认取消该异常订单？', confirmColor: '#b42318', success: r => resolve(r.confirm), fail: () => resolve(false) }))
        if (!ok) return
        await cancelOrder(this.current.id, { reason: this.reason })
        this.closeCancel()
        await this.load()
      } finally { this.submitting = false }
    },
    orderStatusText(status) { return ({ PENDING: '待交易', COMPLETED: '已完成', CANCELLED: '已取消' }[status] || status || '-') },
    money(value) { return value == null || value === '' ? '-' : `¥${Number(value).toFixed(2)}` }
  }
}
</script>

<style scoped>
.tr { display: grid; grid-template-columns: 70px 1fr 90px 90px 90px 110px 110px 170px; align-items: center; min-height: 50px; padding: 0 14px; border-bottom: 1px solid #edf1f5; font-size: 14px; }
.th { background: #f8fafc; font-weight: 700; color: #4a5568; }
.log-line { padding: 6px 0; border-bottom: 1px solid #edf1f5; color: #4a5568; }
</style>

<template>
  <admin-layout title="数据看板" active="/pages/dashboard/dashboard">
    <view v-if="error" class="error-panel">
      <text>{{ error }}</text>
      <button class="primary" @click="load">重新加载</button>
    </view>
    <view v-if="loading" class="loading">正在加载看板数据...</view>
    <view v-else class="cards">
      <view v-for="card in cards" :key="card.key" class="card">
        <text class="card-label">{{ card.label }}</text>
        <view class="card-value">{{ data[card.key] || 0 }}</view>
      </view>
    </view>
    <view class="panel">
      <view class="panel-title">近 7 天发布趋势</view>
      <view class="trend">
        <view v-for="item in data.publishTrend || []" :key="item.date" class="trend-item">{{ item.date }}：{{ item.count }}</view>
        <text v-if="!(data.publishTrend || []).length" class="muted">暂无趋势数据</text>
      </view>
    </view>
    <view class="panel">
      <view class="panel-title">近 7 天订单趋势</view>
      <view class="trend">
        <view v-for="item in data.orderTrend || []" :key="item.date" class="trend-item">{{ item.date }}：{{ item.count }}</view>
        <text v-if="!(data.orderTrend || []).length" class="muted">暂无趋势数据</text>
      </view>
    </view>
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import { overview } from '../../api/dashboard'

export default {
  components: { AdminLayout },
  data() {
    return {
      data: {},
      loading: false,
      error: '',
      cards: [
        { key: 'userTotal', label: '用户总数' },
        { key: 'todayNewUsers', label: '今日新增' },
        { key: 'itemTotal', label: '商品总数' },
        { key: 'onSaleItems', label: '在售商品' },
        { key: 'wantedTotal', label: '求购总数' },
        { key: 'orderTotal', label: '订单总数' },
        { key: 'pendingReports', label: '待处理举报' },
        { key: 'completedOrders', label: '成交订单' }
      ]
    }
  },
  onShow() { this.load() },
  methods: {
    async load() {
      this.loading = true
      this.error = ''
      try {
        this.data = await overview()
      } catch (e) {
        this.data = {}
        this.error = '看板数据加载失败，请确认后端已启动且后台数据表已初始化。'
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.cards { display: grid; grid-template-columns: repeat(4, minmax(0, 1fr)); gap: 16px; }
.card, .panel { display: block; background: #fff; border: 1px solid #dfe5ec; border-radius: 8px; padding: 18px; box-sizing: border-box; }
.card-label { color: #718096; font-size: 13px; }
.card-value { margin-top: 10px; font-size: 28px; font-weight: 700; color: #1f3d2f; }
.panel { margin-top: 18px; }
.panel-title { font-weight: 700; margin-bottom: 12px; }
.trend { display: flex; gap: 14px; flex-wrap: wrap; color: #4a5568; }
.trend-item { padding: 8px 10px; border: 1px solid #edf1f5; border-radius: 6px; background: #f8fafc; }
.error-panel { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 16px; padding: 12px 14px; border: 1px solid #f0b8b2; border-radius: 8px; background: #fff7f6; color: #b42318; }
@media (max-width: 1100px) { .cards { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
</style>

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
    <view class="chart-grid">
      <view class="panel chart-panel">
        <view class="panel-title">近 7 天商品发布趋势</view>
        <view class="bar-chart">
          <view v-for="item in data.publishTrend || []" :key="item.date" class="bar-cell">
            <view class="bar-track"><view class="bar-fill item-bar" :style="{ height: barHeight(item, data.publishTrend) }"></view></view>
            <text class="bar-value">{{ item.count || 0 }}</text>
            <text class="bar-label">{{ shortDate(item.date) }}</text>
          </view>
          <text v-if="!(data.publishTrend || []).length" class="muted">暂无数据</text>
        </view>
      </view>
      <view class="panel chart-panel">
        <view class="panel-title">近 7 天订单趋势</view>
        <view class="bar-chart">
          <view v-for="item in data.orderTrend || []" :key="item.date" class="bar-cell">
            <view class="bar-track"><view class="bar-fill order-bar" :style="{ height: barHeight(item, data.orderTrend) }"></view></view>
            <text class="bar-value">{{ item.count || 0 }}</text>
            <text class="bar-label">{{ shortDate(item.date) }}</text>
          </view>
          <text v-if="!(data.orderTrend || []).length" class="muted">暂无数据</text>
        </view>
      </view>
      <view class="panel chart-panel">
        <view class="panel-title">商品状态占比</view>
        <view class="ratio-list">
          <view v-for="stat in data.itemStatusStats || []" :key="stat.key" class="ratio-row">
            <text class="ratio-label">{{ stat.label }}</text>
            <view class="ratio-track"><view class="ratio-fill" :class="statClass(stat.key)" :style="{ width: statPercent(stat, data.itemStatusStats) }"></view></view>
            <text class="ratio-count">{{ stat.count || 0 }}</text>
          </view>
        </view>
      </view>
      <view class="panel chart-panel">
        <view class="panel-title">在售商品分类分布</view>
        <view class="ratio-list">
          <view v-for="stat in data.onSaleCategoryStats || []" :key="stat.key" class="ratio-row">
            <text class="ratio-label">{{ stat.label }}</text>
            <view class="ratio-track"><view class="ratio-fill" :class="statClass(stat.key)" :style="{ width: statPercent(stat, data.onSaleCategoryStats) }"></view></view>
            <text class="ratio-count">{{ stat.count || 0 }}</text>
          </view>
          <text v-if="!(data.onSaleCategoryStats || []).length" class="muted">暂无在售商品</text>
        </view>
      </view>
      <view class="panel chart-panel">
        <view class="panel-title">举报状态占比</view>
        <view class="ratio-list">
          <view v-for="stat in data.reportStatusStats || []" :key="stat.key" class="ratio-row">
            <text class="ratio-label">{{ stat.label }}</text>
            <view class="ratio-track"><view class="ratio-fill" :class="statClass(stat.key)" :style="{ width: statPercent(stat, data.reportStatusStats) }"></view></view>
            <text class="ratio-count">{{ stat.count || 0 }}</text>
          </view>
        </view>
      </view>
      <view class="panel chart-panel">
        <view class="panel-title">求购状态概览</view>
        <view class="ratio-list">
          <view v-for="stat in data.wantedStatusStats || []" :key="stat.key" class="ratio-row">
            <text class="ratio-label">{{ stat.label }}</text>
            <view class="ratio-track"><view class="ratio-fill" :class="statClass(stat.key)" :style="{ width: statPercent(stat, data.wantedStatusStats) }"></view></view>
            <text class="ratio-count">{{ stat.count || 0 }}</text>
          </view>
        </view>
      </view>
      <view class="panel chart-panel full">
        <view class="panel-title">用户状态概览</view>
        <view class="ratio-list">
          <view v-for="stat in data.userStatusStats || []" :key="stat.key" class="ratio-row">
            <text class="ratio-label">{{ stat.label }}</text>
            <view class="ratio-track"><view class="ratio-fill" :class="statClass(stat.key)" :style="{ width: statPercent(stat, data.userStatusStats) }"></view></view>
            <text class="ratio-count">{{ stat.count || 0 }}</text>
          </view>
        </view>
      </view>
    </view>
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import { overview } from '../../api/dashboard'
import { listCategories } from '../../api/categories'
import { listItems } from '../../api/items'
import { listWanted } from '../../api/wanted'

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
        const data = await overview()
        await this.fillFallbackStats(data)
        this.data = data
      } catch (e) {
        this.data = {}
        this.error = '看板数据加载失败，请确认后端已启动且后台数据表已初始化。'
      } finally {
        this.loading = false
      }
    },
    trendMax(list) {
      return Math.max(1, ...((list || []).map(item => Number(item.count || 0))))
    },
    barHeight(item, list) {
      const percent = Math.max(6, Math.round((Number(item.count || 0) / this.trendMax(list)) * 100))
      return percent + '%'
    },
    shortDate(date) {
      return String(date || '').slice(5)
    },
    statPercent(stat, list) {
      const total = (list || []).reduce((sum, item) => sum + Number(item.count || 0), 0)
      if (!total) return '0%'
      return Math.round((Number(stat.count || 0) / total) * 100) + '%'
    },
    statClass(key) {
      if (['ON_SALE', 'HANDLED', 'active'].includes(key)) return 'tone-success'
      if (['PENDING', 'SOLD', 'admin', 'pending', 'sold'].includes(key)) return 'tone-warning'
      if (['REMOVED', 'REJECTED', 'inactive', 'closed'].includes(key)) return 'tone-danger'
      return 'tone-neutral'
    },
    async fillFallbackStats(data) {
      const needsCategoryStats = Number(data.onSaleItems || 0) > 0 && !(data.onSaleCategoryStats || []).length
      const needsWantedStats = Number(data.wantedTotal || 0) > 0 && !(data.wantedStatusStats || []).length
      if (!needsCategoryStats && !needsWantedStats) return

      const [categoriesResult, itemsResult, wantedResult] = await Promise.all([
        needsCategoryStats ? listCategories().catch(() => []) : Promise.resolve([]),
        needsCategoryStats ? listItems({ status: 'ON_SALE', page: 1, size: 1000 }).catch(() => ({ records: [] })) : Promise.resolve({ records: [] }),
        needsWantedStats ? listWanted({ page: 1, size: 1000 }).catch(() => ({ records: [] })) : Promise.resolve({ records: [] })
      ])

      if (needsCategoryStats) {
        data.onSaleCategoryStats = this.buildCategoryStats(itemsResult.records || [], categoriesResult || [])
      }
      if (needsWantedStats) {
        data.wantedStatusStats = this.buildWantedStatusStats(wantedResult.records || [])
      }
    },
    buildCategoryStats(items, categories) {
      const categoryMap = new Map((categories || []).map(category => [Number(category.id), category.name]))
      const counts = new Map()
      ;(items || []).forEach(item => {
        const key = Number(item.categoryId || 0)
        if (!key) return
        counts.set(key, (counts.get(key) || 0) + 1)
      })
      return Array.from(counts.entries()).map(([key, count]) => ({
        key: String(key),
        label: categoryMap.get(key) || `分类 #${key}`,
        count
      }))
    },
    buildWantedStatusStats(wantedList) {
      const labels = { active: '有效', pending: '待定', closed: '已关闭', sold: '已成交' }
      const order = ['active', 'pending', 'closed', 'sold']
      const counts = new Map(order.map(status => [status, 0]))
      ;(wantedList || []).forEach(wanted => {
        const status = wanted.status || 'unknown'
        counts.set(status, (counts.get(status) || 0) + 1)
      })
      return Array.from(counts.entries())
        .filter(([, count]) => count > 0)
        .map(([key, count]) => ({ key, label: labels[key] || key, count }))
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
.error-panel { display: flex; align-items: center; justify-content: space-between; gap: 12px; margin-bottom: 16px; padding: 12px 14px; border: 1px solid #f0b8b2; border-radius: 8px; background: #fff7f6; color: #b42318; }
.chart-grid { display: grid; grid-template-columns: repeat(2, minmax(0, 1fr)); gap: 16px; margin-top: 18px; }
.chart-panel.full { grid-column: 1 / -1; }
.bar-chart { height: 220px; display: grid; grid-template-columns: repeat(7, minmax(0, 1fr)); align-items: end; gap: 10px; }
.bar-cell { min-width: 0; height: 100%; display: flex; flex-direction: column; align-items: center; justify-content: flex-end; gap: 6px; }
.bar-track { width: 100%; height: 140px; display: flex; align-items: flex-end; justify-content: center; border-radius: 6px; background: #f1f5f9; overflow: hidden; }
.bar-fill { width: 70%; min-height: 6px; border-radius: 6px 6px 0 0; transition: height .2s ease; }
.item-bar { background: #2f7d51; }
.order-bar { background: #3b6ea8; }
.bar-value { font-size: 12px; color: #1f2937; font-weight: 700; }
.bar-label { font-size: 11px; color: #718096; }
.ratio-list { display: grid; gap: 12px; }
.ratio-row { display: grid; grid-template-columns: 88px minmax(0, 1fr) 54px; align-items: center; gap: 10px; }
.ratio-label { color: #4a5568; font-size: 13px; }
.ratio-track { height: 12px; border-radius: 999px; background: #edf1f5; overflow: hidden; }
.ratio-fill { height: 100%; min-width: 2px; border-radius: inherit; }
.ratio-count { text-align: right; color: #1f2937; font-weight: 700; font-size: 13px; }
.tone-success { background: #2f7d51; }
.tone-warning { background: #e6a23c; }
.tone-danger { background: #d84a3a; }
.tone-neutral { background: #718096; }
@media (max-width: 1100px) { .cards { grid-template-columns: repeat(2, minmax(0, 1fr)); } }
@media (max-width: 1100px) { .chart-grid { grid-template-columns: 1fr; } .chart-panel.full { grid-column: auto; } }
</style>

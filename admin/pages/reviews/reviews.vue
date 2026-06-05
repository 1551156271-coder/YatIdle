<template>
  <admin-layout title="评价管理" active="/pages/reviews/reviews">
    <view class="toolbar">
      <input v-model.number="query.reviewerId" placeholder="评价人ID" @confirm="search" />
      <input v-model.number="query.revieweeId" placeholder="被评价人ID" @confirm="search" />
      <view class="segmented">
        <text :class="{ active: query.rating === '' }" @click="setRating('')">全部评分</text>
        <text v-for="n in [1,2,3,4,5]" :key="n" :class="{ active: query.rating === n }" @click="setRating(n)">{{ n }}分</text>
      </view>
      <button class="primary" @click="search">查询</button>
      <button @click="reset">重置</button>
    </view>

    <view v-if="loading" class="loading">正在加载评价...</view>
    <view v-else-if="records.length === 0" class="empty">暂无评价数据</view>
    <view v-else class="table">
      <view class="tr th"><text>ID</text><text>订单</text><text>评价人</text><text>被评价人</text><text>评分</text><text>内容</text><text>操作</text></view>
      <view v-for="r in records" :key="r.id" class="tr">
        <text>{{ r.id }}</text><text>{{ r.orderId }}</text><text>{{ r.reviewerId }}</text><text>{{ r.revieweeId }}</text><text>{{ r.rating }}</text><text>{{ r.content || '-' }}</text>
        <view class="ops"><button size="mini" @click="openDetail(r)">详情</button><button size="mini" class="danger" @click="openDelete(r)">删除</button></view>
      </view>
    </view>

    <view class="pager">
      <button :disabled="query.page <= 1" @click="goPage(query.page - 1)">上一页</button>
      <text>第 {{ query.page }} / {{ pages }} 页，共 {{ total }} 条</text>
      <button :disabled="query.page >= pages" @click="goPage(query.page + 1)">下一页</button>
    </view>

    <view v-if="detailVisible" class="modal-mask" @click="closeDetail">
      <view class="modal" @click.stop>
        <view class="modal-header"><text class="modal-title">评价详情</text><button @click="closeDetail">关闭</button></view>
        <view class="modal-body"><view class="detail-grid">
          <view class="detail-item"><text class="detail-label">ID</text><text class="detail-value">{{ detail.id }}</text></view>
          <view class="detail-item"><text class="detail-label">订单ID</text><text class="detail-value">{{ detail.orderId }}</text></view>
          <view class="detail-item"><text class="detail-label">评价人</text><text class="detail-value">{{ detail.reviewerId }}</text></view>
          <view class="detail-item"><text class="detail-label">被评价人</text><text class="detail-value">{{ detail.revieweeId }}</text></view>
          <view class="detail-item"><text class="detail-label">评分</text><text class="detail-value">{{ detail.rating }}</text></view>
          <view class="detail-item wide"><text class="detail-label">内容</text><text class="detail-value">{{ detail.content || '-' }}</text></view>
        </view></view>
      </view>
    </view>

    <danger-action-modal
      :visible="deleteVisible"
      title="删除违规评价"
      :object-text="deleteObjectText"
      impact="删除后该评价将不再展示，请确认内容确实违规。"
      :submit="performDelete"
      submit-text="确认删除"
      @close="closeDelete"
      @success="onDeleteSuccess"
    />
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import DangerActionModal from '../../components/danger-action-modal.vue'
import { listReviews, getReviewDetail, deleteReview } from '../../api/reviews'

export default {
  components: { AdminLayout, DangerActionModal },
  data() {
    return {
      query: { reviewerId: '', revieweeId: '', rating: '', page: 1, size: 10 },
      records: [],
      total: 0,
      pages: 1,
      loading: false,
      detailVisible: false,
      detail: {},
      deleteVisible: false,
      current: {}
    }
  },
  onShow() {
    this.load()
  },
  computed: {
    deleteObjectText() {
      if (!this.current.id) return ''
      return `#${this.current.id} ${this.current.content || ''}`
    }
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const params = { ...this.query }
        if (params.reviewerId === '') delete params.reviewerId
        if (params.revieweeId === '') delete params.revieweeId
        if (params.rating === '') delete params.rating
        const data = await listReviews(params)
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
      this.query = { reviewerId: '', revieweeId: '', rating: '', page: 1, size: 10 }
      this.load()
    },
    setRating(rating) {
      this.query.rating = rating
      this.search()
    },
    goPage(page) {
      if (page >= 1 && page <= this.pages) {
        this.query.page = page
        this.load()
      }
    },
    async openDetail(row) {
      this.detail = await getReviewDetail(row.id)
      this.detailVisible = true
    },
    closeDetail() {
      this.detailVisible = false
      this.detail = {}
    },
    openDelete(row) {
      this.current = row
      this.deleteVisible = true
    },
    closeDelete() {
      this.deleteVisible = false
      this.current = {}
    },
    async performDelete(reason) {
      await deleteReview(this.current.id, { reason })
    },
    async onDeleteSuccess() {
      await this.load()
    }
  }
}
</script>

<style scoped>
.tr {
  display: grid;
  grid-template-columns: 70px 100px 100px 110px 80px 1fr 170px;
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
</style>

<template>
  <admin-layout title="商品管理" active="/pages/items/items">
    <view class="toolbar">
      <input v-model="query.keyword" placeholder="商品标题" @confirm="search" />
      <input v-model="query.campus" placeholder="校区" @confirm="search" />
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
      <button class="primary" @click="search">查询</button>
      <button @click="reset">重置</button>
    </view>

    <view v-if="loading" class="loading">正在加载商品...</view>
    <view v-else-if="records.length === 0" class="empty">暂无商品数据</view>
    <view v-else class="table">
      <view class="tr th">
        <text>ID</text><text>标题</text><text>分类</text><text>校区</text><text>价格</text><text>状态</text><text>操作</text>
      </view>
      <view v-for="item in records" :key="item.id" class="tr">
        <text>{{ item.id }}</text>
        <text>{{ item.title || '-' }}</text>
        <text>{{ categoryName(item.categoryId) }}</text>
        <text>{{ item.campus || '-' }}</text>
        <text>{{ money(item.price) }}</text>
        <text>{{ itemStatusText(item.status) }}</text>
        <view class="ops">
          <button size="mini" @click="openDetail(item)">详情</button>
          <button size="mini" @click="openAction(item, item.status === 'REMOVED' ? 'restore' : 'remove')">
            {{ item.status === 'REMOVED' ? '恢复' : '下架' }}
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
              <view v-if="detailImages.length" class="image-list">
                <image v-for="img in detailImages" :key="img" :src="img" mode="aspectFill" class="preview-img"></image>
              </view>
              <text v-else class="detail-value muted">暂无图片</text>
            </view>
          </view>
        </view>
      </view>
    </view>

    <view v-if="actionVisible" class="modal-mask" @click="closeAction">
      <view class="modal" @click.stop>
        <view class="modal-header">
          <text class="modal-title">{{ actionTitle }}</text>
          <button @click="closeAction">关闭</button>
        </view>
        <view class="modal-body">
          <view class="detail-item wide">
            <text class="detail-label">操作对象</text>
            <text class="detail-value">#{{ currentItem.id }} {{ currentItem.title }}</text>
          </view>
          <textarea v-model="actionReason" placeholder="请输入操作原因，原因会写入操作日志" />
        </view>
        <view class="modal-footer">
          <button @click="closeAction">取消</button>
          <button class="danger" :disabled="submitting" @click="submitAction">确认执行</button>
        </view>
      </view>
    </view>
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import { listCategories } from '../../api/categories'
import { listItems, getItemDetail, updateItemStatus, deleteItem } from '../../api/items'

export default {
  components: { AdminLayout },
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
      actionReason: '',
      submitting: false
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
    actionTitle() {
      if (this.actionType === 'delete') return '删除违规商品'
      if (this.actionType === 'restore') return '恢复商品'
      return '下架商品'
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
    },
    openAction(item, type) {
      this.currentItem = item
      this.actionType = type
      this.actionReason = ''
      this.actionVisible = true
    },
    closeAction() {
      this.actionVisible = false
      this.currentItem = {}
      this.actionType = ''
      this.actionReason = ''
    },
    async submitAction() {
      if (!this.actionReason.trim()) {
        uni.showToast({ title: '请填写操作原因', icon: 'none' })
        return
      }
      this.submitting = true
      try {
        const ok = await this.confirmTwice(this.actionTitle)
        if (!ok) return
        if (this.actionType === 'delete') {
          await deleteItem(this.currentItem.id, { reason: this.actionReason })
        } else {
          await updateItemStatus(this.currentItem.id, {
            status: this.actionType === 'restore' ? 'ON_SALE' : 'REMOVED',
            reason: this.actionReason
          })
        }
        this.closeAction()
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
    categoryName(id) {
      const hit = this.categories.find(c => c.id === id)
      return hit ? hit.name : (id ? `#${id}` : '-')
    },
    itemStatusText(status) {
      const map = { ON_SALE: '在售', SOLD: '已售', REMOVED: '已下架' }
      return map[status] || status || '-'
    },
    money(value) {
      if (value == null || value === '') return '-'
      return `¥${Number(value).toFixed(2)}`
    }
  }
}
</script>

<style scoped>
.category-filter {
  max-width: 520px;
  overflow-x: auto;
}

.tr {
  display: grid;
  grid-template-columns: 76px 1fr 130px 130px 110px 110px 260px;
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

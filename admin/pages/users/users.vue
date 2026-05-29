<template>
  <admin-layout title="用户管理" active="/pages/users/users">
    <view class="toolbar">
      <input v-model="query.keyword" placeholder="用户名 / 昵称" @confirm="search" />
      <view class="segmented">
        <text :class="{ active: query.status === '' }" @click="setFilter('status', '')">全部状态</text>
        <text :class="{ active: query.status === 'active' }" @click="setFilter('status', 'active')">正常</text>
        <text :class="{ active: query.status === 'inactive' }" @click="setFilter('status', 'inactive')">封禁</text>
      </view>
      <view class="segmented">
        <text :class="{ active: query.role === '' }" @click="setFilter('role', '')">全部角色</text>
        <text :class="{ active: query.role === 0 }" @click="setFilter('role', 0)">用户</text>
        <text :class="{ active: query.role === 1 }" @click="setFilter('role', 1)">管理员</text>
      </view>
      <button class="primary" @click="search">查询</button>
      <button @click="reset">重置</button>
    </view>

    <view v-if="loading" class="loading">正在加载用户...</view>
    <view v-else-if="records.length === 0" class="empty">暂无用户数据</view>
    <view v-else class="table">
      <view class="tr th">
        <text>ID</text><text>用户名</text><text>昵称</text><text>角色</text><text>状态</text><text>信用分</text><text>操作</text>
      </view>
      <view v-for="u in records" :key="u.id" class="tr">
        <text>{{ u.id }}</text>
        <text>{{ u.username || '-' }}</text>
        <text>{{ u.nickname || '-' }}</text>
        <text>{{ roleText(u.role) }}</text>
        <text>{{ statusText(u.status) }}</text>
        <text>{{ u.creditScore == null ? '-' : u.creditScore }}</text>
        <view class="ops">
          <button size="mini" @click="openDetail(u)">详情</button>
          <button size="mini" @click="openAction(u, 'status')">{{ u.status === 'active' ? '封禁' : '解封' }}</button>
          <button size="mini" @click="openAction(u, 'role')">{{ u.role === 1 ? '取消管理员' : '设为管理员' }}</button>
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
        <view class="modal-header">
          <text class="modal-title">用户详情</text>
          <button @click="closeDetail">关闭</button>
        </view>
        <view class="modal-body">
          <view class="detail-grid">
            <view class="detail-item"><text class="detail-label">ID</text><text class="detail-value">{{ detail.id }}</text></view>
            <view class="detail-item"><text class="detail-label">用户名</text><text class="detail-value">{{ detail.username || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">昵称</text><text class="detail-value">{{ detail.nickname || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">手机号</text><text class="detail-value">{{ detail.phone || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">校区</text><text class="detail-value">{{ detail.campus || '-' }}</text></view>
            <view class="detail-item"><text class="detail-label">信用分</text><text class="detail-value">{{ detail.creditScore == null ? '-' : detail.creditScore }}</text></view>
            <view class="detail-item"><text class="detail-label">角色</text><text class="detail-value">{{ roleText(detail.role) }}</text></view>
            <view class="detail-item"><text class="detail-label">状态</text><text class="detail-value">{{ statusText(detail.status) }}</text></view>
            <view class="detail-item wide"><text class="detail-label">简介</text><text class="detail-value">{{ detail.bio || '-' }}</text></view>
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
            <text class="detail-value">#{{ currentUser.id }} {{ currentUser.username }}</text>
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
import { listUsers, getUserDetail, updateUserStatus, updateUserRole } from '../../api/users'

export default {
  components: { AdminLayout },
  data() {
    return {
      query: { keyword: '', status: '', role: '', page: 1, size: 10 },
      records: [],
      total: 0,
      pages: 1,
      loading: false,
      detailVisible: false,
      detail: {},
      actionVisible: false,
      actionType: '',
      currentUser: {},
      actionReason: '',
      submitting: false
    }
  },
  onShow() {
    this.load()
  },
  computed: {
    actionTitle() {
      if (!this.currentUser.id) return '确认操作'
      if (this.actionType === 'role') return this.currentUser.role === 1 ? '取消管理员权限' : '设置管理员权限'
      return this.currentUser.status === 'active' ? '封禁用户' : '解封用户'
    }
  },
  methods: {
    async load() {
      this.loading = true
      try {
        const params = { ...this.query }
        if (params.role === '') delete params.role
        const data = await listUsers(params)
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
      this.query = { keyword: '', status: '', role: '', page: 1, size: 10 }
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
    async openDetail(user) {
      this.detail = await getUserDetail(user.id)
      this.detailVisible = true
    },
    closeDetail() {
      this.detailVisible = false
      this.detail = {}
    },
    openAction(user, type) {
      this.currentUser = user
      this.actionType = type
      this.actionReason = ''
      this.actionVisible = true
    },
    closeAction() {
      this.actionVisible = false
      this.currentUser = {}
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
        if (this.actionType === 'role') {
          const role = this.currentUser.role === 1 ? 0 : 1
          await updateUserRole(this.currentUser.id, { role, reason: this.actionReason })
        } else {
          const status = this.currentUser.status === 'active' ? 'inactive' : 'active'
          await updateUserStatus(this.currentUser.id, { status, reason: this.actionReason })
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
    roleText(role) {
      return role === 1 ? '管理员' : '普通用户'
    },
    statusText(status) {
      return status === 'inactive' ? '封禁' : '正常'
    }
  }
}
</script>

<style scoped>
.tr {
  display: grid;
  grid-template-columns: 76px 150px 150px 110px 100px 100px 320px;
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

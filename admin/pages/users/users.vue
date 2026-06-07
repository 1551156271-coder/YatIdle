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
        <text>ID</text><text>头像</text><text>用户名</text><text>昵称</text><text>角色</text><text>状态</text><text>信用分</text><text>操作</text>
      </view>
      <view v-for="u in records" :key="u.id" class="tr" :class="{ banned: u.status === 'inactive' }">
        <text>{{ u.id }}</text>
        <view class="cover-cell">
          <image v-if="userAvatar(u) && !brokenUserAvatars[u.id]" :src="userAvatar(u)" mode="aspectFill" class="avatar-img" @error="markUserAvatarBroken(u.id)"></image>
          <text v-else class="cover-empty">无头像</text>
        </view>
        <text>{{ u.username || '-' }}</text>
        <text>{{ u.nickname || '-' }}</text>
        <text>{{ roleText(u.role) }}</text>
        <text><text class="status-pill" :class="userStatusClass(u.status)">{{ statusText(u.status) }}</text></text>
        <text>{{ u.creditScore == null ? '-' : u.creditScore }}</text>
        <view class="ops">
          <button size="mini" @click="openDetail(u)">详情</button>
          <button v-if="!isSelf(u)" size="mini" @click="openAction(u, 'status')">{{ u.status === 'active' ? '封禁' : '解封' }}</button>
          <button v-if="!isSelf(u)" size="mini" @click="openAction(u, 'role')">{{ u.role === 1 ? '取消管理员' : '设为管理员' }}</button>
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
          <view class="user-detail-head">
            <view class="detail-avatar-wrap">
              <image v-if="detailAvatar && !detailAvatarBroken" :src="detailAvatar" mode="aspectFill" class="detail-avatar-img" @error="detailAvatarBroken = true"></image>
              <text v-else class="detail-avatar-empty">{{ userInitial(detail) }}</text>
            </view>
            <view class="detail-user-main">
              <text class="detail-user-name">{{ detail.nickname || detail.username || '-' }}</text>
              <text class="detail-user-meta">#{{ detail.id || '-' }} · {{ roleText(detail.role) }} · {{ statusText(detail.status) }}</text>
            </view>
          </view>
          <view class="detail-grid">
            <view class="detail-item"><text class="detail-label">余额</text><text class="detail-value">{{ detail.balance == null ? '-' : detail.balance }}</text></view>
            <view class="detail-item"><text class="detail-label">成交数</text><text class="detail-value">{{ detail.dealCount == null ? '-' : detail.dealCount }}</text></view>
            <view class="detail-item"><text class="detail-label">在售数</text><text class="detail-value">{{ detail.goodsCount == null ? '-' : detail.goodsCount }}</text></view>
            <view class="detail-item"><text class="detail-label">评价数</text><text class="detail-value">{{ detail.reviewCount == null ? '-' : detail.reviewCount }}</text></view>
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

    <danger-action-modal
      :visible="actionVisible"
      :title="actionTitle"
      :object-text="actionObjectText"
      :impact="actionImpact"
      :submit="performAction"
      @close="closeAction"
      @success="onActionSuccess"
    />
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import DangerActionModal from '../../components/danger-action-modal.vue'
import { listUsers, getUserDetail, updateUserStatus, updateUserRole } from '../../api/users'

export default {
  components: { AdminLayout, DangerActionModal },
  data() {
    const adminUser = uni.getStorageSync('adminUser') || {}
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
      currentAdminId: adminUser.id,
      brokenUserAvatars: {},
      detailAvatarBroken: false
    }
  },
  onShow() {
    this.load()
  },
  computed: {
    detailAvatar() {
      return this.userAvatar(this.detail || {})
    },
    actionTitle() {
      if (!this.currentUser.id) return '确认操作'
      if (this.actionType === 'role') return this.currentUser.role === 1 ? '取消管理员权限' : '设置管理员权限'
      return this.currentUser.status === 'active' ? '封禁用户' : '解封用户'
    },
    actionObjectText() {
      if (!this.currentUser.id) return ''
      return `#${this.currentUser.id} ${this.currentUser.username || this.currentUser.nickname || ''}`
    },
    actionImpact() {
      if (this.actionType === 'role') {
        return this.currentUser.role === 1 ? '取消后该用户将失去后台管理权限。' : '设置后该用户将获得后台管理权限，请确认身份可信。'
      }
      return this.currentUser.status === 'active' ? '封禁后该用户将无法继续正常使用账号。' : '解封后该用户将恢复正常使用权限。'
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
      this.detailAvatarBroken = false
      this.detail = await getUserDetail(user.id)
      this.detailVisible = true
    },
    closeDetail() {
      this.detailVisible = false
      this.detail = {}
    },
    openAction(user, type) {
      if (this.isSelf(user)) {
        uni.showToast({ title: '不能操作当前登录管理员', icon: 'none' })
        return
      }
      this.currentUser = user
      this.actionType = type
      this.actionVisible = true
    },
    closeAction() {
      this.actionVisible = false
      this.currentUser = {}
      this.actionType = ''
    },
    async performAction(reason) {
      if (this.isSelf(this.currentUser)) {
        throw new Error('不能操作当前登录管理员。')
      }
      if (this.actionType === 'role') {
        const role = this.currentUser.role === 1 ? 0 : 1
        await updateUserRole(this.currentUser.id, { role, reason })
      } else {
        const status = this.currentUser.status === 'active' ? 'inactive' : 'active'
        await updateUserStatus(this.currentUser.id, { status, reason })
      }
    },
    async onActionSuccess() {
      await this.load()
    },
    roleText(role) {
      return role === 1 ? '管理员' : '普通用户'
    },
    statusText(status) {
      return status === 'inactive' ? '封禁' : '正常'
    },
    userStatusClass(status) {
      return status === 'inactive' ? 'status-inactive' : 'status-active'
    },
    userAvatar(user) {
      return this.resolveAssetUrl(user.avatar || '')
    },
    resolveAssetUrl(url) {
      if (!url) return ''
      if (url.startsWith('http://') || url.startsWith('https://')) return url
      if (url.startsWith('/')) return 'http://127.0.0.1:8080' + url
      return 'http://127.0.0.1:8080/' + url
    },
    markUserAvatarBroken(id) {
      this.brokenUserAvatars = { ...this.brokenUserAvatars, [id]: true }
    },
    userInitial(user) {
      const text = (user && (user.nickname || user.username)) || '?'
      return String(text).charAt(0).toUpperCase()
    },
    isSelf(user) {
      return user && this.currentAdminId != null && Number(user.id) === Number(this.currentAdminId)
    }
  }
}
</script>

<style scoped>
.tr {
  display: grid;
  grid-template-columns: 76px 92px 150px 150px 110px 100px 100px 320px;
  align-items: center;
  min-height: 76px;
  padding: 6px 14px;
  border-bottom: 1px solid #edf1f5;
  font-size: 14px;
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

.avatar-img {
  width: 48px;
  height: 48px;
  display: block;
  border-radius: 50%;
  border: 1px solid #dfe5ec;
  background: #f8fafc;
  object-fit: cover;
}

.avatar-img > div,
.avatar-img img,
.avatar-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
  background-size: cover !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

::v-deep .avatar-img > div,
::v-deep .avatar-img img,
::v-deep .avatar-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
  background-size: cover !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

.cover-empty {
  color: #9aa5b1;
  font-size: 12px;
}

.user-detail-head {
  display: flex;
  align-items: center;
  gap: 18px;
  margin-bottom: 18px;
  padding: 16px;
  border: 1px solid #edf1f5;
  border-radius: 8px;
  background: #f8fafc;
}

.detail-avatar-wrap {
  width: 86px;
  height: 86px;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
  overflow: hidden;
  border-radius: 50%;
  border: 1px solid #dfe5ec;
  background: #e8f5ee;
}

.detail-avatar-img {
  width: 100%;
  height: 100%;
  display: block;
  border-radius: 50%;
  object-fit: cover;
}

.detail-avatar-img > div,
.detail-avatar-img img,
.detail-avatar-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
  background-size: cover !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

::v-deep .detail-avatar-img > div,
::v-deep .detail-avatar-img img,
::v-deep .detail-avatar-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: cover !important;
  background-size: cover !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

.detail-avatar-empty {
  color: #2f7d51;
  font-size: 34px;
  font-weight: 700;
}

.detail-user-main {
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.detail-user-name {
  color: #17202a;
  font-size: 20px;
  font-weight: 700;
}

.detail-user-meta {
  color: #718096;
  font-size: 13px;
}

.tr.banned {
  background: #fff7f6;
  border-left: 4px solid #e67c73;
}

.status-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 52px;
  padding: 3px 10px;
  border-radius: 999px;
  font-size: 12px;
  font-weight: 700;
}

.status-active {
  color: #0f7a45;
  background: #e8f5ee;
}

.status-inactive {
  color: #b42318;
  background: #fff0ed;
}

.tr:not(.banned) .ops button:nth-child(2) {
  color: #b42318;
  background: #fff0ed;
}

.tr.banned .ops button:nth-child(2) {
  color: #0f7a45;
  background: #e8f5ee;
}
</style>

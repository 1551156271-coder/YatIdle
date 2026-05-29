<template>
  <view class="admin-shell">
    <view class="sidebar">
      <view class="brand">YatIdle Admin</view>
      <view
        v-for="item in menus"
        :key="item.path"
        :class="['menu-item', active === item.path ? 'active' : '']"
        @click="go(item.path)"
      >
        <text class="menu-icon">{{ item.icon }}</text>
        <text>{{ item.label }}</text>
      </view>
    </view>
    <view class="main">
      <view class="topbar">
        <view>
          <view class="title">{{ title }}</view>
          <view class="subtitle">校园二手交易平台运营后台</view>
        </view>
        <view class="account">
          <text>{{ adminUser.username || '管理员' }}</text>
          <button size="mini" @click="logout">退出</button>
        </view>
      </view>
      <view class="content"><slot /></view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'AdminLayout',
  props: {
    title: { type: String, default: '后台管理' },
    active: { type: String, default: '/pages/dashboard/dashboard' }
  },
  data() {
    return {
      adminUser: uni.getStorageSync('adminUser') || {},
      menus: [
        { label: '数据看板', path: '/pages/dashboard/dashboard', icon: 'D' },
        { label: '用户管理', path: '/pages/users/users', icon: 'U' },
        { label: '商品管理', path: '/pages/items/items', icon: 'I' },
        { label: '举报管理', path: '/pages/reports/reports', icon: 'R' },
        { label: '求购管理', path: '/pages/wanted/wanted', icon: 'W' },
        { label: '订单管理', path: '/pages/orders/orders', icon: 'O' },
        { label: '评价管理', path: '/pages/reviews/reviews', icon: 'V' },
        { label: '聊天审计', path: '/pages/chats/chats', icon: 'C' },
        { label: '分类管理', path: '/pages/categories/categories', icon: 'G' },
        { label: '操作日志', path: '/pages/logs/logs', icon: 'L' }
      ]
    }
  },
  created() {
    if (!uni.getStorageSync('adminToken')) {
      uni.reLaunch({ url: '/pages/login/login' })
    }
  },
  methods: {
    go(path) {
      if (path !== this.active) uni.redirectTo({ url: path })
    },
    logout() {
      uni.removeStorageSync('adminToken')
      uni.removeStorageSync('adminUser')
      uni.reLaunch({ url: '/pages/login/login' })
    }
  }
}
</script>

<style scoped>
.admin-shell { width: 100vw; min-height: 100vh; display: flex; background: #f4f6f8; overflow: hidden; }
.sidebar { flex: 0 0 224px; width: 224px; min-height: 100vh; background: #1f3d2f; color: #dce8df; padding: 18px 12px; box-sizing: border-box; }
.brand { height: 52px; line-height: 52px; padding: 0 12px; font-size: 18px; font-weight: 700; color: #fff; }
.menu-item { height: 42px; display: flex; align-items: center; gap: 10px; padding: 0 12px; border-radius: 6px; cursor: pointer; font-size: 14px; }
.menu-item.active, .menu-item:hover { background: #2f6a4b; color: #fff; }
.menu-icon { width: 22px; height: 22px; line-height: 22px; text-align: center; border: 1px solid rgba(255,255,255,.25); border-radius: 5px; font-size: 12px; }
.main { flex: 1; min-width: 0; max-width: calc(100vw - 224px); }
.topbar { height: 72px; background: #fff; border-bottom: 1px solid #dfe5ec; display: flex; align-items: center; justify-content: space-between; padding: 0 24px; box-sizing: border-box; }
.title { font-size: 20px; font-weight: 700; color: #17202a; }
.subtitle { margin-top: 4px; color: #718096; font-size: 13px; }
.account { display: flex; align-items: center; gap: 12px; color: #4a5568; }
.account uni-button,
.account button { flex: 0 0 auto; }
.content { height: calc(100vh - 72px); padding: 24px; box-sizing: border-box; overflow: auto; }
</style>

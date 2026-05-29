<template>
  <view class="login-page">
    <view class="login-panel">
      <view class="title">YatIdle 后台管理</view>
      <view class="field"><text>账号</text><input v-model="form.username" placeholder="admin" /></view>
      <view class="field"><text>密码</text><input v-model="form.password" password placeholder="123456" /></view>
      <button class="login-btn" :loading="loading" @click="submit">登录</button>
    </view>
  </view>
</template>

<script>
import { login } from '../../api/auth'

export default {
  data() {
    return { form: { username: '', password: '' }, loading: false }
  },
  methods: {
    async submit() {
      if (!this.form.username || !this.form.password) {
        uni.showToast({ title: '请输入账号和密码', icon: 'none' })
        return
      }
      this.loading = true
      try {
        const data = await login(this.form)
        uni.setStorageSync('adminToken', data.token)
        uni.setStorageSync('adminUser', data.user)
        uni.reLaunch({ url: '/pages/dashboard/dashboard' })
      } finally {
        this.loading = false
      }
    }
  }
}
</script>

<style scoped>
.login-page { min-height: 100vh; display: flex; align-items: center; justify-content: center; background: #eef3f0; }
.login-panel { width: 380px; background: #fff; border: 1px solid #dfe5ec; border-radius: 8px; padding: 32px; box-sizing: border-box; }
.title { font-size: 24px; font-weight: 700; margin-bottom: 28px; color: #1f3d2f; }
.field { margin-bottom: 18px; }
.field text { display: block; margin-bottom: 8px; color: #4a5568; font-size: 14px; }
.field uni-input { height: 42px; border: 1px solid #cfd8e3; border-radius: 6px; box-sizing: border-box; }
.login-btn { height: 44px; line-height: 44px; background: #2f6a4b; color: #fff; border-radius: 6px; }
</style>

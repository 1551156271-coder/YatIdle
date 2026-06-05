<template>
  <admin-layout title="分类管理" active="/pages/categories/categories">
    <view class="toolbar">
      <button class="primary" @click="openEditor()">新增分类</button>
      <button @click="load">刷新</button>
    </view>

    <view v-if="loading" class="loading">正在加载分类...</view>
    <view v-else-if="records.length === 0" class="empty">暂无分类数据</view>
    <view v-else class="table">
      <view class="tr th"><text>ID</text><text>名称</text><text>排序</text><text>状态</text><text>操作</text></view>
      <view v-for="c in records" :key="c.id" class="tr">
        <text>{{ c.id }}</text><text>{{ c.name }}</text><text>{{ c.sortOrder }}</text><text>{{ c.status === 1 ? '启用' : '禁用' }}</text>
        <view class="ops"><button size="mini" @click="openEditor(c)">编辑</button><button size="mini" class="danger" @click="remove(c)">删除</button></view>
      </view>
    </view>

    <view v-if="editorVisible" class="modal-mask" @click="closeEditor">
      <view class="modal" @click.stop>
        <view class="modal-header"><text class="modal-title">{{ form.id ? '编辑分类' : '新增分类' }}</text><button @click="closeEditor">关闭</button></view>
        <view class="modal-body">
          <view class="form-grid">
            <view><text class="detail-label">名称</text><input v-model="form.name" placeholder="分类名称" /></view>
            <view><text class="detail-label">排序</text><input v-model.number="form.sortOrder" placeholder="排序" /></view>
            <view class="wide"><text class="detail-label">状态</text><view class="segmented"><text :class="{ active: form.status === 1 }" @click="form.status = 1">启用</text><text :class="{ active: form.status === 0 }" @click="form.status = 0">禁用</text></view></view>
          </view>
        </view>
        <view class="modal-footer"><button @click="closeEditor">取消</button><button class="primary" :disabled="submitting" @click="submitEditor">保存</button></view>
      </view>
    </view>
  </admin-layout>
</template>

<script>
import AdminLayout from '../../components/admin-layout.vue'
import { listCategories, createCategory, updateCategory, deleteCategory } from '../../api/categories'

export default {
  components: { AdminLayout },
  data() { return { records: [], loading: false, editorVisible: false, form: { id: null, name: '', sortOrder: 0, status: 1 }, submitting: false } },
  onShow() { this.load() },
  methods: {
    async load() { this.loading = true; try { this.records = await listCategories() || [] } finally { this.loading = false } },
    openEditor(row) { this.form = row ? { id: row.id, name: row.name, sortOrder: row.sortOrder, status: row.status } : { id: null, name: '', sortOrder: 0, status: 1 }; this.editorVisible = true },
    closeEditor() { this.editorVisible = false },
    async submitEditor() {
      if (!this.form.name.trim()) return uni.showToast({ title: '请输入分类名称', icon: 'none' })
      this.submitting = true
      try {
        if (this.form.id) await updateCategory(this.form.id, { name: this.form.name, sortOrder: this.form.sortOrder, status: this.form.status })
        else await createCategory({ name: this.form.name, sortOrder: this.form.sortOrder, status: this.form.status })
        this.closeEditor()
        await this.load()
      } finally { this.submitting = false }
    },
    async remove(row) {
      const ok = await this.confirm(`确认删除分类 ${row.name}？`)
      if (!ok) return
      try {
        await deleteCategory(row.id)
        this.load()
      } catch (e) {
        uni.showToast({ title: e.message || '删除失败，请稍后重试', icon: 'none' })
      }
    },
    confirm(content) { return new Promise(resolve => uni.showModal({ title: '二次确认', content, confirmColor: '#b42318', success: r => resolve(r.confirm), fail: () => resolve(false) })) }
  }
}
</script>

<style scoped>
.tr { display: grid; grid-template-columns: 80px 1fr 120px 120px 180px; align-items: center; min-height: 50px; padding: 0 14px; border-bottom: 1px solid #edf1f5; font-size: 14px; }
.th { background: #f8fafc; font-weight: 700; color: #4a5568; }
.form-grid { display: grid; grid-template-columns: 1fr 160px; gap: 14px; }
.wide { grid-column: 1 / -1; }
</style>

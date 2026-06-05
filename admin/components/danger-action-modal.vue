<template>
  <view v-if="visible" class="modal-mask" @click="close">
    <view class="modal danger-action-modal" @click.stop>
      <view class="modal-header">
        <text class="modal-title">{{ title }}</text>
        <button :disabled="submitting" @click="close">关闭</button>
      </view>

      <view class="modal-body">
        <view class="detail-item wide">
          <text class="detail-label">操作对象</text>
          <text class="detail-value">{{ objectText || '-' }}</text>
        </view>

        <view v-if="impact" class="danger-impact">
          {{ impact }}
        </view>

        <view v-if="safeImages.length" class="detail-item wide image-panel">
          <text class="detail-label">相关图片</text>
          <view class="thumb-grid">
            <image
              v-for="(img, index) in safeImages"
              :key="img"
              :src="img"
              mode="aspectFit"
              class="thumb-img"
              @click="preview(index)"
            ></image>
          </view>
        </view>

        <slot name="extra"></slot>

        <textarea v-model="reason" :placeholder="reasonPlaceholder" />

        <view class="confirm-row" @click="confirmed = !confirmed">
          <text class="confirm-check" :class="{ active: confirmed }">{{ confirmed ? '✓' : '' }}</text>
          <text>{{ confirmText }}</text>
        </view>

        <view v-if="error" class="submit-error">{{ error }}</view>
      </view>

      <view class="modal-footer">
        <button :disabled="submitting" @click="close">取消</button>
        <button class="danger" :disabled="submitting" @click="confirmSubmit">
          {{ submitting ? '提交中...' : submitText }}
        </button>
      </view>
    </view>

    <view v-if="viewerVisible" class="danger-image-viewer-mask" @click.stop="closeViewer">
      <view class="danger-image-viewer" @click.stop>
        <view class="danger-image-viewer-header">
          <text>{{ viewerIndex + 1 }} / {{ safeImages.length }}</text>
          <button @click="closeViewer">关闭</button>
        </view>
        <view class="danger-image-viewer-body">
          <button v-if="safeImages.length > 1" class="danger-viewer-nav danger-viewer-prev" @click="prevViewer">上一张</button>
          <image v-if="currentViewerImage" :src="currentViewerImage" mode="aspectFit" class="danger-viewer-img"></image>
          <button v-if="safeImages.length > 1" class="danger-viewer-nav danger-viewer-next" @click="nextViewer">下一张</button>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
export default {
  name: 'DangerActionModal',
  props: {
    visible: { type: Boolean, default: false },
    title: { type: String, default: '确认危险操作' },
    objectText: { type: String, default: '' },
    impact: { type: String, default: '' },
    images: { type: Array, default: () => [] },
    submit: { type: Function, required: true },
    submitText: { type: String, default: '确认执行' },
    confirmText: { type: String, default: '我确认执行此操作，并理解该操作会写入后台日志。' },
    reasonPlaceholder: { type: String, default: '请输入操作原因，原因会写入操作日志' }
  },
  data() {
    return {
      reason: '',
      confirmed: false,
      submitting: false,
      error: '',
      viewerVisible: false,
      viewerIndex: 0
    }
  },
  computed: {
    safeImages() {
      return (this.images || []).filter(Boolean)
    },
    currentViewerImage() {
      return this.safeImages[this.viewerIndex] || ''
    }
  },
  watch: {
    visible(next) {
      if (next) this.reset()
    }
  },
  methods: {
    reset() {
      this.reason = ''
      this.confirmed = false
      this.submitting = false
      this.error = ''
      this.viewerVisible = false
      this.viewerIndex = 0
    },
    close() {
      if (this.submitting) return
      this.$emit('close')
    },
    preview(index) {
      if (!this.safeImages.length) return
      this.viewerIndex = Math.min(Math.max(Number(index) || 0, 0), this.safeImages.length - 1)
      this.viewerVisible = true
    },
    closeViewer() {
      this.viewerVisible = false
      this.viewerIndex = 0
    },
    prevViewer() {
      if (!this.safeImages.length) return
      this.viewerIndex = (this.viewerIndex + this.safeImages.length - 1) % this.safeImages.length
    },
    nextViewer() {
      if (!this.safeImages.length) return
      this.viewerIndex = (this.viewerIndex + 1) % this.safeImages.length
    },
    async confirmSubmit() {
      const reason = this.reason.trim()
      if (!reason) {
        this.error = '请填写操作原因。'
        return
      }
      if (!this.confirmed) {
        this.error = '请先勾选二次确认。'
        return
      }
      this.submitting = true
      this.error = ''
      try {
        const result = await this.submit(reason)
        this.$emit('success', result)
        this.$emit('close')
      } catch (e) {
        this.error = (e && (e.message || e.msg || e.error)) || '操作失败，请检查原因后重试。'
      } finally {
        this.submitting = false
      }
    }
  }
}
</script>

<style scoped>
.danger-action-modal {
  width: min(720px, calc(100vw - 48px));
}

.danger-action-modal .modal-body {
  overflow-x: hidden;
}

.danger-impact {
  margin: 12px 0;
  padding: 10px 12px;
  border: 1px solid #f0b8b2;
  border-radius: 6px;
  background: #fff7f6;
  color: #8f1d12;
  font-size: 13px;
  line-height: 1.5;
}

.thumb-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, 88px);
  gap: 10px;
  width: 100%;
  max-height: 196px;
  overflow-y: auto;
  overflow-x: hidden;
}

.thumb-img {
  width: 88px;
  height: 88px;
  display: block;
  border: 1px solid #dfe5ec;
  border-radius: 6px;
  background: #f8fafc;
  object-fit: contain;
  cursor: zoom-in;
}

.thumb-img > div,
.thumb-img img,
.thumb-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

::v-deep .thumb-img > div,
::v-deep .thumb-img img,
::v-deep .thumb-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

.danger-image-viewer-mask {
  position: fixed;
  z-index: 1300;
  left: 0;
  top: 0;
  right: 0;
  bottom: 0;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, 0.76);
}

.danger-image-viewer {
  width: min(880px, calc(100vw - 48px));
  max-height: calc(100vh - 48px);
  display: flex;
  flex-direction: column;
  border-radius: 8px;
  background: #111827;
  overflow: hidden;
  box-shadow: 0 18px 50px rgba(15, 23, 42, 0.35);
}

.danger-image-viewer-header {
  height: 48px;
  padding: 0 14px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  color: #f8fafc;
  font-size: 14px;
}

.danger-image-viewer-header button {
  width: auto;
  min-width: 64px;
  height: 32px;
  color: #1f2937;
  background: #fff;
}

.danger-image-viewer-body {
  position: relative;
  min-height: 300px;
  max-height: calc(100vh - 112px);
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 16px;
  overflow: hidden;
  background: #0f172a;
}

.danger-viewer-img {
  width: 100%;
  height: min(72vh, 640px);
  display: block;
  object-fit: contain;
}

.danger-viewer-img > div,
.danger-viewer-img img,
.danger-viewer-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

::v-deep .danger-viewer-img > div,
::v-deep .danger-viewer-img img,
::v-deep .danger-viewer-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

.danger-viewer-nav {
  position: absolute;
  top: 50%;
  z-index: 1;
  width: auto;
  min-width: 72px;
  height: 36px;
  transform: translateY(-50%);
  color: #1f2937;
  background: rgba(255, 255, 255, 0.92);
}

.danger-viewer-prev {
  left: 16px;
}

.danger-viewer-next {
  right: 16px;
}

.confirm-row {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-top: 12px;
  color: #4a5568;
  font-size: 13px;
  cursor: pointer;
  user-select: none;
}

.confirm-check {
  width: 16px;
  height: 16px;
  flex: 0 0 16px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  border: 1px solid #cfd8e3;
  border-radius: 4px;
  background: #fff;
  color: #fff;
  font-size: 12px;
  line-height: 16px;
}

.confirm-check.active {
  border-color: #b42318;
  background: #b42318;
}

.submit-error {
  margin-top: 12px;
  padding: 10px 12px;
  border: 1px solid #f0b8b2;
  border-radius: 6px;
  background: #fff7f6;
  color: #b42318;
  font-size: 13px;
  line-height: 1.5;
  word-break: break-word;
}
</style>

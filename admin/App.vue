<script>
export default {
  onLaunch() {
    const token = uni.getStorageSync('adminToken')
    const current = getCurrentPages()[0]
    if (!token && current && current.route !== 'pages/login/login') {
      uni.reLaunch({ url: '/pages/login/login' })
    }
  }
}
</script>

<style>
html,
body,
#app,
uni-app,
uni-page,
uni-page-body {
  width: 100%;
  min-height: 100%;
  margin: 0;
  padding: 0;
}

uni-view {
  display: block;
}

uni-text {
  display: inline;
}

page {
  min-height: 100%;
  background: #f4f6f8;
  color: #17202a;
  font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", sans-serif;
}

button,
uni-button {
  border-radius: 6px;
  cursor: pointer;
  user-select: none;
}

button *,
uni-button * {
  cursor: inherit;
  user-select: none;
}

button[disabled],
uni-button[disabled],
button[aria-disabled="true"],
uni-button[aria-disabled="true"] {
  cursor: not-allowed;
}

.login-panel uni-input,
.admin-shell uni-input {
  display: block;
  width: 100%;
  height: 42px;
  min-height: 42px;
  border: 1px solid #cfd8e3;
  border-radius: 6px;
  background: #fff;
  padding: 0;
  cursor: text;
}

.login-panel .uni-input-wrapper,
.admin-shell .uni-input-wrapper {
  position: relative;
  display: block;
  width: 100%;
  height: 100%;
  overflow: hidden;
  pointer-events: auto;
}

.login-panel .uni-input-input,
.admin-shell .uni-input-input {
  display: block !important;
  width: 100%;
  height: 100%;
  min-height: 40px;
  line-height: 40px;
  padding: 0 10px;
  border: none !important;
  outline: none !important;
  background: transparent !important;
  box-shadow: none !important;
  appearance: none;
  color: #17202a;
  font-size: 14px;
  box-sizing: border-box;
  cursor: text !important;
  user-select: text !important;
  pointer-events: auto !important;
}

.login-panel input,
.admin-shell input {
  padding: 0 10px;
  border: none;
  outline: none;
  background: transparent;
  box-sizing: border-box;
}

.login-panel .uni-input-placeholder,
.admin-shell .uni-input-placeholder {
  position: absolute;
  left: 10px;
  right: 10px;
  top: 0;
  height: 100%;
  line-height: 40px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  pointer-events: none;
}

.login-panel uni-button,
.admin-shell uni-button {
  width: auto;
  min-width: 64px;
  height: 34px;
  line-height: 32px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0 12px;
  border: 1px solid #cfd8e3;
  border-radius: 6px;
  background: #fff;
  color: #1f2937;
  font-size: 14px;
}

.login-panel uni-button::after,
.admin-shell uni-button::after {
  border: none;
}

.login-panel .login-btn {
  width: 100%;
  height: 44px;
  line-height: 42px;
  margin-top: 6px;
  background: #2f6a4b;
  color: #fff;
  border-color: #2f6a4b;
  font-weight: 600;
}

.admin-shell,
.admin-shell * {
  box-sizing: border-box;
}

.admin-shell button,
.admin-shell uni-button {
  width: auto;
  min-width: 64px;
  height: 34px;
  line-height: 32px;
  display: inline-flex !important;
  align-items: center;
  justify-content: center;
  margin: 0;
  padding: 0 12px;
  border: 1px solid #cfd8e3;
  border-radius: 6px;
  background: #fff;
  color: #1f2937;
  font-size: 14px;
  cursor: pointer;
  user-select: none;
}

.admin-shell button *,
.admin-shell uni-button * {
  cursor: inherit;
  user-select: none;
}

.admin-shell button[disabled],
.admin-shell uni-button[disabled],
.admin-shell button[aria-disabled="true"],
.admin-shell uni-button[aria-disabled="true"] {
  cursor: not-allowed;
}

.admin-shell button::after,
.admin-shell uni-button::after {
  border: none;
}

.admin-shell button[type="warn"],
.admin-shell button.warn,
.admin-shell uni-button[type="warn"],
.admin-shell uni-button.warn {
  border-color: #f0b8b2;
  color: #b42318;
  background: #fff7f6;
}

.admin-shell uni-input {
  display: block;
  width: 100%;
  height: 36px;
  min-height: 36px;
  line-height: 36px;
  margin: 0;
  padding: 0;
  border: 1px solid #cfd8e3;
  border-radius: 6px;
  background: #fff;
  color: #17202a;
  font-size: 14px;
  cursor: text;
}

.admin-shell .toolbar uni-input {
  width: 220px;
  height: 36px;
  min-height: 36px;
}

.admin-shell .toolbar input,
.admin-shell .toolbar uni-input {
  width: 220px;
  height: 36px;
  min-height: 36px;
  line-height: 36px;
}

.admin-shell .toolbar .uni-input-input {
  min-height: 34px;
  line-height: 34px;
}

.admin-shell .toolbar {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-wrap: wrap;
  margin-bottom: 14px;
}

.admin-shell .toolbar button,
.admin-shell .toolbar uni-button,
.admin-shell .pager button,
.admin-shell .pager uni-button {
  height: 36px;
  line-height: 34px;
}

.admin-shell .table {
  width: 100%;
  max-width: 100%;
  overflow-x: auto;
  background: #fff;
  border: 1px solid #dfe5ec;
  border-radius: 8px;
}

.admin-shell .tr {
  min-width: 860px;
}

.admin-shell .tr > text,
.admin-shell .tr > view,
.admin-shell .tr > uni-text,
.admin-shell .tr > uni-view {
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.admin-shell .ops {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.admin-shell .segmented {
  display: flex;
  height: 36px;
  border: 1px solid #cfd8e3;
  border-radius: 6px;
  overflow: hidden;
  background: #fff;
}

.admin-shell .segmented text,
.admin-shell .segmented uni-text {
  min-width: 64px;
  height: 34px;
  line-height: 34px;
  padding: 0 12px;
  text-align: center;
  color: #4a5568;
  cursor: pointer;
  user-select: none;
  border-right: 1px solid #e5ebf2;
  white-space: nowrap;
}

.admin-shell .segmented text:last-child,
.admin-shell .segmented uni-text:last-child {
  border-right: none;
}

.admin-shell .segmented .active {
  background: #2f6a4b;
  color: #fff;
}

.admin-shell .pager {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 10px;
  margin-top: 12px;
  color: #4a5568;
  font-size: 13px;
}

.admin-shell .empty,
.admin-shell .loading {
  padding: 36px 12px;
  color: #718096;
  text-align: center;
  background: #fff;
  border: 1px solid #dfe5ec;
  border-radius: 8px;
}

.admin-shell .modal-mask {
  position: fixed;
  inset: 0;
  z-index: 99;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 24px;
  background: rgba(15, 23, 42, .42);
}

.admin-shell .modal {
  width: min(760px, calc(100vw - 48px));
  max-height: calc(100vh - 80px);
  display: flex;
  flex-direction: column;
  overflow: hidden;
  background: #fff;
  border-radius: 8px;
  box-shadow: 0 24px 60px rgba(15, 23, 42, .22);
}

.admin-shell .modal.large {
  width: min(980px, calc(100vw - 48px));
}

.admin-shell .modal-header,
.admin-shell .modal-footer {
  flex: 0 0 auto;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px 18px;
  border-bottom: 1px solid #edf1f5;
}

.admin-shell .modal-footer {
  justify-content: flex-end;
  gap: 10px;
  border-top: 1px solid #edf1f5;
  border-bottom: none;
}

.admin-shell .modal-title {
  font-weight: 700;
  color: #17202a;
}

.admin-shell .modal-body {
  flex: 1 1 auto;
  min-height: 0;
  overflow-y: auto;
  padding: 18px;
}

.admin-shell .detail-grid {
  display: grid;
  grid-template-columns: repeat(2, minmax(0, 1fr));
  gap: 12px;
}

.admin-shell .detail-item {
  min-width: 0;
  padding: 10px 12px;
  border: 1px solid #edf1f5;
  border-radius: 6px;
  background: #f8fafc;
}

.admin-shell .detail-item.wide {
  grid-column: 1 / -1;
}

.admin-shell .detail-label {
  display: block;
  margin-bottom: 5px;
  color: #718096;
  font-size: 12px;
}

.admin-shell .detail-value {
  color: #17202a;
  font-size: 14px;
  line-height: 1.5;
  word-break: break-all;
}

.admin-shell uni-textarea {
  display: block;
  width: 100%;
  min-height: 96px;
  padding: 10px;
  border: 1px solid #cfd8e3;
  border-radius: 6px;
  background: #fff;
  color: #17202a;
  font-size: 14px;
  box-sizing: border-box;
  cursor: text;
}

.admin-shell .uni-textarea-wrapper {
  position: relative;
  display: block;
  width: 100%;
  min-height: 96px;
  overflow: hidden;
}

.admin-shell textarea,
.admin-shell .uni-textarea-textarea {
  display: block !important;
  width: 100%;
  min-height: 96px;
  padding: 10px;
  border: none !important;
  outline: none !important;
  background: transparent !important;
  box-shadow: none !important;
  color: #17202a;
  font-size: 14px;
  line-height: 1.5;
  box-sizing: border-box;
  cursor: text !important;
  user-select: text !important;
}

.admin-shell .uni-textarea-placeholder {
  position: absolute;
  left: 10px;
  right: 10px;
  top: 10px;
  color: #9aa5b1;
  pointer-events: none;
}

.admin-shell .image-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, 96px);
  gap: 10px;
  width: 100%;
  max-width: 100%;
  overflow: hidden;
}

.admin-shell .preview-img {
  width: 96px;
  height: 96px;
  display: block;
  border-radius: 6px;
  border: 1px solid #dfe5ec;
  object-fit: contain;
  background: #f8fafc;
  cursor: zoom-in;
}

.admin-shell .cover-img > div,
.admin-shell .cover-img img,
.admin-shell .cover-img .uni-image-img,
.admin-shell .preview-img > div,
.admin-shell .preview-img img,
.admin-shell .preview-img .uni-image-img,
.admin-shell .thumb-img > div,
.admin-shell .thumb-img img,
.admin-shell .thumb-img .uni-image-img {
  width: 100% !important;
  height: 100% !important;
  object-fit: contain !important;
  background-size: contain !important;
  background-position: center !important;
  background-repeat: no-repeat !important;
}

.admin-shell .danger {
  border-color: #f0b8b2;
  color: #b42318;
  background: #fff7f6;
}

.admin-shell .primary {
  border-color: #2f6a4b;
  color: #fff;
  background: #2f6a4b;
}

.admin-shell .muted {
  color: #718096;
}

@media (max-width: 900px) {
  .admin-shell .detail-grid {
    grid-template-columns: 1fr;
  }
}
</style>

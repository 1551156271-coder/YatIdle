<template>
	<view class="wallet-page">
		<!-- 余额卡片 -->
		<view class="balance-card">
			<text class="balance-label">钱包余额（元）</text>
			<text class="balance-num">{{ wallet.balance.toFixed(2) }}</text>
			<view class="balance-row">
				<view class="balance-item">
					<text class="bi-num">{{ wallet.totalIncome.toFixed(2) }}</text>
					<text class="bi-label">累计收入</text>
				</view>
				<view class="balance-item">
					<text class="bi-num">{{ wallet.totalExpense.toFixed(2) }}</text>
					<text class="bi-label">累计支出</text>
				</view>
			</view>
		</view>

		<!-- 交易明细 -->
		<view class="section-card">
			<view class="section-head">
				<text class="section-title">交易明细</text>
			</view>
			<view v-if="loading" class="empty-inline">
				<text class="empty-txt">加载中...</text>
			</view>
			<view v-else-if="transactions.length > 0" class="tx-list">
				<view v-for="t in transactions" :key="t.id + '_' + t.txType" class="tx-item" @click="goDetail(t)">
					<image class="tx-img" :src="t.image" mode="aspectFill"></image>
					<view class="tx-info">
						<text class="tx-title">{{ t.title }}</text>
						<view class="tx-meta">
							<text class="tx-time">{{ t.time }}</text>
							<text class="tx-tag" :class="t.txType === 'sold' ? 'tag-sold' : 'tag-bought'">{{ t.txType === 'sold' ? '已售' : '已购' }}</text>
							<text class="tx-status" :class="t.status === 'COMPLETED' ? 'st-done' : 'st-pending'">{{ t.statusLabel }}</text>
						</view>
					</view>
					<text class="tx-amount" :class="t.txType === 'sold' ? 'tx-income' : 'tx-expense'">
						{{ t.txType === 'sold' ? '+' : '-' }}¥{{ Number(t.price || 0).toFixed(2) }}
					</text>
				</view>
			</view>
			<view v-else class="empty-inline">
				<text class="empty-txt">暂无交易记录</text>
			</view>
		</view>
	</view>
</template>

<script>
	import { getWallet } from '@/api/wallet.js'
	import { getMySellOrders, getMyBuyOrders } from '@/api/order.js'

	export default {
		data() {
			return {
				wallet: {
					balance: 0,
					totalIncome: 0,
					totalExpense: 0
				},
				transactions: [],
				loading: true
			}
		},
		onShow() {
			this.loadData()
		},
		methods: {
			async loadData() {
				const user = uni.getStorageSync('user')
				const userId = user ? user.id : 1
				this.loading = true
				try {
					const [walletData, sellRes, buyRes] = await Promise.all([
						getWallet(userId).catch(() => null),
						getMySellOrders(userId).catch(() => []),
						getMyBuyOrders(userId).catch(() => [])
					])
					if (walletData) {
						this.wallet = {
							balance: walletData.balance || 0,
							totalIncome: walletData.totalIncome || 0,
							totalExpense: walletData.totalExpense || 0
						}
					}

					const sellList = (sellRes.records || sellRes || []).map(o => ({ ...o, txType: 'sold' }))
					const buyList = (buyRes.records || buyRes || []).map(o => ({ ...o, txType: 'bought' }))
					const merged = [...sellList, ...buyList]
						.filter(o => o.status !== 'CANCELLED')
						.sort((a, b) => new Date(b.createTime) - new Date(a.createTime))

					this.transactions = merged.map(o => ({
						id: o.id,
						txType: o.txType,
						type: o.txType === 'sold' ? 'INCOME' : 'EXPENSE',
						title: o.itemTitle || ('商品 #' + o.itemId),
						image: o.itemImageUrl || '',
						price: o.price,
						amount: o.price,
						status: o.status,
						statusLabel: this.mapStatus(o.status),
						time: this.formatTime(o.createTime),
						_raw: o
					}))
				} catch (e) {
					// ignore
				} finally {
					this.loading = false
				}
			},
			mapStatus(status) {
				const map = { PENDING: '待交易', COMPLETED: '已完成', CANCELLED: '已取消' }
				return map[status] || status
			},
			goDetail(t) {
				uni.setStorageSync('currentOrder', t._raw)
				uni.navigateTo({ url: '/pages/order-detail/order-detail?id=' + t.id + '&type=' + (t.txType === 'bought' ? 'purchased' : 'sold') })
			},
			formatTime(dateStr) {
				if (!dateStr) return ''
				const t = new Date(dateStr)
				const month = t.getMonth() + 1
				const day = t.getDate()
				const hour = String(t.getHours()).padStart(2, '0')
				const min = String(t.getMinutes()).padStart(2, '0')
				return month + '月' + day + '日 ' + hour + ':' + min
			}
		}
	}
</script>

<style>
	.wallet-page {
		min-height: 100vh;
		background: #f5f5f5;
		padding: 20rpx;
		box-sizing: border-box;
	}

	/* 余额卡片 */
	.balance-card {
		background: linear-gradient(135deg, #3A6341, #4E7D56);
		border-radius: 20rpx;
		padding: 40rpx 30rpx 30rpx;
		display: flex; flex-direction: column; align-items: center;
		margin-bottom: 20rpx;
	}
	.balance-label {
		font-size: 26rpx; color: rgba(255,255,255,0.8);
		margin-bottom: 12rpx;
	}
	.balance-num {
		font-size: 72rpx; color: #ffffff; font-weight: bold;
		line-height: 1; margin-bottom: 32rpx;
	}
	.balance-row {
		display: flex; width: 100%;
		justify-content: space-around;
		border-top: 1rpx solid rgba(255,255,255,0.2);
		padding-top: 24rpx;
	}
	.balance-item {
		display: flex; flex-direction: column; align-items: center;
	}
	.bi-num {
		font-size: 30rpx; color: #ffffff; font-weight: bold;
		margin-bottom: 6rpx;
	}
	.bi-label {
		font-size: 22rpx; color: rgba(255,255,255,0.7);
	}

	/* 明细 */
	.section-card {
		background: #ffffff; margin-bottom: 20rpx; border-radius: 20rpx;
		padding: 24rpx;
		box-shadow: 0 2rpx 12rpx rgba(0,0,0,0.04);
	}
	.section-head { margin-bottom: 10rpx; }
	.section-title { font-size: 30rpx; color: #333; font-weight: bold; }

	.tx-item {
		display: flex; align-items: center; justify-content: space-between;
		padding: 22rpx 0; border-bottom: 1rpx solid #f5f5f5;
	}
	.tx-item:last-child { border-bottom: none; }
	.tx-img {
		width: 100rpx; height: 100rpx;
		border-radius: 10rpx; background: #eee;
		flex-shrink: 0; margin-right: 18rpx;
	}
	.tx-info { flex: 1; overflow: hidden; }
	.tx-title {
		font-size: 28rpx; color: #333; display: block;
		overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
		margin-bottom: 6rpx;
	}
	.tx-meta { display: flex; align-items: center; gap: 12rpx; }
	.tx-time { font-size: 22rpx; color: #ccc; }
	.tx-tag { font-size: 20rpx; padding: 2rpx 10rpx; border-radius: 4rpx; }
	.tag-sold { color: #2E7D32; background: #e8f5e9; }
	.tag-bought { color: #e74c3c; background: #fdecea; }
	.tx-status { font-size: 20rpx; }
	.st-done { color: #2E7D32; }
	.st-pending { color: #f0ad4e; }
	.tx-amount { font-size: 30rpx; font-weight: bold; flex-shrink: 0; margin-left: 16rpx; }
	.tx-income { color: #2E7D32; }
	.tx-expense { color: #e74c3c; }

	/* 空态 */
	.empty-inline { padding: 30rpx 0; text-align: center; }
	.empty-txt { font-size: 24rpx; color: #ccc; }
</style>

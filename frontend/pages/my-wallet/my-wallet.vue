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
				<view v-for="t in transactions" :key="t.id" class="tx-item">
					<view class="tx-left">
						<text class="tx-icon">{{ t.type === 'INCOME' ? '↓' : '↑' }}</text>
						<view class="tx-info">
							<text class="tx-title">{{ t.title }}</text>
							<text class="tx-time">{{ t.time }}</text>
						</view>
					</view>
					<text class="tx-amount" :class="t.type === 'INCOME' ? 'tx-income' : 'tx-expense'">
						{{ t.type === 'INCOME' ? '+' : '' }}{{ t.amount }}
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
	import { getWallet, getWalletTransactions } from '@/api/wallet.js'

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
		onLoad() {
			this.loadData()
		},
		methods: {
			async loadData() {
				const user = uni.getStorageSync('user')
				const userId = user ? user.id : 1
				this.loading = true
				try {
					const [walletData, txList] = await Promise.all([
						getWallet(userId).catch(() => null),
						getWalletTransactions(userId).catch(() => [])
					])
					if (walletData) {
						this.wallet = {
							balance: walletData.balance || 0,
							frozenAmount: walletData.frozenAmount || 0,
							totalIncome: walletData.totalIncome || 0,
							totalExpense: walletData.totalExpense || 0
						}
					}
					this.transactions = (txList || []).map(t => ({
						id: t.id,
						type: t.type,
						title: t.title,
						amount: t.amount,
						balanceAfter: t.balanceAfter,
						time: this.formatTime(t.createTime)
					}))
				} catch (e) {
					// ignore
				} finally {
					this.loading = false
				}
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
	.tx-left { display: flex; align-items: center; gap: 16rpx; flex: 1; overflow: hidden; }
	.tx-icon {
		width: 52rpx; height: 52rpx; border-radius: 50%;
		background: #f5f5f5;
		display: flex; align-items: center; justify-content: center;
		font-size: 28rpx; flex-shrink: 0;
	}
	.tx-info { flex: 1; overflow: hidden; }
	.tx-title {
		font-size: 28rpx; color: #333; display: block;
		overflow: hidden; text-overflow: ellipsis; white-space: nowrap;
	}
	.tx-time { font-size: 22rpx; color: #ccc; margin-top: 4rpx; display: block; }
	.tx-amount { font-size: 30rpx; font-weight: bold; flex-shrink: 0; margin-left: 16rpx; }
	.tx-income { color: #2E7D32; }
	.tx-expense { color: #e74c3c; }

	/* 空态 */
	.empty-inline { padding: 30rpx 0; text-align: center; }
	.empty-txt { font-size: 24rpx; color: #ccc; }
</style>

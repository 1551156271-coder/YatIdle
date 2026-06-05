import { get } from './index'

export function getWallet(userId) {
  return get('/api/user/wallet', { userId })
}

export function getWalletTransactions(userId) {
  return get('/api/wallet/transactions', { userId })
}

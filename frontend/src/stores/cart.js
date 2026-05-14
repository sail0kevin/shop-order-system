import { defineStore } from 'pinia'
import { getCartList, addCart as addCartApi, updateCart as updateCartApi, removeCart as removeCartApi } from '@/api/cart'

export const useCartStore = defineStore('cart', {
  state: () => ({
    items: [],
    loading: false
  }),
  getters: {
    totalCount: state => state.items.reduce((s, i) => s + i.quantity, 0),
    totalAmount: state => state.items.reduce((s, i) => s + parseFloat(i.subtotal), 0),
    isEmpty: state => state.items.length === 0
  },
  actions: {
    async fetchCart() {
      this.loading = true
      try { this.items = await getCartList() || [] }
      finally { this.loading = false }
    },
    async addCart(productId, quantity) {
      await addCartApi({ productId, quantity })
      await this.fetchCart()
    },
    async updateCart(productId, quantity) {
      if (quantity < 1) { await this.removeCart(productId); return }
      await updateCartApi({ productId, quantity })
      await this.fetchCart()
    },
    async removeCart(productId) {
      await removeCartApi({ productId })
      await this.fetchCart()
    },
    clearCart() { this.items = [] }
  }
})

import { defineStore } from 'pinia'
import { login as loginApi, register as registerApi, getUserInfo } from '@/api/user'

export const useAuthStore = defineStore('auth', {
  state: () => ({
    token: localStorage.getItem('token') || '',
    refreshToken: localStorage.getItem('refreshToken') || '',
    user: null
  }),
  getters: {
    isLoggedIn: state => !!state.token,
    userId: state => state.user?.id,
    nickname: state => state.user?.nickname || state.user?.username || ''
  },
  actions: {
    async login(credentials) {
      const data = await loginApi(credentials)
      this.token = data.token
      this.refreshToken = data.refreshToken
      localStorage.setItem('token', data.token)
      localStorage.setItem('refreshToken', data.refreshToken)
      await this.fetchUser()
    },
    async register(data) {
      await registerApi(data)
    },
    async fetchUser() {
      this.user = await getUserInfo()
    },
    logout() {
      this.token = ''
      this.refreshToken = ''
      this.user = null
      localStorage.removeItem('token')
      localStorage.removeItem('refreshToken')
    }
  }
})

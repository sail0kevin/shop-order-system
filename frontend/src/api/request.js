import axios from 'axios'
import { ElMessage } from 'element-plus'

const request = axios.create({ baseURL: '/api', timeout: 15000 })

// 无感刷新队列
let isRefreshing = false
let pendingQueue = []

function onRefreshed(token) {
  pendingQueue.forEach(cb => cb(token))
  pendingQueue = []
}

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) config.headers.Authorization = 'Bearer ' + token
  return config
})

request.interceptors.response.use(
  res => {
    const body = res.data
    if (body.code === 200) return body.data
    ElMessage.error(body.msg || '请求失败')
    return Promise.reject(new Error(body.msg))
  },
  async err => {
    const { response, config } = err
    if (!response || response.status !== 401 || config.url === '/user/refresh') {
      ElMessage.error('网络错误')
      return Promise.reject(err)
    }

    // 401 尝试刷新 Token
    if (!isRefreshing) {
      isRefreshing = true
      const refreshToken = localStorage.getItem('refreshToken')
      if (!refreshToken) {
        localStorage.clear()
        window.location.href = '/login'
        return Promise.reject(err)
      }
      try {
        const res = await axios.post('/api/user/refresh', { refreshToken })
        const data = res.data.data
        localStorage.setItem('token', data.token)
        localStorage.setItem('refreshToken', data.refreshToken)
        isRefreshing = false
        onRefreshed(data.token)
        config.headers.Authorization = 'Bearer ' + data.token
        return request(config)  // 重试原请求
      } catch {
        localStorage.clear()
        window.location.href = '/login'
        return Promise.reject(err)
      }
    } else {
      // 正在刷新，排队等待
      return new Promise(resolve => {
        pendingQueue.push(token => {
          config.headers.Authorization = 'Bearer ' + token
          resolve(request(config))
        })
      })
    }
  }
)

export default request

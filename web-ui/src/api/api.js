import axios from 'axios'
import { getCookie, setCookie, deleteCookie } from '../utils/cookies'

// Use Vite env var (VITE_API_BASE) in the browser, fallback to localhost
const API_BASE = import.meta.env.VITE_API_BASE || 'http://localhost:8080'

const api = axios.create({ baseURL: API_BASE })

const triggerLogout = () => {
  deleteCookie('access_token')
  deleteCookie('refresh_token')
  if (typeof window !== 'undefined') {
    window.dispatchEvent(new Event('auth:logout'))
  }
}

// simple token handling
api.interceptors.request.use(config => {
  const token = getCookie('access_token')
  if (token) config.headers.Authorization = `Bearer ${token}`
  return config
})

api.interceptors.response.use(
  r => r,
  async err => {
    const original = err.config
    if (err.response && err.response.status === 401 && !original._retry) {
      original._retry = true
      const refresh = getCookie('refresh_token')
      if (!refresh) {
        triggerLogout()
        return Promise.reject(err)
      }
      try {
        const resp = await axios.post(`${API_BASE}/api/auth/reissue`, { refreshToken: refresh })
        const { accessToken, refreshToken } = resp.data || {}
        if (accessToken) setCookie('access_token', accessToken)
        if (refreshToken) setCookie('refresh_token', refreshToken)
        original.headers.Authorization = `Bearer ${accessToken}`
        return api(original)
      } catch (e) {
        triggerLogout()
        return Promise.reject(e)
      }
    }
    return Promise.reject(err)
  }
)

export async function signup(payload) {
  const res = await api.post('/api/auth/signup', payload)
  return res.data
}

export async function login(payload) {
  const res = await api.post('/api/auth/login', payload)
  // assume response contains accessToken/refreshToken
  if (res.data?.accessToken) setCookie('access_token', res.data.accessToken)
  if (res.data?.refreshToken) setCookie('refresh_token', res.data.refreshToken)
  return res.data
}

export async function reissue(payload) {
  const res = await api.post('/api/auth/reissue', payload)
  return res.data
}

export async function getQuote(symbol) {
  const res = await api.get(`/api/market/quote/${encodeURIComponent(symbol)}`)
  return res.data
}

export async function getQuotesBulk(symbolsCsv) {
  const res = await api.get(`/api/market/quote`, { params: { symbols: symbolsCsv } })
  return res.data
}

export async function getHistorical(symbol, start, end, interval = '1d') {
  const res = await api.get(`/api/market/historical/${encodeURIComponent(symbol)}`, {
    params: { start, end, interval }
  })
  return res.data
}

export async function getInfo(symbol) {
  const res = await api.get(`/api/market/info/${encodeURIComponent(symbol)}`)
  return res.data
}

export async function getSnapshot(symbol) {
  const res = await api.get(`/api/market/snapshot/${encodeURIComponent(symbol)}`)
  return res.data
}

export async function getExchangeRates() {
  const res = await api.get('/api/exchange')
  return res.data
}

export async function getMyAccounts(filters = {}) {
  const params = new URLSearchParams()
  if (filters.currencies?.length) {
    filters.currencies.forEach(currency => params.append('currencies', currency))
  }
  if (filters.types?.length) {
    filters.types.forEach(type => params.append('types', type))
  }
  if (filters.statuses?.length) {
    filters.statuses.forEach(status => params.append('statuses', status))
  }
  const res = await api.get('/api/account/myAccounts', {
    params,
  })
  return res.data
}

export async function createAccount(payload) {
  const res = await api.post('/api/account/create', payload)
  return res.data
}

export async function depositAccount(payload) {
  const res = await api.post('/api/account/deposit', payload)
  return res.data
}

export async function withdrawAccount(payload) {
  const res = await api.post('/api/account/withdraw', payload)
  return res.data
}

export async function transferAccount(payload) {
  const res = await api.post('/api/account/transfer', payload)
  return res.data
}

export async function createOrder(payload) {
  const res = await api.post('/api/order/create', payload)
  return res.data
}

export async function getOrders(page = 0, size = 10, status) {
  const params = { page, size }
  if (status) params.status = status
  const res = await api.post('/api/order', null, { params })
  return res.data
}

export async function cancelOrder(orderId) {
  const res = await api.post(`/api/order/${encodeURIComponent(orderId)}/cancel`)
  return res.data
}

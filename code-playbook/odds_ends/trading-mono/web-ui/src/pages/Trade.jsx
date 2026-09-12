import React, { useCallback, useEffect, useMemo, useState } from 'react'
import { cancelOrder, createOrder, getMyAccounts, getOrders, getSnapshot } from '../api/api'
import SymbolSearch from '../components/SymbolSearch'
import { useAuth } from '../auth/AuthProvider'
import TradeAccountsCard from '../components/trade/TradeAccountsCard'
import TradeOrderFormCard from '../components/trade/TradeOrderFormCard'
import TradeOrderbookCard from '../components/trade/TradeOrderbookCard'
import TradeOrdersPanel from '../components/trade/TradeOrdersPanel'
import TradeQuoteCard from '../components/trade/TradeQuoteCard'

export default function Trade() {
  const getCookie = name => {
    const match = document.cookie.match(new RegExp(`(?:^|; )${name}=([^;]*)`))
    return match ? decodeURIComponent(match[1]) : ''
  }

  const setCookie = (name, value, days = 7) => {
    const expires = new Date(Date.now() + days * 86400000).toUTCString()
    document.cookie = `${name}=${encodeURIComponent(value)}; expires=${expires}; path=/`
  }

  const initialSymbol = getCookie('last_symbol') || 'AAPL'
  const auth = useAuth()
  const [symbol, setSymbol] = useState(initialSymbol)
  const [snapshot, setSnapshot] = useState(null)
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [orderType, setOrderType] = useState('LIMIT')
  const [quantity, setQuantity] = useState('')
  const [limitPrice, setLimitPrice] = useState('')
  const [selectedAccountNo, setSelectedAccountNo] = useState('')
  const [orderbook, setOrderbook] = useState({ bids: [], asks: [] })
  const [orderbookUpdatedAt, setOrderbookUpdatedAt] = useState(null)
  const [notice, setNotice] = useState(null)
  const [orderError, setOrderError] = useState(null)
  const [submitting, setSubmitting] = useState(false)
  const [ordersData, setOrdersData] = useState(null)
  const [ordersLoading, setOrdersLoading] = useState(false)
  const [ordersError, setOrdersError] = useState(null)
  const [ordersMessage, setOrdersMessage] = useState(null)
  const [ordersSize, setOrdersSize] = useState(10)
  const [ordersStatus, setOrdersStatus] = useState('')

  const currency = snapshot?.currency || snapshot?.info?.currency || ''
  const price = snapshot?.current_price || snapshot?.quote?.current_price || 0
  const getCurrencyDecimalsFor = useCallback(code => {
    const map = {
      KRW: 0,
      JPY: 0,
      USD: 2,
      EUR: 2,
      GBP: 2,
      CNY: 2,
      HKD: 2,
      SGD: 2,
    }
    return map[code] ?? 2
  }, [])

  const currencyDecimals = useMemo(() => getCurrencyDecimalsFor(currency), [currency, getCurrencyDecimalsFor])

  const truncateTo = useCallback((value, decimals) => {
    const factor = Math.pow(10, decimals)
    return Math.trunc((Number(value) || 0) * factor) / factor
  }, [])

  const formatAmount = useCallback(
    value => {
      const trimmed = truncateTo(value, currencyDecimals)
      return new Intl.NumberFormat('en-US', {
        minimumFractionDigits: currencyDecimals,
        maximumFractionDigits: currencyDecimals,
      }).format(trimmed)
    },
    [currencyDecimals, truncateTo]
  )

  const totalAvailable = useMemo(() => {
    const sum = accounts.reduce((acc, acct) => acc + (Number(acct.availableCash) || 0), 0)
    return truncateTo(sum, currencyDecimals)
  }, [accounts, truncateTo, currencyDecimals])

  const selectedAccount = useMemo(() => {
    return accounts.find(acct => acct.accountNo === selectedAccountNo) || null
  }, [accounts, selectedAccountNo])

  const selectedAvailable = truncateTo(selectedAccount?.availableCash, currencyDecimals)

  const estimatedCost = useMemo(() => {
    const qty = Number(quantity) || 0
    const unit = orderType === 'LIMIT' ? Number(limitPrice) || 0 : Number(price) || 0
    return truncateTo(qty * unit, currencyDecimals)
  }, [quantity, limitPrice, orderType, price, truncateTo, currencyDecimals])

  const buildOrderbook = useCallback((nextPrice, decimals = currencyDecimals) => {
    const basePrice = Number(nextPrice) || 0
    if (!basePrice) {
      setOrderbook({ bids: [], asks: [] })
      setOrderbookUpdatedAt(null)
      return
    }
    const step = Math.max(1 / Math.pow(10, decimals), basePrice * 0.001)
    const round = value => truncateTo(value, decimals)
    const seed = Math.floor(basePrice * 100)
    const makeQty = idx => (seed + idx * 13) % 90 + 10
    const bids = Array.from({ length: 6 }, (_, idx) => ({
      price: round(basePrice - step * (idx + 1)),
      qty: makeQty(idx + 1),
    }))
    const asks = Array.from({ length: 6 }, (_, idx) => ({
      price: round(basePrice + step * (idx + 1)),
      qty: makeQty(idx + 7),
    }))
    setOrderbook({ bids, asks })
    setOrderbookUpdatedAt(new Date())
  }, [currencyDecimals, truncateTo])

  const miniSeries = useMemo(() => {
    const base = Number(price) || 0
    if (!base) return []
    const seed = Math.floor(base * 10)
    const points = Array.from({ length: 16 }, (_, idx) => {
      const drift = Math.sin((idx + seed) * 0.6) * (base * 0.004)
      const jitter = ((seed + idx * 7) % 9 - 4) * (base * 0.0006)
      return base + drift + jitter
    })
    return points
  }, [price])

  const miniChartData = useMemo(() => {
    if (miniSeries.length === 0) return []
    const interval = 5 * 60
    const now = Math.floor(Date.now() / 1000 / interval) * interval
    return miniSeries.map((value, idx) => ({
      time: now - (miniSeries.length - 1 - idx) * interval,
      value,
    }))
  }, [miniSeries])

  const refreshAccounts = useCallback(async currencyCode => {
    const code = currencyCode || currency || snapshot?.info?.currency || ''
    if (!code) return
    try {
      const acctList = await getMyAccounts({ currencies: [code].filter(Boolean) })
      setAccounts(Array.isArray(acctList) ? acctList : [])
    } catch (e) {
      // ignore refresh errors
    }
  }, [currency, snapshot])

  const fetchSnapshot = useCallback(async nextSymbol => {
    const target = (nextSymbol || symbol || '').trim()
    if (!target) return
    setLoading(true)
    setError(null)
    setNotice(null)
    try {
      const data = await getSnapshot(target)
      setSnapshot(data)
      const dataCurrency = data?.currency || data?.info?.currency || ''
      const decimals = getCurrencyDecimalsFor(dataCurrency)
      buildOrderbook(data?.current_price || data?.quote?.current_price || 0, decimals)
      await refreshAccounts(dataCurrency)
    } catch (e) {
      setError(e.response?.data || e.message)
      setSnapshot(null)
      setAccounts([])
      buildOrderbook(0, currencyDecimals)
    } finally {
      setLoading(false)
    }
  }, [symbol, buildOrderbook, getCurrencyDecimalsFor, refreshAccounts, currencyDecimals])

  useEffect(() => {
    fetchSnapshot(symbol)
  }, [])

  useEffect(() => {
    if (symbol) setCookie('last_symbol', symbol)
  }, [symbol])

  const fetchOrders = useCallback(async () => {
    setOrdersLoading(true)
    setOrdersError(null)
    setOrdersMessage(null)
    try {
      const res = await getOrders(0, ordersSize, ordersStatus || undefined)
      setOrdersData(res)
    } catch (e) {
      setOrdersError(e.response?.data || e.message)
    } finally {
      setOrdersLoading(false)
    }
  }, [ordersSize, ordersStatus])

  useEffect(() => {
    fetchOrders()
  }, [fetchOrders])

  useEffect(() => {
    if (accounts.length === 0) {
      setSelectedAccountNo('')
      return
    }
    const exists = accounts.some(acct => acct.accountNo === selectedAccountNo)
    if (!exists) setSelectedAccountNo(accounts[0].accountNo)
  }, [accounts, selectedAccountNo])

  const onSubmit = e => {
    e.preventDefault()
    setNotice(null)
    setOrderError(null)
    if (!snapshot) {
      setOrderError('종목을 먼저 조회하세요.')
      return
    }
    if (!selectedAccountNo) {
      setOrderError('구매 계좌를 선택하세요.')
      return
    }
    const qty = Number(quantity)
    if (!qty || qty <= 0) {
      setOrderError('수량을 입력하세요.')
      return
    }
    if (orderType === 'LIMIT' && (!limitPrice || Number(limitPrice) <= 0)) {
      setOrderError('지정가를 입력하세요.')
      return
    }
    const payload = {
      accountNo: selectedAccountNo,
      symbol: snapshot.symbol,
      side: 'BUY',
      type: orderType,
      tif: 'DAY',
      qty,
      limitPriceAmount: orderType === 'LIMIT' ? Number(limitPrice) : undefined,
      currency: currency || snapshot?.info?.currency || 'USD',
    }
    setSubmitting(true)
    createOrder(payload)
      .then(res => {
        setNotice(`주문이 접수되었습니다. 주문 ID: ${res?.id || '-'}`)
        fetchOrders()
        refreshAccounts()
      })
      .catch(err => {
        setOrderError(err.response?.data || err.message)
      })
      .finally(() => {
        setSubmitting(false)
      })
  }

  const handleCancelOrder = async orderId => {
    setOrdersError(null)
    setOrdersMessage(null)
    try {
      await cancelOrder(orderId)
      setOrdersMessage('주문이 취소되었습니다.')
      await fetchOrders()
      await refreshAccounts()
    } catch (e) {
      setOrdersError(e.response?.data || e.message)
    }
  }

  const orders = ordersData?.content || []

  const onSelectPrice = nextPrice => {
    setOrderType('LIMIT')
    setLimitPrice(String(truncateTo(nextPrice, currencyDecimals)))
  }

  return (
    <div className="trade-page">
      <section className="trade-hero">
        <div>
          <h2>주식 구매</h2>
          <p className="trade-subtitle">종목 가격과 동일 통화 계좌 잔고를 함께 확인하세요.</p>
        </div>
        <div className="trade-meta">
          {auth?.user ? <span>로그인됨</span> : <span>로그인이 필요합니다</span>}
          <button className="ghost-button" onClick={() => fetchSnapshot(symbol)} disabled={loading}>
            {loading ? '불러오는 중...' : '가격 조회'}
          </button>
        </div>
      </section>

      <section className="trade-search">
        <SymbolSearch value={symbol} onChange={setSymbol} onSelect={setSymbol} />
        <button className="primary-button" onClick={() => fetchSnapshot(symbol)} disabled={loading}>
          조회
        </button>
      </section>

      {error && <div className="trade-alert is-error">{JSON.stringify(error)}</div>}
      {notice && <div className="trade-alert">{notice}</div>}
      {orderError && <div className="trade-alert is-error">{JSON.stringify(orderError)}</div>}

      <section className="trade-grid">
        <TradeQuoteCard
          snapshot={snapshot}
          price={price}
          currency={currency}
          formatAmount={formatAmount}
          miniChartData={miniChartData}
        />
        <TradeAccountsCard
          currency={currency}
          totalAvailable={totalAvailable}
          accounts={accounts}
          selectedAccountNo={selectedAccountNo}
          onSelectAccount={setSelectedAccountNo}
          formatAmount={formatAmount}
        />
       
      </section>


      <section className="trade-lower">
        <section className="trade-bottom ">
          <section>
            <TradeOrderbookCard
            orderbook={orderbook}
            orderbookUpdatedAt={orderbookUpdatedAt}
            limitPrice={limitPrice}
            formatAmount={formatAmount}
            onSelectPrice={onSelectPrice}
            onRefresh={() => buildOrderbook(price, currencyDecimals)}
            canRefresh={!!price}
          />
          </section>
          <section>
          <TradeOrderFormCard
            accounts={accounts}
            selectedAccountNo={selectedAccountNo}
            onSelectAccount={setSelectedAccountNo}
            orderType={orderType}
            onChangeOrderType={setOrderType}
            quantity={quantity}
            onChangeQuantity={setQuantity}
            limitPrice={limitPrice}
            onChangeLimitPrice={setLimitPrice}
            currencyDecimals={currencyDecimals}
            estimatedCost={estimatedCost}
            selectedAvailable={selectedAvailable}
            totalAvailable={totalAvailable}
            formatAmount={formatAmount}
            onSubmit={onSubmit}
            submitting={submitting}
            canSubmit={!!snapshot}
          />
          </section>
          
          
        </section>
        <TradeOrdersPanel
          orders={orders}
          ordersLoading={ordersLoading}
          ordersError={ordersError}
          ordersMessage={ordersMessage}
          ordersSize={ordersSize}
          ordersStatus={ordersStatus}
          onChangeSize={setOrdersSize}
          onChangeStatus={setOrdersStatus}
          onRefresh={fetchOrders}
          onCancel={handleCancelOrder}
        />
       
      </section>

    </div>
  )
}

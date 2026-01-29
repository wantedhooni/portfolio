import React, { useMemo, useState, useEffect, useRef } from 'react'
import { format } from 'date-fns'
import CandleChart from '../components/CandleChart'
import SymbolSearch from '../components/SymbolSearch'
import { getHistorical, getQuote, getInfo } from '../api/api'
import { useLocation } from 'react-router-dom'

export default function Chart() {
  const location = useLocation()
  const params = new URLSearchParams(location.search)
  const initial = params.get('symbol') || 'AAPL'
  const [symbol, setSymbol] = useState(initial)
  const [data, setData] = useState([])
  const [quote, setQuote] = useState(null)
  const [info, setInfo] = useState(null)
  const [loading, setLoading] = useState(false)
  const isFetchingRef = useRef(false)
  const earliestTimeRef = useRef(null)
  const lastFetchRef = useRef(null)

  const toNumber = value => {
    if (value === null || value === undefined) return null
    const num = typeof value === 'number' ? value : Number(String(value).replace(/,/g, ''))
    return Number.isFinite(num) ? num : null
  }

  const formatNumber = (value, fractionDigits = 2) => {
    if (value === null || value === undefined) return null
    if (typeof value === 'number') {
      return value.toLocaleString('en-US', { maximumFractionDigits: fractionDigits })
    }
    const num = toNumber(value)
    if (num === null) return String(value)
    return num.toLocaleString('en-US', { maximumFractionDigits: fractionDigits })
  }

  const mergeSeriesData = (existing, incoming) => {
    if (!existing.length) return incoming
    if (!incoming.length) return existing
    const map = new Map()
    existing.forEach(item => map.set(item.time, item))
    incoming.forEach(item => map.set(item.time, item))
    return Array.from(map.values()).sort((a, b) => new Date(a.time) - new Date(b.time))
  }

  const fetchHistorical = async ({ startDate, endDate, mode = 'replace', trackLoading = true, targetSymbol } = {}) => {
    if (!startDate || !endDate) return
    if (isFetchingRef.current) return
    isFetchingRef.current = true
    if (trackLoading) setLoading(true)
    try {
      const start = format(startDate, 'yyyy-MM-dd')
      const end = format(endDate, 'yyyy-MM-dd')
      const resp = await getHistorical(targetSymbol || symbol, start, end, '1d')
      // convert to lightweight-charts format: { time, open, high, low, close }
      const prices = resp.prices.map(p => ({ time: p.date, open: p.open, high: p.high, low: p.low, close: p.close, volume: p.volume }))
      const sortedPrices = prices.sort((a, b) => new Date(a.time) - new Date(b.time))
      setData(prev => (mode === 'replace' ? sortedPrices : mergeSeriesData(prev, sortedPrices)))
    } catch (e) {
      console.error(e)
      if (mode === 'replace') setData([])
    } finally {
      if (trackLoading) setLoading(false)
      isFetchingRef.current = false
    }
  }

  const fetchQuote = async (targetSymbol) => {
    try {
      const q = await getQuote(targetSymbol || symbol)
      setQuote(q)
    } catch (e) {
      setQuote(null)
    }

    try {
      const inf = await getInfo(targetSymbol || symbol)
      setInfo(inf)
    } catch (e) {
      setInfo(null)
    }
  }

  const onSearch = async (targetSymbol) => {
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(endDate.getDate() - 30)
    await fetchQuote(targetSymbol)
    await fetchHistorical({ startDate, endDate, mode: 'replace', trackLoading: true, targetSymbol })
  }

  useEffect(() => {
    // load initial symbol on mount
    onSearch()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    if (data.length) {
      earliestTimeRef.current = data[0].time
    }
  }, [data])

  const toDate = value => {
    if (!value) return null
    if (typeof value === 'number') return new Date(value * 1000)
    if (typeof value === 'string') return new Date(value)
    if (typeof value === 'object' && value.year && value.month && value.day) {
      return new Date(Date.UTC(value.year, value.month - 1, value.day))
    }
    return null
  }

  const handleVisibleRangeChange = range => {
    if (!range || isFetchingRef.current) return
    const fromDate = toDate(range.from)
    const earliest = toDate(earliestTimeRef.current)
    if (!fromDate || !earliest) return
    const threshold = new Date(earliest)
    threshold.setDate(threshold.getDate() + 4)
    if (fromDate > threshold) return
    const endDate = new Date(earliest)
    endDate.setDate(endDate.getDate() - 1)
    const startDate = new Date(earliest)
    startDate.setDate(startDate.getDate() - 30)
    if (endDate < startDate) return
    const key = `${format(startDate, 'yyyy-MM-dd')}_${format(endDate, 'yyyy-MM-dd')}_${symbol}`
    if (lastFetchRef.current === key) return
    lastFetchRef.current = key
    fetchHistorical({ startDate, endDate, mode: 'merge', trackLoading: false })
  }

  const {
    latestPrice,
    changeValue,
    changePercent,
    dayRange,
    stats,
    summary,
    marketNote,
  } = useMemo(() => {
    const current = toNumber(quote?.current_price ?? info?.current_price)
    const previous = toNumber(quote?.previous_close ?? quote?.prev_close ?? info?.previous_close)
    const change = current !== null && previous !== null ? current - previous : toNumber(quote?.change)
    const percent = change !== null && previous ? (change / previous) * 100 : toNumber(quote?.change_percent)
    const dayLow = quote?.day_low ?? info?.day_low
    const dayHigh = quote?.day_high ?? info?.day_high
    const hasRange = dayLow !== null && dayLow !== undefined && dayHigh !== null && dayHigh !== undefined
    const range = hasRange ? `${formatNumber(dayLow)} - ${formatNumber(dayHigh)}` : null
    const noteRaw = quote?.timestamp || quote?.as_of || quote?.market_time || ''
    const note = noteRaw ? String(noteRaw) : ''
    const statsList = [
      { label: 'Previous Close', value: previous ?? quote?.previous_close },
      { label: 'Day Range', value: range },
      { label: 'Volume', value: quote?.volume ?? info?.volume },
      { label: 'Market Cap', value: info?.market_cap },
      { label: 'Dividend Yield', value: info?.dividend_yield },
      { label: 'PE Ratio (TTM)', value: info?.trailing_pe },
      { label: 'Beta (5Y)', value: info?.beta },
      { label: 'Exchange', value: info?.exchange },
    ]
    return {
      latestPrice: current,
      changeValue: change,
      changePercent: percent,
      dayRange: range,
      stats: statsList,
      summary: info?.description,
      marketNote: note,
    }
  }, [info, quote])

  const displayName = info?.short_name || info?.long_name || symbol
  const displaySymbol = info?.symbol || symbol
  const changeClass = changeValue !== null && changeValue >= 0 ? 'is-up' : 'is-down'
  const changePercentDisplay = changePercent !== null ? formatNumber(changePercent, 2) : '--'

  return (
    <div className="market-page">
      <section className="market-actions">
        <SymbolSearch value={symbol} onChange={setSymbol} onSelect={(s) => { setSymbol(s); onSearch(s) }} />
        <button className="primary-button" onClick={() => onSearch()} disabled={loading}>
          {loading ? '로딩...' : '조회'}
        </button>
      </section>

      <section className="market-hero">
        <div>
          <div className="market-hero__title">
            <h2>{displayName}</h2>
            <span className="market-hero__ticker">{displaySymbol}</span>
          </div>
          <p className="market-hero__sub">{info?.exchange || info?.sector || 'Global Equity'}</p>
        </div>
        <div className="market-hero__actions">
          <button className="ghost-button">Follow</button>
        </div>
      </section>

      <section className="market-price-strip">
        <div className="market-price-main">
          <span className="market-price-value">{formatNumber(latestPrice) ?? '--'}</span>
          {changeValue !== null && (
            <span className={`market-price-change ${changeClass}`}>
              {changeValue >= 0 ? '+' : ''}{formatNumber(changeValue)} ({changePercentDisplay}%)
            </span>
          )}
        </div>
        <div className="market-price-meta">
          {marketNote ? `As of ${marketNote}.` : 'As of market open.'}
        </div>
      </section>

      

      <section className="chart-card">

        {/* <div className="chart-toolbar">
          <div className="chart-range">
            {['1D', '5D', '1M', '6M', 'YTD', '1Y', '5Y', 'All'].map((range, index) => (
              <button key={range} className={`chip-button ${index === 0 ? 'is-active' : ''}`}>
                {range}
              </button>
            ))}
          </div>
          <div className="chart-tools">
            <button className="chip-button">Key Events</button>
            <button className="chip-button">Mountain</button>
            <button className="chip-button">Settings</button>
          </div>
        </div> */}
        <CandleChart data={data} height={460} onVisibleRangeChange={handleVisibleRangeChange} />
        {loading && <div className="chart-loading">Loading chart...</div>}
      </section>

      <section className="market-stats-grid">
        {stats.map(item => (
          <div key={item.label} className="stat-card">
            <span className="stat-label">{item.label}</span>
            <span className="stat-value">
              {item.value === null || item.value === undefined || item.value === '' ? '--' : formatNumber(item.value)}
            </span>
          </div>
        ))}
      </section>

      <section className="overview-card">
        <div className="overview-main">
          <h3>{displayName} Overview</h3>
          <p>{summary || 'No description available for this symbol yet.'}</p>
        </div>
        <div className="overview-aside">
          <div className="overview-item">
            <span className="stat-label">CEO</span>
            <span className="stat-value">{info?.ceo || '--'}</span>
          </div>
          <div className="overview-item">
            <span className="stat-label">Website</span>
            {info?.website ? (
              <a href={info.website} target="_blank" rel="noreferrer" className="overview-link">
                {info.website}
              </a>
            ) : (
              <span className="stat-value">--</span>
            )}
          </div>
          <div className="overview-item">
            <span className="stat-label">Day Range</span>
            <span className="stat-value">{dayRange || '--'}</span>
          </div>
        </div>
      </section>
    </div>
  )
}

import React, { useMemo, useState, useEffect, useRef } from 'react'
import { format } from 'date-fns'
import CandleChart from '../components/CandleChart'
import SymbolSearch from '../components/SymbolSearch'
import { getHistorical, getQuote, getInfo } from '../api/api'
import { Link, useLocation } from 'react-router-dom'
import { formatApiError } from '../utils/format'

const QUICK_SYMBOLS = ['AAPL', 'MSFT', 'NVDA', 'TSLA', 'AMZN', 'META']

export default function Chart() {
  const location = useLocation()
  const params = new URLSearchParams(location.search)
  const initial = params.get('symbol') || 'AAPL'
  const [symbol, setSymbol] = useState(initial)
  const [data, setData] = useState([])
  const [quote, setQuote] = useState(null)
  const [info, setInfo] = useState(null)
  const [loading, setLoading] = useState(true)
  const [error, setError] = useState(null)
  const [prependShift, setPrependShift] = useState(0)
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
      let nextShift = 0
      setData(prev => {
        if (mode === 'replace') return sortedPrices
        const prevTimes = new Set(prev.map(item => item.time))
        nextShift = sortedPrices.filter(item => !prevTimes.has(item.time)).length
        return mergeSeriesData(prev, sortedPrices)
      })
      if (mode === 'merge' && nextShift > 0) {
        setPrependShift(prev => prev + nextShift)
      }
      return sortedPrices
    } catch (e) {
      console.error(e)
      if (mode === 'replace') setData([])
      throw e
    } finally {
      if (trackLoading) setLoading(false)
      isFetchingRef.current = false
    }
  }

  const fetchQuote = async (targetSymbol) => {
    const [quoteResult, infoResult] = await Promise.allSettled([
      getQuote(targetSymbol || symbol),
      getInfo(targetSymbol || symbol),
    ])

    if (quoteResult.status === 'fulfilled') setQuote(quoteResult.value)
    else setQuote(null)

    if (infoResult.status === 'fulfilled') setInfo(infoResult.value)
    else setInfo(null)

    if (quoteResult.status === 'rejected' && infoResult.status === 'rejected') {
      throw quoteResult.reason || infoResult.reason || new Error('Failed to fetch quote data')
    }
  }

  const onSearch = async (targetSymbol) => {
    const nextSymbol = targetSymbol || symbol
    const endDate = new Date()
    const startDate = new Date()
    startDate.setDate(endDate.getDate() - 30)
    setLoading(true)
    setError(null)
    try {
      await Promise.all([
        fetchQuote(nextSymbol),
        fetchHistorical({ startDate, endDate, mode: 'replace', trackLoading: false, targetSymbol: nextSymbol }),
      ])
    } catch (e) {
      setError(formatApiError(e?.response?.data || e?.message, '시세 정보를 불러오지 못했습니다.'))
    } finally {
      setLoading(false)
    }
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

  const handleVisibleRangeChange = payload => {
    if (!payload || isFetchingRef.current) return
    if (payload.barsBefore === null || payload.barsBefore === undefined || payload.barsBefore > 8) return
    const earliest = earliestTimeRef.current ? new Date(earliestTimeRef.current) : null
    if (!earliest || Number.isNaN(earliest.getTime())) return
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
    const fallbackLow = data.length ? Math.min(...data.map(item => Number(item.low)).filter(Number.isFinite)) : null
    const fallbackHigh = data.length ? Math.max(...data.map(item => Number(item.high)).filter(Number.isFinite)) : null
    const resolvedLow = dayLow ?? fallbackLow
    const resolvedHigh = dayHigh ?? fallbackHigh
    const hasRange = resolvedLow !== null && resolvedLow !== undefined && resolvedHigh !== null && resolvedHigh !== undefined
    const range = hasRange ? `${formatNumber(resolvedLow)} - ${formatNumber(resolvedHigh)}` : null
    const noteRaw = quote?.timestamp || quote?.as_of || quote?.market_time || data[data.length - 1]?.time || ''
    const note = noteRaw ? String(noteRaw) : ''
    const statsList = [
      { label: '전일 종가', value: previous ?? quote?.previous_close },
      { label: '당일 범위', value: range },
      { label: '거래량', value: quote?.volume ?? info?.volume },
      { label: '시가총액', value: info?.market_cap },
      { label: '배당수익률', value: info?.dividend_yield },
      { label: 'PER', value: info?.trailing_pe },
      { label: '베타', value: info?.beta },
      { label: '거래소', value: info?.exchange },
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
  }, [data, info, quote])

  const displayName = info?.short_name || info?.long_name || symbol
  const displaySymbol = info?.symbol || symbol
  const changeClass = changeValue !== null && changeValue >= 0 ? 'is-up' : 'is-down'
  const changePercentDisplay = changePercent !== null ? formatNumber(changePercent, 2) : '--'

  return (
    <div className="market-page">
      <section className="market-overview-card">
        <div className="market-overview-card__main">
          <span className="intro-eyebrow">시장</span>
          <h2>{displayName} 시세 확인</h2>
          <p className="market-summary__text">
            종목을 찾고 현재가와 차트만 바로 확인할 수 있습니다.
          </p>
          <div className="market-actions market-actions--embedded">
            <SymbolSearch value={symbol} onChange={setSymbol} onSelect={(s) => { setSymbol(s); onSearch(s) }} />
            <button className="primary-button" onClick={() => onSearch()} disabled={loading}>
              {loading ? '불러오는 중...' : '시세 조회'}
            </button>
          </div>
        </div>
        <div className="market-overview-card__side">
          <Link to={`/trade?symbol=${encodeURIComponent(displaySymbol)}`} className="primary-button">
            주문으로 이동
          </Link>
        </div>
      </section>

      {error ? <div className="trade-alert is-error">{error}</div> : null}

      <section className="quick-symbols">
        <span className="quick-symbols__label">자주 보는 종목</span>
        <div className="quick-symbols__chips">
          {QUICK_SYMBOLS.map(item => (
            <button
              key={item}
              type="button"
              className={`ghost-button ${displaySymbol === item ? 'quick-symbols__chip--active' : ''}`}
              onClick={() => {
                setSymbol(item)
                onSearch(item)
              }}
            >
              {item}
            </button>
          ))}
        </div>
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
          <span className="market-hero__hint">왼쪽으로 이동하면 이전 데이터가 이어집니다.</span>
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
          {marketNote ? `기준 시각 ${marketNote}` : '시장 기준 시각 정보를 기다리는 중입니다.'}
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
        <CandleChart
          data={data}
          height={460}
          onVisibleRangeChange={handleVisibleRangeChange}
          fitContentKey={displaySymbol}
          prependShift={prependShift}
        />
        {loading && <div className="chart-loading">차트 데이터를 불러오는 중입니다.</div>}
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
          <h3>{displayName} 개요</h3>
          <p>{summary || '아직 제공된 종목 설명이 없습니다.'}</p>
        </div>
        <div className="overview-aside">
          <div className="overview-item">
            <span className="stat-label">CEO</span>
            <span className="stat-value">{info?.ceo || '--'}</span>
          </div>
          <div className="overview-item">
            <span className="stat-label">웹사이트</span>
            {info?.website ? (
              <a href={info.website} target="_blank" rel="noreferrer" className="overview-link">
                {info.website}
              </a>
            ) : (
              <span className="stat-value">--</span>
            )}
          </div>
          <div className="overview-item">
            <span className="stat-label">당일 범위</span>
            <span className="stat-value">{dayRange || '--'}</span>
          </div>
        </div>
      </section>
    </div>
  )
}

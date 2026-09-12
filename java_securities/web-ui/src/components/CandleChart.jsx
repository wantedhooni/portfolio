import React, { useRef, useEffect } from 'react'
import { createChart } from 'lightweight-charts'
import { useTheme } from '../theme/ThemeProvider'

export default function CandleChart({ data, height = 420, onVisibleRangeChange, fitContentKey, prependShift = 0 }) {
  const { theme } = useTheme()
  const ref = useRef()
  const chartRef = useRef()
  const seriesRef = useRef()
  const tooltipRef = useRef()
  const volumeRef = useRef()
  const maRef = useRef()
  const rangeChangeRef = useRef(onVisibleRangeChange)
  const lastFitKeyRef = useRef(null)
  const lastAppliedShiftRef = useRef(0)

  useEffect(() => {
    rangeChangeRef.current = onVisibleRangeChange
  }, [onVisibleRangeChange])

  useEffect(() => {
    if (!ref.current) return

    const palette = theme === 'dark'
      ? {
          background: '#151922',
          text: '#d5dbe3',
          grid: 'rgba(148,163,184,0.12)',
          crosshair: 'rgba(203,213,225,0.35)',
          tooltipBg: 'rgba(17, 24, 39, 0.94)',
          tooltipText: '#f8fafc',
        }
      : {
          background: '#ffffff',
          text: '#4e5968',
          grid: 'rgba(229,232,235,0.9)',
          crosshair: 'rgba(107,118,132,0.28)',
          tooltipBg: 'rgba(25, 31, 40, 0.92)',
          tooltipText: '#ffffff',
        }

    const chart = createChart(ref.current, {
      width: ref.current.clientWidth,
      height,
      layout: {
        background: { color: palette.background },
        textColor: palette.text,
      },
      grid: {
        vertLines: { color: palette.grid },
        horzLines: { color: palette.grid },
      },
      timeScale: { borderVisible: false },
      rightPriceScale: { borderVisible: false },
      crosshair: {
        horzLine: { color: palette.crosshair },
        vertLine: { color: palette.crosshair },
      },
    })
    chartRef.current = chart

    // store series in ref so other hooks can access it
    const candleSeries = chart.addCandlestickSeries({
      upColor: '#3fb950',
      downColor: '#f85149',
      wickUpColor: '#3fb950',
      wickDownColor: '#f85149',
      borderVisible: false,
    })
    seriesRef.current = candleSeries
    if (data && data.length) candleSeries.setData(data)

    // ensure the candle series leaves space at bottom for the volume pane
    try { chart.priceScale('right').applyOptions({ scaleMargins: { top: 0.12, bottom: 0.28 } }) } catch (e) {}

    // add volume histogram series in the lower scale area (separate price scale)
    const volumeSeries = chart.addHistogramSeries({
      priceFormat: { type: 'volume' },
      priceScaleId: 'volume',
      color: '#26a69a',
    })
    volumeRef.current = volumeSeries
    try {
      chart.priceScale('volume').applyOptions({
        scaleMargins: { top: 0.78, bottom: 0 },
        borderVisible: false,
        visible: false,
      })
    } catch (e) {}
    // add moving average series (line)
    const maSeries = chart.addLineSeries({ color: '#f59e0b', lineWidth: 2 })
    maRef.current = maSeries

    // tooltip element
    const tip = document.createElement('div')
    tip.className = 'chart-tooltip'
    tip.style.cssText = `position:absolute;display:none;padding:10px 12px;border-radius:12px;background:${palette.tooltipBg};color:${palette.tooltipText};font-size:12px;pointer-events:none;box-shadow:0 10px 28px rgba(15,23,42,0.18);white-space:pre-line;z-index:5;`
    ref.current.appendChild(tip)
    tooltipRef.current = tip

    const handleResize = () => chart.applyOptions({ width: ref.current.clientWidth })
    window.addEventListener('resize', handleResize)

    // create a named handler so we can unsubscribe correctly
    const handler = param => {
      if (!param || !param.time) {
        tip.style.display = 'none'
        return
      }
      if (!param.seriesPrices || typeof param.seriesPrices.get !== 'function') {
        tip.style.display = 'none'
        return
      }
      const series = seriesRef.current
      if (!series) return
      const price = param.seriesPrices.get(series)
      if (!price) return
      // ensure point exists
      if (!param.point) return
      tip.style.display = 'block'
      tip.innerText = `시간: ${param.time}\n시가: ${price.open}\n종가: ${price.close}`
      const nextLeft = Math.min(param.point.x + 12, Math.max(0, ref.current.clientWidth - 160))
      const nextTop = Math.max(12, param.point.y - 56)
      tip.style.left = `${nextLeft}px`
      tip.style.top = `${nextTop}px`
    }

    chart.subscribeCrosshairMove(handler)
    const timeScale = chart.timeScale()
    const handleRangeChange = logicalRange => {
      if (!rangeChangeRef.current || !logicalRange || !seriesRef.current) return
      const barsInfo = seriesRef.current.barsInLogicalRange(logicalRange)
      rangeChangeRef.current({
        logicalRange,
        barsBefore: barsInfo?.barsBefore ?? null,
        barsAfter: barsInfo?.barsAfter ?? null,
      })
    }
    timeScale.subscribeVisibleLogicalRangeChange(handleRangeChange)

    return () => {
      window.removeEventListener('resize', handleResize)
      // unsubscribe using the handler reference
      try {
        chart.unsubscribeCrosshairMove(handler)
      } catch (e) {
        // ignore if unsubscribe not supported
      }
      try {
        timeScale.unsubscribeVisibleLogicalRangeChange(handleRangeChange)
      } catch (e) {
        // ignore if unsubscribe not supported
      }
      if (tooltipRef.current && tooltipRef.current.parentNode) {
        tooltipRef.current.parentNode.removeChild(tooltipRef.current)
      }
      chart.remove()
    }
  }, [height, theme])

  useEffect(() => {
    if (!seriesRef.current) return
    if (data) seriesRef.current.setData(data)
    // update volume series
    if (volumeRef.current && data) {
      const vols = data.map(d => ({ time: d.time, value: d.volume || 0, color: d.close >= d.open ? '#26a69a' : '#ef5350' }))
      // histogram expects { time, value }
      volumeRef.current.setData(vols)
    }
    // update MA(20)
    if (maRef.current && data) {
      const period = 20
      const ma = []
      for (let i = 0; i < data.length; i++) {
        if (i + 1 >= period) {
          let sum = 0
          for (let j = i + 1 - period; j <= i; j++) sum += data[j].close
          ma.push({ time: data[i].time, value: +(sum / period).toFixed(4) })
        }
      }
      maRef.current.setData(ma)
    }
    if (chartRef.current && data?.length && fitContentKey && lastFitKeyRef.current !== fitContentKey) {
      chartRef.current.timeScale().fitContent()
      lastFitKeyRef.current = fitContentKey
    }
  }, [data, fitContentKey])

  useEffect(() => {
    if (!chartRef.current) return
    const delta = prependShift - lastAppliedShiftRef.current
    if (!delta) return
    const timeScale = chartRef.current.timeScale()
    const currentRange = timeScale.getVisibleLogicalRange()
    if (!currentRange) return
    timeScale.setVisibleLogicalRange({
      from: currentRange.from + delta,
      to: currentRange.to + delta,
    })
    lastAppliedShiftRef.current = prependShift
  }, [prependShift])

  return <div ref={ref} className="chart-wrapper" style={{ position: 'relative' }} />
}

import React, { useRef, useEffect } from 'react'
import { createChart } from 'lightweight-charts'

export default function CandleChart({ data, height = 420, onVisibleRangeChange }) {
  const ref = useRef()
  const chartRef = useRef()
  const seriesRef = useRef()
  const tooltipRef = useRef()
  const volumeRef = useRef()
  const maRef = useRef()
  const rangeChangeRef = useRef(onVisibleRangeChange)

  useEffect(() => {
    rangeChangeRef.current = onVisibleRangeChange
  }, [onVisibleRangeChange])

  useEffect(() => {
    if (!ref.current) return

    const chart = createChart(ref.current, {
      width: ref.current.clientWidth,
      height,
      layout: { backgroundColor: '#10151c', textColor: '#cbd5e1' },
      grid: {
        vertLines: { color: 'rgba(148,163,184,0.15)' },
        horzLines: { color: 'rgba(148,163,184,0.15)' },
      },
      timeScale: { borderVisible: false },
      rightPriceScale: { borderVisible: false },
      crosshair: {
        horzLine: { color: 'rgba(203,213,225,0.35)' },
        vertLine: { color: 'rgba(203,213,225,0.35)' },
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
    tip.style.cssText = 'position:absolute;display:none;padding:6px;border-radius:6px;background:#111;color:#fff;font-size:12px;pointer-events:none;'
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
      tip.style.left = `${param.point.x + 10}px`
      tip.style.top = `${param.point.y + 10}px`
    }

    chart.subscribeCrosshairMove(handler)
    const timeScale = chart.timeScale()
    const handleRangeChange = range => {
      if (rangeChangeRef.current) rangeChangeRef.current(range)
    }
    timeScale.subscribeVisibleTimeRangeChange(handleRangeChange)

    return () => {
      window.removeEventListener('resize', handleResize)
      // unsubscribe using the handler reference
      try {
        chart.unsubscribeCrosshairMove(handler)
      } catch (e) {
        // ignore if unsubscribe not supported
      }
      try {
        timeScale.unsubscribeVisibleTimeRangeChange(handleRangeChange)
      } catch (e) {
        // ignore if unsubscribe not supported
      }
      chart.remove()
    }
  }, [])

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
  }, [data])

  return <div ref={ref} className="chart-wrapper" style={{ position: 'relative' }} />
}

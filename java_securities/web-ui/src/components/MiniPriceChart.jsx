import React, { useEffect, useRef } from 'react'
import { createChart } from 'lightweight-charts'

export default function MiniPriceChart({ data = [], height = 80 }) {
  const containerRef = useRef(null)
  const chartRef = useRef(null)
  const seriesRef = useRef(null)

  useEffect(() => {
    if (!containerRef.current) return
    const chart = createChart(containerRef.current, {
      width: containerRef.current.clientWidth,
      height,
      layout: {
        background: { color: 'transparent' },
        textColor: '#cbd5e1',
      },
      grid: {
        vertLines: { color: 'rgba(148,163,184,0.1)' },
        horzLines: { color: 'rgba(148,163,184,0.1)' },
      },
      timeScale: {
        visible: true,
        borderVisible: false,
        timeVisible: true,
        secondsVisible: false,
      },
      rightPriceScale: {
        visible: true,
        borderVisible: false,
        scaleMargins: { top: 0.2, bottom: 0.2 },
      },
      crosshair: {
        horzLine: { visible: false },
        vertLine: { visible: false },
      },
    })
    chartRef.current = chart
    const series = chart.addLineSeries({
      color: 'rgba(59, 130, 246, 0.9)',
      lineWidth: 2,
      priceLineVisible: true,
    })
    seriesRef.current = series
    if (data.length) series.setData(data)

    const handleResize = () => {
      if (!containerRef.current) return
      chart.applyOptions({ width: containerRef.current.clientWidth })
    }
    window.addEventListener('resize', handleResize)

    return () => {
      window.removeEventListener('resize', handleResize)
      chart.remove()
    }
  }, [height])

  useEffect(() => {
    if (!seriesRef.current) return
    seriesRef.current.setData(data)
  }, [data])

  return <div ref={containerRef} style={{ width: '100%', height }} />
}

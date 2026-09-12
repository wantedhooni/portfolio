import React from 'react'

export default function SymbolInfo({ info }) {
  if (!info) return null

  return (
    <div style={{ marginTop: 8 }}>
      <h3 style={{ margin: 0 }}>{info.short_name || info.symbol} <small style={{ fontSize: 12, color: '#666' }}>({info.symbol})</small></h3>
      <div style={{ marginTop: 6, display: 'flex', gap: 12, flexWrap: 'wrap' }}>
        <div><strong>현재가:</strong> {info.current_price ?? '-'}</div>
        <div><strong>시가총액:</strong> {info.market_cap ?? '-'}</div>
        <div><strong>배당수익률:</strong> {info.dividend_yield ?? '-'}</div>
        <div><strong>PER:</strong> {info.trailing_pe ?? '-'}</div>
        <div><strong>Beta:</strong> {info.beta ?? '-'}</div>
      </div>
      {info.ceo && <div style={{ marginTop: 8 }}><strong>CEO:</strong> {info.ceo}</div>}
      {info.exchange && <div style={{ marginTop: 6 }}><strong>Exchange:</strong> {info.exchange}</div>}
      {info.website && (
        <div style={{ marginTop: 6 }}>
          <strong>Website:</strong> <a href={info.website} target="_blank" rel="noreferrer">{info.website}</a>
        </div>
      )}
      {info.description && (
        <div style={{ marginTop: 8, color: '#333' }}>{info.description}</div>
      )}
    </div>
  )
}

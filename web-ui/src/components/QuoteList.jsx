import React from 'react'

export default function QuoteList({ quotes = {}, onSelect = () => {}, onSnapshot = () => {} }) {
  const keys = Object.keys(quotes)
  if (!keys.length) return <div>데이터 없음</div>

  return (
    <table style={{ width: '100%', borderCollapse: 'collapse' }}>
      <thead>
        <tr>
          <th style={{ textAlign: 'left', padding: 8 }}>Symbol</th>
          <th style={{ textAlign: 'right', padding: 8 }}>Price</th>
          <th style={{ textAlign: 'right', padding: 8 }}>Change</th>
          <th style={{ textAlign: 'center', padding: 8 }}>Actions</th>
        </tr>
      </thead>
      <tbody>
        {keys.map(k => {
          const item = quotes[k]
          return (
            <tr key={k} style={{ borderTop: '1px solid #eee' }}>
              <td style={{ padding: 8 }}>{k}</td>
              <td style={{ padding: 8, textAlign: 'right' }}>{item?.current_price ?? '-'}</td>
              <td style={{ padding: 8, textAlign: 'right' }}>{item && item.previous_close ? ((item.current_price - item.previous_close).toFixed(2)) : '-'}</td>
              <td style={{ padding: 8, textAlign: 'center' }}>
                <button onClick={() => onSelect(k)} style={{ marginRight: 8 }}>차트</button>
                <button onClick={() => onSnapshot(k)}>스냅샷</button>
              </td>
            </tr>
          )
        })}
      </tbody>
    </table>
  )
}

import React from 'react'

export default function TradeOrderbookCard({
  orderbook,
  orderbookUpdatedAt,
  limitPrice,
  formatAmount,
  onSelectPrice,
  onRefresh,
  canRefresh,
}) {
  return (
    <div className="trade-card trade-book">
      <div className="trade-book__header">
        <div>
          <h3>호가</h3>
          <p>가격을 선택하면 지정가에 반영됩니다.</p>
        </div>
        <div className="trade-book__actions">
          <span>{orderbookUpdatedAt ? orderbookUpdatedAt.toLocaleTimeString() : '-'}</span>
          <button type="button" className="ghost-button" onClick={onRefresh} disabled={!canRefresh}>
            갱신
          </button>
        </div>
      </div>
      {orderbook.asks.length === 0 ? (
        <div className="trade-empty">호가 데이터를 불러오세요.</div>
      ) : (
        <div className="trade-book__grid">
          <div className="trade-book__side">
            <div className="trade-book__title">매도</div>
            {orderbook.asks.map((row, idx) => (
              <button
                key={`ask-${idx}`}
                type="button"
                className={`trade-book__row is-ask ${Number(limitPrice) === row.price ? 'is-selected' : ''}`}
                onClick={() => onSelectPrice(row.price)}
              >
                <span>{formatAmount(row.price)}</span>
                <strong>{row.qty}</strong>
              </button>
            ))}
          </div>
          <div className="trade-book__side">
            <div className="trade-book__title">매수</div>
            {orderbook.bids.map((row, idx) => (
              <button
                key={`bid-${idx}`}
                type="button"
                className={`trade-book__row is-bid ${Number(limitPrice) === row.price ? 'is-selected' : ''}`}
                onClick={() => onSelectPrice(row.price)}
              >
                <span>{formatAmount(row.price)}</span>
                <strong>{row.qty}</strong>
              </button>
            ))}
          </div>
        </div>
      )}
    </div>
  )
}

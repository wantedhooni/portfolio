import React from 'react'
import MiniPriceChart from '../MiniPriceChart'

export default function TradeQuoteCard({ snapshot, price, currency, formatAmount, miniChartData }) {
  return (
    <div className="trade-card">
      <h3>현재 시세</h3>
      {snapshot ? (
        <div className="trade-quote">
          <div>
            <span>심볼</span>
            <strong>{snapshot.symbol}</strong>
          </div>
          <div>
            <span>현재가</span>
            <strong>{formatAmount(price)}</strong>
          </div>
          <div>
            <span>통화</span>
            <strong>{currency || '-'}</strong>
          </div>
          <div>
            <span>이전 종가</span>
            <strong>{snapshot?.quote?.previous_close != null ? formatAmount(snapshot.quote.previous_close) : '-'}</strong>
          </div>
          <div className="trade-mini-chart">
            <MiniPriceChart data={miniChartData} height={80} />
          </div>
        </div>
      ) : (
        <div className="trade-empty">종목을 조회하세요.</div>
      )}
    </div>
  )
}

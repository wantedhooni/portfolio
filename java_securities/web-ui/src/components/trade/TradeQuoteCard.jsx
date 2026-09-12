import React from 'react'
import MiniPriceChart from '../MiniPriceChart'

export default function TradeQuoteCard({ snapshot, price, currency, formatAmount, miniChartData }) {
  const previousClose = snapshot?.quote?.previous_close
  const change = previousClose != null && price != null ? Number(price) - Number(previousClose) : null
  const directionClass = change !== null && change >= 0 ? 'is-up' : 'is-down'

  return (
    <div className="trade-card">
      <div className="trade-card__title-row">
        <div>
          <h3>현재 시세</h3>
          <p className="trade-card__caption">현재가와 전일 대비만 먼저 확인할 수 있게 정리했습니다.</p>
        </div>
      </div>
      {snapshot ? (
        <div className="trade-quote">
          <div className="trade-quote__headline">
            <span className="trade-symbol-badge">{snapshot.symbol}</span>
            <strong className="trade-quote__price">{formatAmount(price)}</strong>
          </div>
          <div>
            <span>심볼</span>
            <strong>{snapshot.symbol}</strong>
          </div>
          <div>
            <span>현재가</span>
            <strong>{formatAmount(price)}</strong>
          </div>
          <div>
            <span>전일 대비</span>
            <strong className={`trade-delta ${directionClass}`}>
              {change !== null ? `${change >= 0 ? '+' : ''}${formatAmount(change)}` : '-'}
            </strong>
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

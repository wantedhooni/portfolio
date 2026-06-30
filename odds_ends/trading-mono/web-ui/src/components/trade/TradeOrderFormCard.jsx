import React from 'react'

export default function TradeOrderFormCard({
  accounts,
  selectedAccountNo,
  onSelectAccount,
  orderType,
  onChangeOrderType,
  quantity,
  onChangeQuantity,
  limitPrice,
  onChangeLimitPrice,
  currencyDecimals,
  estimatedCost,
  selectedAvailable,
  totalAvailable,
  formatAmount,
  onSubmit,
  submitting,
  canSubmit,
}) {
  return (
    <div className="trade-card trade-order">
      <div className="trade-order__header">
        <h3>구매 주문</h3>
      </div>
      <form onSubmit={onSubmit} className="trade-form">
        <div className="trade-field">
          <label>구매 계좌</label>
          <select
            value={selectedAccountNo}
            onChange={e => onSelectAccount(e.target.value)}
            disabled={accounts.length === 0}
          >
            {accounts.length === 0 ? (
              <option value="">계좌 없음</option>
            ) : (
              accounts.map(account => (
                <option key={account.accountNo} value={account.accountNo}>
                  {account.accountNo} · {account.type} · {formatAmount(account.availableCash)}
                </option>
              ))
            )}
          </select>
        </div>
        <div className="trade-field">
          <label>주문 유형</label>
          <select value={orderType} onChange={e => onChangeOrderType(e.target.value)}>
            <option value="MARKET">MARKET</option>
            <option value="LIMIT">LIMIT</option>
          </select>
        </div>
        <div className="trade-field">
          <label>수량</label>
          <input
            type="number"
            min="0"
            step="0.000001"
            value={quantity}
            onChange={e => onChangeQuantity(e.target.value)}
            placeholder="0"
          />
        </div>
        {orderType === 'LIMIT' && (
          <div className="trade-field">
            <label>지정가</label>
            <input
              type="number"
              min="0"
              step={currencyDecimals === 0 ? '1' : String(Math.pow(10, -currencyDecimals))}
              value={limitPrice}
              onChange={e => onChangeLimitPrice(e.target.value)}
              placeholder="0"
            />
          </div>
        )}
        <div className="trade-summary">
          <div>
            <span>예상 주문 금액</span>
            <strong>{formatAmount(estimatedCost)}</strong>
          </div>
          <div>
            <span>선택 계좌 사용 가능</span>
            <strong>{selectedAccountNo ? formatAmount(selectedAvailable) : '-'}</strong>
          </div>
          <div>
            <span>사용 가능 합계</span>
            <strong>{formatAmount(totalAvailable)}</strong>
          </div>
        </div>
        <div className="trade-actions">
          <button type="submit" className="primary-button" disabled={!canSubmit || submitting}>
            {submitting ? '주문 중...' : '구매'}
          </button>
        </div>
      </form>
    </div>
  )
}

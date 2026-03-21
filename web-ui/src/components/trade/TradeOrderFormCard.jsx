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
  const quickQuantities = [1, 5, 10, 20]
  const hasAccounts = accounts.length > 0

  return (
    <div className="trade-card trade-order">
      <div className="trade-card__title-row">
        <div>
          <h3>구매 주문</h3>
          <p className="trade-card__caption">계좌와 수량만 확인한 뒤 바로 주문할 수 있게 단순하게 정리했습니다.</p>
        </div>
      </div>
      {!hasAccounts && (
        <div className="trade-alert is-error">
          주문 통화와 일치하는 계좌가 없어 주문을 진행할 수 없습니다. 계좌를 만들거나 다른 종목을 선택하세요.
        </div>
      )}
      <form onSubmit={onSubmit} className="trade-form">
        <div className="trade-order__overview">
          <div className="trade-order__metric">
            <span>예상 주문 금액</span>
            <strong>{formatAmount(estimatedCost)}</strong>
          </div>
          <div className="trade-order__metric">
            <span>선택 계좌 사용 가능</span>
            <strong>{selectedAccountNo ? formatAmount(selectedAvailable) : '-'}</strong>
          </div>
          <div className="trade-order__metric">
            <span>주문 후 예상 잔액</span>
            <strong>{selectedAccountNo ? formatAmount(selectedAvailable - estimatedCost) : '-'}</strong>
          </div>
        </div>

        <div className="trade-order__grid">
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
        </div>

        <div className="trade-order__grid">
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
          <div className="trade-field">
            <label>{orderType === 'LIMIT' ? '지정가' : '현재가 기준'}</label>
            {orderType === 'LIMIT' ? (
              <>
                <input
                  type="number"
                  min="0"
                  step={currencyDecimals === 0 ? '1' : String(Math.pow(10, -currencyDecimals))}
                  value={limitPrice}
                  onChange={e => onChangeLimitPrice(e.target.value)}
                  placeholder="0"
                />
                <p className="trade-field__hint">호가 패널 가격을 누르면 지정가가 자동 반영됩니다.</p>
              </>
            ) : (
              <div className="trade-order__static-value">{formatAmount(estimatedCost ? estimatedCost / ((Number(quantity) || 1)) : 0)}</div>
            )}
          </div>
        </div>

        <div className="trade-order__shortcut-row">
          <span className="trade-order__shortcut-label">빠른 수량</span>
          <div className="trade-quick-actions">
            {quickQuantities.map(value => (
              <button
                key={value}
                type="button"
                className="ghost-button"
                onClick={() => onChangeQuantity(String(value))}
              >
                {value}주
              </button>
            ))}
          </div>
        </div>

        <div className="trade-summary trade-summary--compact">
          <div>
            <span>사용 가능 합계</span>
            <strong>{formatAmount(totalAvailable)}</strong>
          </div>
          <div>
            <span>현재 입력 상태</span>
            <strong>{orderType === 'LIMIT' ? '지정가 주문' : '시장가 주문'}</strong>
          </div>
        </div>
        <div className="trade-actions">
          <button type="submit" className="primary-button" disabled={!canSubmit || submitting || !hasAccounts}>
            {submitting ? '주문 중...' : '구매'}
          </button>
        </div>
      </form>
    </div>
  )
}

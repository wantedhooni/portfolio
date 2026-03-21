import React, { useEffect, useMemo, useState } from 'react'
import { getExchangeRates, getMyAccounts } from '../api/api'

const toNumber = (value) => {
  if (value === null || value === undefined || value === '') return null
  const num = typeof value === 'number' ? value : Number(String(value).replace(/,/g, ''))
  return Number.isFinite(num) ? num : null
}

export default function Exchange() {
  const [rateMap, setRateMap] = useState({})
  const [base, setBase] = useState('USD')
  const [dest, setDest] = useState('')
  const [amountInput, setAmountInput] = useState('1000000')
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState('')
  const [accounts, setAccounts] = useState([])
  const [accountsLoading, setAccountsLoading] = useState(false)
  const [accountsError, setAccountsError] = useState('')

  const loadRates = async () => {
    setLoading(true)
    setError('')
    try {
      const resp = await getExchangeRates()
      const raw = resp?.rate || resp?.data?.rate || {}
      setRateMap(raw || {})
      const bases = Object.keys(raw || {})
      if (bases.length && !bases.includes(base)) {
        setBase(bases[0])
      }
    } catch (e) {
      setError('환율 정보를 불러오지 못했습니다.')
    } finally {
      setLoading(false)
    }
  }

  useEffect(() => {
    loadRates()
    // eslint-disable-next-line react-hooks/exhaustive-deps
  }, [])

  useEffect(() => {
    const loadAccounts = async () => {
      setAccountsLoading(true)
      setAccountsError('')
      try {
        const data = await getMyAccounts({ statuses: ['ACTIVE'] })
        setAccounts(Array.isArray(data) ? data : [])
      } catch (e) {
        setAccountsError('계좌 정보를 불러오지 못했습니다.')
      } finally {
        setAccountsLoading(false)
      }
    }
    loadAccounts()
  }, [])

  const baseOptions = useMemo(() => Object.keys(rateMap || {}).sort(), [rateMap])
  const destOptions = useMemo(() => {
    const list = rateMap?.[base]
    return Array.isArray(list) ? list.map(item => item.dest).filter(Boolean).sort() : []
  }, [rateMap, base])

  useEffect(() => {
    if (destOptions.length && !destOptions.includes(dest)) {
      setDest(destOptions[0])
    }
    if (!destOptions.length) setDest('')
  }, [destOptions, dest])

  const rate = useMemo(() => {
    const list = rateMap?.[base]
    if (!Array.isArray(list)) return null
    const found = list.find(item => item.dest === dest)
    return toNumber(found?.rate)
  }, [rateMap, base, dest])

  const amountNum = toNumber(amountInput)
  const result = amountNum !== null && rate !== null ? amountNum * rate : null
  const fee = 0
  const total = amountNum !== null ? amountNum + fee : null
  const baseAccounts = useMemo(
    () => accounts.filter(account => account.currency === base),
    [accounts, base]
  )
  const destAccounts = useMemo(
    () => accounts.filter(account => account.currency === dest),
    [accounts, dest]
  )
  const selectedAccounts = useMemo(
    () => accounts.filter(account => account.currency === base || account.currency === dest),
    [accounts, base, dest]
  )

  const formatAmount = (value, digits = 2) => {
    if (value === null || value === undefined) return ''
    return Number(value).toLocaleString('en-US', { maximumFractionDigits: digits })
  }

  const handleAmountChange = (event) => {
    const raw = event.target.value.replace(/[^0-9.]/g, '')
    setAmountInput(raw)
  }

  const handleAmountBlur = () => {
    if (amountNum === null) return
    const formatted = Number(amountNum).toLocaleString('en-US', { maximumFractionDigits: 2 })
    setAmountInput(formatted)
  }

  return (
    <div className="exchange-shell">
      <div className="exchange-layout">
        <div className="exchange-card">
          <div className="exchange-hero">
            <div>
              <span className="intro-eyebrow">환전</span>
              <h2 className="exchange-hero__title">환전 금액과 적용 환율을 바로 확인</h2>
              <p className="exchange-hero__text">
                송금 화면처럼 보이지 않도록 환전 기준으로 다시 정리했습니다. 기준 통화, 환전 후 금액, 보유 계좌를 한 번에 확인할 수 있습니다.
              </p>
            </div>
            <div className="exchange-hero__badge">
              <span>환전 통화</span>
              <strong>{base} → {dest || '--'}</strong>
            </div>
          </div>

          <div className="exchange-quick-amounts">
            {[1000, 10000, 100000, 1000000].map(value => (
              <button
                key={value}
                type="button"
                className="ghost-button"
                onClick={() => setAmountInput(String(value))}
              >
                {value.toLocaleString('en-US')}
              </button>
            ))}
          </div>

          <div className="exchange-section">
            <div className="exchange-title">환전할 금액</div>
            <div className="exchange-input-row">
              <select value={base} onChange={e => setBase(e.target.value)} disabled={loading || !baseOptions.length}>
                {baseOptions.map(code => (
                  <option key={code} value={code}>{code}</option>
                ))}
              </select>
              <input
                type="text"
                value={amountInput}
                onChange={handleAmountChange}
                onBlur={handleAmountBlur}
                placeholder="1,000,000"
              />
              <span className="exchange-currency">{base}</span>
            </div>
          </div>

          <div className="exchange-divider" />

          <div className="exchange-section">
            <div className="exchange-title">환전 후 예상 금액</div>
            <div className="exchange-input-row">
              <select value={dest} onChange={e => setDest(e.target.value)} disabled={loading || !destOptions.length}>
                {destOptions.map(code => (
                  <option key={code} value={code}>{code}</option>
                ))}
              </select>
              <input
                type="text"
                value={result !== null ? formatAmount(result, 2) : ''}
                readOnly
                placeholder="0.00"
              />
              <span className="exchange-currency exchange-currency--accent">{dest || '--'}</span>
            </div>
          </div>

          <div className="exchange-info-card">
            <div className="exchange-info-row">
              <span>기준 환율</span>
              <strong>
                {base && dest && rate !== null
                  ? `1 ${base} = ${formatAmount(rate, 4)} ${dest}`
                  : '--'}
              </strong>
            </div>
            <div className="exchange-info-row">
              <span>기준 통화 계좌</span>
              <strong>{baseAccounts.length ? `${baseAccounts.length}개 보유` : '없음'}</strong>
            </div>
            <div className="exchange-info-row">
              <span>환전 대상 통화 계좌</span>
              <strong>{destAccounts.length ? `${destAccounts.length}개 보유` : '없음'}</strong>
            </div>
            <div className="exchange-info-row">
              <span>예상 차감 금액</span>
              <strong>{total !== null ? `${formatAmount(total, 2)} ${base}` : '--'}</strong>
            </div>
            <div className="exchange-info-row">
              <span>예상 환전 금액</span>
              <strong>{result !== null ? `${formatAmount(result, 2)} ${dest}` : '--'}</strong>
            </div>
          </div>

          {error && <div className="account-alert is-error">{error}</div>}

          <button className="exchange-action" type="button" onClick={loadRates} disabled={loading}>
            {loading ? '환율 불러오는 중...' : '환율 다시 계산'}
          </button>
          <p className="exchange-footnote">환율은 실시간 시세 기준이며 실제 환전 시점에 일부 변동될 수 있습니다.</p>
        </div>

        <aside className="exchange-side">
          <div className="exchange-side__header">
            <h3>보유 외화 계좌</h3>
            <span className="intro-muted">{accountsLoading ? '불러오는 중...' : `${accounts.length}개`}</span>
          </div>
          {selectedAccounts.length > 0 && (
            <div className="exchange-side__focus">
              <span>환전에 바로 쓸 수 있는 계좌</span>
              <strong>{selectedAccounts.length}개</strong>
            </div>
          )}
          {accountsError && <div className="account-alert is-error">{accountsError}</div>}
          <div className="exchange-accounts">
            {!accountsLoading && !accounts.length && (
              <div className="exchange-empty">표시할 계좌가 없습니다.</div>
            )}
            {accounts.map(account => (
              <div key={account.accountNo} className="exchange-account-card">
                <div>
                  <div className="exchange-account-title">{account.accountNo}</div>
                  <div className="exchange-account-meta">
                    {account.type} · {account.currency}
                  </div>
                </div>
                <div className="exchange-account-balance">
                  {formatAmount(account.availableCash ?? account.cashBalance, 2)}
                </div>
              </div>
            ))}
          </div>
        </aside>
      </div>
    </div>
  )
}

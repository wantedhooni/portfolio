import React, { useCallback, useEffect, useMemo, useState } from 'react'
import {
  createAccount,
  depositAccount,
  getMyAccounts,
  transferAccount,
  withdrawAccount,
} from '../api/api'
import { useAuth } from '../auth/AuthProvider'
import { formatAccountStatus, formatApiError } from '../utils/format'

const ACCOUNT_TYPES = ['CASH', 'MARGIN']
const ACCOUNT_STATUSES = ['ACTIVE', 'SUSPENDED', 'CLOSED']

function parseCsv(value) {
  return value
    .split(',')
    .map(v => v.trim())
    .filter(Boolean)
}

function formatMoney(value) {
  return Number(value || 0).toLocaleString('en-US', { maximumFractionDigits: 2 })
}

export default function Account() {
  const auth = useAuth()
  const [accounts, setAccounts] = useState([])
  const [loading, setLoading] = useState(false)
  const [error, setError] = useState(null)
  const [filters, setFilters] = useState({
    currencies: '',
    types: ['CASH', 'MARGIN'],
    statuses: ['ACTIVE'],
  })

  const [createForm, setCreateForm] = useState({ accountType: 'CASH', currency: 'USD' })
  const [depositForm, setDepositForm] = useState({ accountNo: '', amount: '' })
  const [withdrawForm, setWithdrawForm] = useState({ accountNo: '', amount: '' })
  const [transferForm, setTransferForm] = useState({
    fromAccountNo: '',
    toAccountNo: '',
    amount: '',
    referenceId: '',
    fromDescription: '',
    toDescription: '',
  })
  const [actionMessage, setActionMessage] = useState(null)
  const [actionError, setActionError] = useState(null)
  const activeAccounts = useMemo(
    () => accounts.filter(account => account.status === 'ACTIVE'),
    [accounts]
  )
  const totalAvailable = useMemo(
    () => accounts.reduce((sum, account) => sum + (Number(account.availableCash) || 0), 0),
    [accounts]
  )
  const accountCurrencies = useMemo(
    () => Array.from(new Set(accounts.map(account => account.currency).filter(Boolean))),
    [accounts]
  )
  const featuredAccounts = useMemo(
    () => activeAccounts.slice(0, 3),
    [activeAccounts]
  )

  const filterPayload = useMemo(() => {
    return {
      currencies: parseCsv(filters.currencies),
      types: filters.types,
      statuses: filters.statuses,
    }
  }, [filters])

  const fetchAccounts = useCallback(async () => {
    setLoading(true)
    setError(null)
    try {
      const data = await getMyAccounts(filterPayload)
      setAccounts(Array.isArray(data) ? data : [])
    } catch (e) {
      setError(formatApiError(e.response?.data || e.message, '계좌 정보를 불러오지 못했습니다.'))
    } finally {
      setLoading(false)
    }
  }, [filterPayload])

  useEffect(() => {
    fetchAccounts()
  }, [fetchAccounts])

  const handleCreate = async e => {
    e.preventDefault()
    setActionMessage(null)
    setActionError(null)
    try {
      const payload = {
        accountType: createForm.accountType,
        currency: createForm.currency.trim(),
      }
      const res = await createAccount(payload)
      setActionMessage(`계좌가 생성되었습니다. ${res?.accountNo || ''}`.trim())
      setCreateForm(prev => ({ ...prev, currency: '' }))
      await fetchAccounts()
    } catch (e) {
      setActionError(formatApiError(e.response?.data || e.message, '계좌 개설에 실패했습니다.'))
    }
  }

  const handleDeposit = async e => {
    e.preventDefault()
    setActionMessage(null)
    setActionError(null)
    try {
      const payload = {
        accountNo: depositForm.accountNo.trim(),
        amount: Number(depositForm.amount),
      }
      await depositAccount(payload)
      setActionMessage('입금이 완료되었습니다.')
      setDepositForm({ accountNo: '', amount: '' })
      await fetchAccounts()
    } catch (e) {
      setActionError(formatApiError(e.response?.data || e.message, '입금에 실패했습니다.'))
    }
  }

  const handleWithdraw = async e => {
    e.preventDefault()
    setActionMessage(null)
    setActionError(null)
    try {
      const payload = {
        accountNo: withdrawForm.accountNo.trim(),
        amount: Number(withdrawForm.amount),
      }
      await withdrawAccount(payload)
      setActionMessage('출금이 완료되었습니다.')
      setWithdrawForm({ accountNo: '', amount: '' })
      await fetchAccounts()
    } catch (e) {
      setActionError(formatApiError(e.response?.data || e.message, '출금에 실패했습니다.'))
    }
  }

  const handleTransfer = async e => {
    e.preventDefault()
    setActionMessage(null)
    setActionError(null)
    try {
      const payload = {
        fromAccountNo: transferForm.fromAccountNo.trim(),
        toAccountNo: transferForm.toAccountNo.trim(),
        amount: Number(transferForm.amount),
        referenceId: transferForm.referenceId.trim() || undefined,
        fromDescription: transferForm.fromDescription.trim() || undefined,
        toDescription: transferForm.toDescription.trim() || undefined,
      }
      await transferAccount(payload)
      setActionMessage('계좌 이체가 완료되었습니다.')
      setTransferForm({
        fromAccountNo: '',
        toAccountNo: '',
        amount: '',
        referenceId: '',
        fromDescription: '',
        toDescription: '',
      })
      await fetchAccounts()
    } catch (e) {
      setActionError(formatApiError(e.response?.data || e.message, '이체에 실패했습니다.'))
    }
  }

  const toggleFilter = (key, value) => {
    setFilters(prev => {
      const list = new Set(prev[key])
      if (list.has(value)) list.delete(value)
      else list.add(value)
      return { ...prev, [key]: Array.from(list) }
    })
  }

  return (
    <div className="account-page">
      <section className="account-overview-card">
        <div className="account-overview-card__main">
          <span className="intro-eyebrow">계좌</span>
          <h2>계좌와 잔액을 한 번에 확인합니다</h2>
          <p className="account-subtitle">계좌 현황을 먼저 보고 필요한 작업만 바로 진행할 수 있습니다.</p>
        </div>
        <div className="account-overview-card__side">
          <div className="account-overview-card__info">
            <span>로그인 상태</span>
            <strong>{auth?.user ? '정상 이용 중' : '로그인 필요'}</strong>
          </div>
          <button onClick={fetchAccounts} className="ghost-button" disabled={loading}>
            {loading ? '불러오는 중...' : '계좌 새로고침'}
          </button>
        </div>
      </section>

      <section className="account-hero">
        <div>
          <h2>내 계좌 현황</h2>
          <p className="account-subtitle">보유 계좌와 사용 가능 금액을 먼저 확인하세요.</p>
        </div>
        <div className="account-hero__meta">
          <span>{loading ? '계좌를 불러오는 중입니다.' : '최신 계좌 기준입니다.'}</span>
        </div>
      </section>

      <section className="account-summary">
        <div className="account-summary__card">
          <span>전체 계좌 수</span>
          <strong>{accounts.length}</strong>
        </div>
        <div className="account-summary__card">
          <span>활성 계좌</span>
          <strong>{activeAccounts.length}</strong>
        </div>
        <div className="account-summary__card">
          <span>사용 가능 금액 합계</span>
          <strong>{formatMoney(totalAvailable)}</strong>
        </div>
        <div className="account-summary__card">
          <span>보유 통화</span>
          <strong>{accountCurrencies.join(', ') || '-'}</strong>
        </div>
      </section>

      <section className="account-shortcuts">
        <a href="#account-list" className="account-shortcut">계좌 목록 보기</a>
        <a href="#account-create" className="account-shortcut">계좌 개설</a>
        <a href="#account-deposit" className="account-shortcut">입금</a>
        <a href="#account-transfer" className="account-shortcut">이체</a>
      </section>

      {featuredAccounts.length > 0 ? (
        <section className="account-featured">
          {featuredAccounts.map(account => (
            <article key={`featured-${account.accountNo}`} className="account-featured__item">
              <div>
                <strong>{account.accountNo}</strong>
                <p>{account.currency} · {account.type}</p>
              </div>
              <div>
                <span>사용 가능 금액</span>
                <strong>{formatMoney(account.availableCash)}</strong>
              </div>
            </article>
          ))}
        </section>
      ) : null}

      <section className="account-filters">
        <div className="account-filter">
          <label>통화</label>
          <input
            type="text"
            value={filters.currencies}
            onChange={e => setFilters(prev => ({ ...prev, currencies: e.target.value }))}
            placeholder="예: USD, KRW"
          />
        </div>
        <div className="account-filter">
          <label>계좌 유형</label>
          <div className="filter-chips">
            {ACCOUNT_TYPES.map(type => (
              <button
                key={type}
                type="button"
                className={`chip-button ${filters.types.includes(type) ? 'is-active' : ''}`}
                onClick={() => toggleFilter('types', type)}
              >
                {type}
              </button>
            ))}
          </div>
        </div>
        <div className="account-filter">
          <label>상태</label>
          <div className="filter-chips">
            {ACCOUNT_STATUSES.map(status => (
              <button
                key={status}
                type="button"
                className={`chip-button ${filters.statuses.includes(status) ? 'is-active' : ''}`}
                onClick={() => toggleFilter('statuses', status)}
              >
                {status}
              </button>
            ))}
          </div>
        </div>
      </section>

      {error && <div className="account-alert is-error">{error}</div>}
      {actionMessage && <div className="account-alert is-success">{actionMessage}</div>}
      {actionError && <div className="account-alert is-error">{actionError}</div>}

      <section id="account-list" className="section-heading">
        <div>
          <span className="section-heading__eyebrow">1. 계좌 확인</span>
          <h3 className="section-heading__title">내 계좌를 먼저 확인하세요</h3>
          <p className="section-heading__text">계좌번호, 통화, 사용 가능 금액만 먼저 보이도록 정리했습니다.</p>
        </div>
      </section>

      <section className="account-grid">
        {accounts.length === 0 ? (
          <div className="account-empty">표시할 계좌가 없습니다.</div>
        ) : (
          accounts.map(account => (
            <article key={account.accountNo} className="account-card">
              <div className="account-card__header">
                <div>
                  <h3>{account.accountNo}</h3>
                  <p>{account.currency} · {account.type}</p>
                </div>
                <span className={`account-status account-status--${account.status?.toLowerCase() || 'active'}`}>
                  {formatAccountStatus(account.status)}
                </span>
              </div>
              <div className="account-card__body">
                <div>
                  <span>총 잔액</span>
                  <strong>{formatMoney(account.cashBalance)}</strong>
                </div>
                <div>
                  <span>사용 가능 금액</span>
                  <strong>{formatMoney(account.availableCash)}</strong>
                </div>
              </div>
            </article>
          ))
        )}
      </section>

      <section className="section-heading">
        <div>
          <span className="section-heading__eyebrow">2. 바로 처리</span>
          <h3 className="section-heading__title">자주 하는 작업을 아래에서 바로 진행하세요</h3>
          <p className="section-heading__text">계좌 개설, 입금, 출금, 이체를 별도 화면 이동 없이 처리할 수 있습니다.</p>
        </div>
      </section>

      <section className="account-actions">
        <form id="account-create" className="account-panel" onSubmit={handleCreate}>
          <h3>계좌 개설</h3>
          <label>계좌 유형</label>
          <select
            value={createForm.accountType}
            onChange={e => setCreateForm(prev => ({ ...prev, accountType: e.target.value }))}
          >
            {ACCOUNT_TYPES.map(type => (
              <option key={type} value={type}>{type}</option>
            ))}
          </select>
          <label>통화</label>
          <input
            type="text"
            value={createForm.currency}
            onChange={e => setCreateForm(prev => ({ ...prev, currency: e.target.value }))}
            placeholder="USD"
          />
          <button type="submit" className="primary-button">개설</button>
        </form>

        <form id="account-deposit" className="account-panel" onSubmit={handleDeposit}>
          <h3>입금</h3>
          <label>계좌번호</label>
          <select
            value={depositForm.accountNo}
            onChange={e => setDepositForm(prev => ({ ...prev, accountNo: e.target.value }))}
          >
            <option value="">계좌 선택</option>
            {activeAccounts.map(account => (
              <option key={account.accountNo} value={account.accountNo}>
                {account.accountNo} · {account.currency}
              </option>
            ))}
          </select>
          <label>금액</label>
          <input
            type="number"
            value={depositForm.amount}
            onChange={e => setDepositForm(prev => ({ ...prev, amount: e.target.value }))}
            placeholder="1000"
          />
          <button type="submit" className="primary-button">입금</button>
        </form>

        <form id="account-withdraw" className="account-panel" onSubmit={handleWithdraw}>
          <h3>출금</h3>
          <label>계좌번호</label>
          <select
            value={withdrawForm.accountNo}
            onChange={e => setWithdrawForm(prev => ({ ...prev, accountNo: e.target.value }))}
          >
            <option value="">계좌 선택</option>
            {activeAccounts.map(account => (
              <option key={account.accountNo} value={account.accountNo}>
                {account.accountNo} · {account.currency}
              </option>
            ))}
          </select>
          <label>금액</label>
          <input
            type="number"
            value={withdrawForm.amount}
            onChange={e => setWithdrawForm(prev => ({ ...prev, amount: e.target.value }))}
            placeholder="1000"
          />
          <button type="submit" className="primary-button">출금</button>
        </form>

        <form id="account-transfer" className="account-panel account-panel--wide" onSubmit={handleTransfer}>
          <h3>계좌 이체</h3>
          <div className="account-row">
            <div>
              <label>출금 계좌</label>
              <select
                value={transferForm.fromAccountNo}
                onChange={e => setTransferForm(prev => ({ ...prev, fromAccountNo: e.target.value }))}
              >
                <option value="">출금 계좌 선택</option>
                {activeAccounts.map(account => (
                  <option key={account.accountNo} value={account.accountNo}>
                    {account.accountNo} · {account.currency}
                  </option>
                ))}
              </select>
            </div>
            <div>
              <label>입금 계좌</label>
              <select
                value={transferForm.toAccountNo}
                onChange={e => setTransferForm(prev => ({ ...prev, toAccountNo: e.target.value }))}
              >
                <option value="">입금 계좌 선택</option>
                {activeAccounts.map(account => (
                  <option key={account.accountNo} value={account.accountNo}>
                    {account.accountNo} · {account.currency}
                  </option>
                ))}
              </select>
            </div>
          </div>
          <div className="account-row">
            <div>
              <label>금액</label>
              <input
                type="number"
                value={transferForm.amount}
                onChange={e => setTransferForm(prev => ({ ...prev, amount: e.target.value }))}
                placeholder="1000"
              />
            </div>
            <div>
              <label>참조 ID</label>
              <input
                type="text"
                value={transferForm.referenceId}
                onChange={e => setTransferForm(prev => ({ ...prev, referenceId: e.target.value }))}
                placeholder="Optional"
              />
            </div>
          </div>
          <div className="account-row">
            <div>
              <label>출금 메모</label>
              <input
                type="text"
                value={transferForm.fromDescription}
                onChange={e => setTransferForm(prev => ({ ...prev, fromDescription: e.target.value }))}
                placeholder="메모"
              />
            </div>
            <div>
              <label>입금 메모</label>
              <input
                type="text"
                value={transferForm.toDescription}
                onChange={e => setTransferForm(prev => ({ ...prev, toDescription: e.target.value }))}
                placeholder="메모"
              />
            </div>
          </div>
          <button type="submit" className="primary-button">이체</button>
        </form>
      </section>
    </div>
  )
}

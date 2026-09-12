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
const ACCOUNT_ACTIONS = [
  { id: 'deposit', label: '입금', description: '선택한 계좌에 금액을 바로 넣습니다.' },
  { id: 'withdraw', label: '출금', description: '선택한 계좌에서 출금합니다.' },
  { id: 'transfer', label: '이체', description: '내 계좌끼리 바로 옮깁니다.' },
  { id: 'create', label: '계좌 개설', description: '새 통화나 계좌 유형을 바로 추가합니다.' },
]

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
  const [selectedAccountNo, setSelectedAccountNo] = useState('')
  const [selectedAction, setSelectedAction] = useState('deposit')
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

  const activeAccounts = useMemo(() => accounts.filter(account => account.status === 'ACTIVE'), [accounts])
  const totalAvailable = useMemo(
    () => accounts.reduce((sum, account) => sum + (Number(account.availableCash) || 0), 0),
    [accounts]
  )
  const totalBalance = useMemo(
    () => accounts.reduce((sum, account) => sum + (Number(account.cashBalance) || 0), 0),
    [accounts]
  )
  const accountCurrencies = useMemo(
    () => Array.from(new Set(accounts.map(account => account.currency).filter(Boolean))),
    [accounts]
  )

  const filterPayload = useMemo(
    () => ({
      currencies: parseCsv(filters.currencies),
      types: filters.types,
      statuses: filters.statuses,
    }),
    [filters]
  )

  const selectedAccount = useMemo(
    () => accounts.find(account => account.accountNo === selectedAccountNo) || null,
    [accounts, selectedAccountNo]
  )

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

  useEffect(() => {
    if (accounts.length === 0) {
      setSelectedAccountNo('')
      return
    }
    const exists = accounts.some(account => account.accountNo === selectedAccountNo)
    if (!exists) {
      setSelectedAccountNo((activeAccounts[0] || accounts[0]).accountNo)
    }
  }, [accounts, activeAccounts, selectedAccountNo])

  useEffect(() => {
    if (!selectedAccountNo) return
    setDepositForm(prev => ({ ...prev, accountNo: selectedAccountNo }))
    setWithdrawForm(prev => ({ ...prev, accountNo: selectedAccountNo }))
    setTransferForm(prev => ({ ...prev, fromAccountNo: selectedAccountNo }))
  }, [selectedAccountNo])

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
      setCreateForm(prev => ({ ...prev, currency: 'USD' }))
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
      await depositAccount({
        accountNo: depositForm.accountNo.trim(),
        amount: Number(depositForm.amount),
      })
      setActionMessage('입금이 완료되었습니다.')
      setDepositForm(prev => ({ ...prev, amount: '' }))
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
      await withdrawAccount({
        accountNo: withdrawForm.accountNo.trim(),
        amount: Number(withdrawForm.amount),
      })
      setActionMessage('출금이 완료되었습니다.')
      setWithdrawForm(prev => ({ ...prev, amount: '' }))
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
      await transferAccount({
        fromAccountNo: transferForm.fromAccountNo.trim(),
        toAccountNo: transferForm.toAccountNo.trim(),
        amount: Number(transferForm.amount),
        referenceId: transferForm.referenceId.trim() || undefined,
        fromDescription: transferForm.fromDescription.trim() || undefined,
        toDescription: transferForm.toDescription.trim() || undefined,
      })
      setActionMessage('계좌 이체가 완료되었습니다.')
      setTransferForm(prev => ({
        ...prev,
        amount: '',
        referenceId: '',
        fromDescription: '',
        toDescription: '',
      }))
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

  const renderActionForm = () => {
    if (selectedAction === 'create') {
      return (
        <form className="account-power-panel__form" onSubmit={handleCreate}>
          <div className="account-row">
            <div>
              <label>계좌 유형</label>
              <select
                value={createForm.accountType}
                onChange={e => setCreateForm(prev => ({ ...prev, accountType: e.target.value }))}
              >
                {ACCOUNT_TYPES.map(type => (
                  <option key={type} value={type}>{type}</option>
                ))}
              </select>
            </div>
            <div>
              <label>통화</label>
              <input
                type="text"
                value={createForm.currency}
                onChange={e => setCreateForm(prev => ({ ...prev, currency: e.target.value.toUpperCase() }))}
                placeholder="USD"
              />
            </div>
          </div>
          <button type="submit" className="primary-button">계좌 개설</button>
        </form>
      )
    }

    if (selectedAction === 'transfer') {
      return (
        <form className="account-power-panel__form" onSubmit={handleTransfer}>
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
                placeholder="선택 입력"
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
          <button type="submit" className="primary-button">이체 실행</button>
        </form>
      )
    }

    const isDeposit = selectedAction === 'deposit'
    const formState = isDeposit ? depositForm : withdrawForm
    const setFormState = isDeposit ? setDepositForm : setWithdrawForm
    const onSubmit = isDeposit ? handleDeposit : handleWithdraw

    return (
      <form className="account-power-panel__form" onSubmit={onSubmit}>
        <div className="account-row">
          <div>
            <label>계좌번호</label>
            <select
              value={formState.accountNo}
              onChange={e => setFormState(prev => ({ ...prev, accountNo: e.target.value }))}
            >
              <option value="">계좌 선택</option>
              {activeAccounts.map(account => (
                <option key={account.accountNo} value={account.accountNo}>
                  {account.accountNo} · {account.currency}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label>금액</label>
            <input
              type="number"
              value={formState.amount}
              onChange={e => setFormState(prev => ({ ...prev, amount: e.target.value }))}
              placeholder="1000"
            />
          </div>
        </div>
        <button type="submit" className="primary-button">{isDeposit ? '입금 실행' : '출금 실행'}</button>
      </form>
    )
  }

  return (
    <div className="account-page">
      <section className="account-overview-card">
        <div className="account-overview-card__main">
          <span className="intro-eyebrow">계좌</span>
          <h2>계좌는 단순하게 보고, 작업은 바로 실행합니다</h2>
          <p className="account-subtitle">복잡한 폼 나열 대신 선택한 계좌 기준으로 입금, 출금, 이체, 개설을 한 곳에서 처리할 수 있습니다.</p>
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

      <section className="account-summary">
        <div className="account-summary__card">
          <span>전체 계좌</span>
          <strong>{accounts.length}</strong>
        </div>
        <div className="account-summary__card">
          <span>활성 계좌</span>
          <strong>{activeAccounts.length}</strong>
        </div>
        <div className="account-summary__card">
          <span>총 잔액</span>
          <strong>{formatMoney(totalBalance)}</strong>
        </div>
        <div className="account-summary__card">
          <span>사용 가능 금액</span>
          <strong>{formatMoney(totalAvailable)}</strong>
        </div>
      </section>

      <section className="account-workspace">
        <section className="account-list-panel">
          <div className="account-list-panel__header">
            <div>
              <span className="section-heading__eyebrow">계좌 선택</span>
              <h3 className="section-heading__title">필요한 계좌만 바로 고르세요</h3>
              <p className="section-heading__text">통화와 상태를 가볍게 거른 뒤, 계좌를 선택하면 오른쪽 작업 패널이 바로 연결됩니다.</p>
            </div>
          </div>

          <div className="account-filters">
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
          </div>

          {error && <div className="account-alert is-error">{error}</div>}
          {actionMessage && <div className="account-alert is-success">{actionMessage}</div>}
          {actionError && <div className="account-alert is-error">{actionError}</div>}

          <div className="account-spotlight">
            <div>
              <span>선택한 계좌</span>
              <strong>{selectedAccount?.accountNo || '계좌를 선택하세요'}</strong>
              <p>{selectedAccount ? `${selectedAccount.currency} · ${selectedAccount.type}` : '계좌 목록에서 작업할 계좌를 고르면 됩니다.'}</p>
            </div>
            <div className="account-spotlight__stats">
              <div>
                <span>총 잔액</span>
                <strong>{selectedAccount ? formatMoney(selectedAccount.cashBalance) : '-'}</strong>
              </div>
              <div>
                <span>사용 가능</span>
                <strong>{selectedAccount ? formatMoney(selectedAccount.availableCash) : '-'}</strong>
              </div>
            </div>
          </div>

          <section className="account-list">
            {accounts.length === 0 ? (
              <div className="account-empty">표시할 계좌가 없습니다.</div>
            ) : (
              accounts.map(account => (
                <button
                  key={account.accountNo}
                  type="button"
                  className={`account-list-item ${selectedAccountNo === account.accountNo ? 'is-selected' : ''}`}
                  onClick={() => setSelectedAccountNo(account.accountNo)}
                >
                  <div className="account-list-item__header">
                    <div>
                      <strong>{account.accountNo}</strong>
                      <p>{account.currency} · {account.type}</p>
                    </div>
                    <span className={`account-status account-status--${account.status?.toLowerCase() || 'active'}`}>
                      {formatAccountStatus(account.status)}
                    </span>
                  </div>
                  <div className="account-list-item__body">
                    <div>
                      <span>총 잔액</span>
                      <strong>{formatMoney(account.cashBalance)}</strong>
                    </div>
                    <div>
                      <span>사용 가능</span>
                      <strong>{formatMoney(account.availableCash)}</strong>
                    </div>
                  </div>
                </button>
              ))
            )}
          </section>
        </section>

        <aside className="account-power-panel">
          <div className="account-power-panel__header">
            <div>
              <span className="section-heading__eyebrow">빠른 작업</span>
              <h3 className="section-heading__title">한 곳에서 바로 처리하세요</h3>
              <p className="section-heading__text">계좌를 고른 뒤 아래 작업만 바꿔가며 빠르게 처리할 수 있습니다.</p>
            </div>
          </div>

          <div className="account-power-panel__selected">
            <span>작업 기준 계좌</span>
            <strong>{selectedAccount?.accountNo || '선택된 계좌 없음'}</strong>
            <p>
              {selectedAccount
                ? `${selectedAccount.currency} · 사용 가능 ${formatMoney(selectedAccount.availableCash)}`
                : '계좌 개설은 계좌 선택 없이 진행할 수 있습니다.'}
            </p>
          </div>

          <div className="account-action-tabs">
            {ACCOUNT_ACTIONS.map(action => (
              <button
                key={action.id}
                type="button"
                className={`account-action-tab ${selectedAction === action.id ? 'is-active' : ''}`}
                onClick={() => setSelectedAction(action.id)}
              >
                <strong>{action.label}</strong>
                <span>{action.description}</span>
              </button>
            ))}
          </div>

          {renderActionForm()}

          <div className="account-power-panel__footnote">
            <span>보유 통화</span>
            <strong>{accountCurrencies.join(', ') || '-'}</strong>
          </div>
        </aside>
      </section>
    </div>
  )
}

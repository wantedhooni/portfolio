package com.revy.securities.domain.account;

import com.github.f4b6a3.uuid.UuidCreator;
import com.revy.securities.domain.account.enums.DirectionType;
import com.revy.securities.domain.account.enums.StatementType;
import com.revy.securities.domain.common.BaseUUIDEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * [도메인] 계좌 내역 라인(AccountStatementLine)
 * <p>
 * - 사용자가 보는 "통장 내역 1줄"에 해당하는 조회 최적화 엔티티(프로젝션)
 * - 원장(Ledger)에서 파생된 결과를 저장하거나, 조회 성능/UX를 위해 별도로 유지한다.
 * <p>
 * [왜 필요한가]
 * - 원장(LedgerEntry)을 직접 UI 조회에 쓰면, 복식부기/수수료/세금 라인 등으로 인해
 * 사용자에게 보여줄 "1줄 내역"을 만들기 위한 조합 로직이 복잡해질 수 있다.
 * - Statement는 계좌 기준 페이징/검색/필터를 빠르게 제공하기 위한 모델이다.
 * <p>
 * [데이터 일관성]
 * - tx_id를 연결하면 감사/추적이 쉬워진다.
 * - 다만 외부에서 유입된 임시 내역 등을 고려하면 tx를 nullable로 둘 수도 있다(선택).
 */

@Entity
@Table(
        name = "account_statement",
        indexes = {
                @Index(name = "ix_stmt_account_time", columnList = "account_id, occurred_at"),
               // @Index(name = "ix_stmt_tx_id", columnList = "tx_id")
        }
)

@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AccountStatementLine extends BaseUUIDEntity {
    /** 조회 대상 계좌 */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "account_id", nullable = false)
    private Account account;

    /** 원장 거래(선택): 원장 기반이면 보통 연결, 외부/임시 내역이면 null 가능 */

    /*
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tx_id")
    private LedgerTransaction tx;
     */

    /** 내역 발생 시각(정렬 기준) */
    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt = LocalDateTime.now();

    /** 계좌 잔액 변동 방향 */
    @Enumerated(EnumType.STRING)
    @Column(name = "direction_type", nullable = false, length = 20)
    private DirectionType directionType;

    /** 내역 종류*/
    @Enumerated(EnumType.STRING)
    @Column(name = "statement_type", nullable = false, length = 20)
    private StatementType statementType;

    /** 내역 금액(사인 포함): 입금 +, 출금 - */
    @Column(name = "amount_signed", nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal amountSigned;

    /** 내역 반영 후 잔액(조회용) */
    @Column(name = "balance", nullable = false, updatable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    /** Transaction reference number ID */
    @Column(name = "reference_id", length = 60)
    private String referenceId;

    /** 사용자에게 보여줄 설명 */
    @Column(name = "description", nullable = true, length = 255)
    private String description;



    public static AccountStatementLine create(
            Account account,
            DirectionType directionType,
            StatementType statementType,
            BigDecimal amountSigned,
            BigDecimal balance,
            String referenceId,
            String description
    ) {
        AccountStatementLine line = new AccountStatementLine();
        line.id = UuidCreator.getTimeOrderedEpoch();
        line.occurredAt = LocalDateTime.now();
        line.account = account;
        line.directionType = directionType;
        line.statementType = statementType;
        line.amountSigned = amountSigned;
        line.balance = balance;
        line.referenceId = referenceId;
        line.description = description;
        return line;
    }
}

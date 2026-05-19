package com.revy.example.account.reader;

import com.revy.example.account.reader.dto.AccountResult;
import com.revy.example.account.reader.dto.AccountSearchCondition;
import com.revy.example.account.reader.dto.AccountTxResult;
import com.revy.example.account.reader.dto.AccountTxSearchCondition;
import com.revy.example.account.reader.dto.StockPositionResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

/** business-logic 모듈의 read-side 공개 API — 모든 메서드는 DTO 만 in/out (JPA entity 금지) */
public interface AccountReader {

    // ── Account ──────────────────────────────────────────────────

    Optional<AccountResult> getAccountById(Long id);

    Optional<AccountResult> findByAccountNumber(String accountNumber);

    boolean existsByAccountNumber(String accountNumber);

    Page<AccountResult> searchAccounts(Pageable pageable, AccountSearchCondition condition);

    List<AccountResult> findAllByUserId(Long userId);

    // ── AccountTx ────────────────────────────────────────────────

    Optional<AccountTxResult> getAccountTxById(Long id);

    /** 멱등성 체크 — 브로커 API 재전송·네트워크 재시도로 인한 이중 처리 방지 */
    boolean existsTxByReferenceId(String referenceId);

    Page<AccountTxResult> searchAccountTx(Pageable pageable, AccountTxSearchCondition condition);

    // ── StockPosition ────────────────────────────────────────────

    Optional<StockPositionResult> findPositionByAccountAndStock(Long accountId, Long stockId);

    List<StockPositionResult> findAllPositionsByAccountId(Long accountId);
}

package com.revy.example.fx.command.impl;

import com.revy.example.account.command.AccountCommand;
import com.revy.example.account.command.dto.DepositCommand;
import com.revy.example.account.command.dto.WithdrawCommand;
import com.revy.example.account.reader.AccountReader;
import com.revy.example.account.reader.dto.AccountResult;
import com.revy.example.core.error.BusinessException;
import com.revy.example.core.error.ErrorCode;
import com.revy.example.domain.account.exception.AccountNotFoundException;
import com.revy.example.domain.fx.Currency;
import com.revy.example.domain.fx.ExchangeRate;
import com.revy.example.domain.fx.ExchangeRateHistory;
import com.revy.example.domain.fx.FxConversion;
import com.revy.example.domain.fx.FxCorridor;
import com.revy.example.domain.fx.exception.CurrencyNotActiveException;
import com.revy.example.domain.fx.exception.CurrencyNotFoundException;
import com.revy.example.domain.fx.exception.ExchangeRateNotFoundException;
import com.revy.example.domain.fx.exception.FxCorridorDuplicatedException;
import com.revy.example.domain.fx.exception.FxCorridorNotFoundException;
import com.revy.example.domain.fx.exception.InvalidFxPairException;
import com.revy.example.fx.command.FxCommand;
import com.revy.example.fx.command.dto.ConvertCurrencyCommand;
import com.revy.example.fx.command.dto.CreateFxCorridorCommand;
import com.revy.example.fx.command.dto.QuoteExchangeRateCommand;
import com.revy.example.fx.command.dto.RegisterCurrencyCommand;
import com.revy.example.fx.command.dto.UpdateFxCorridorCommand;
import com.revy.example.fx.reader.FxReader;
import com.revy.example.fx.reader.dto.CurrencyResult;
import com.revy.example.fx.reader.dto.ExchangeRateResult;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.UUID;

@Slf4j
@Component
@Transactional
@RequiredArgsConstructor
public class FxCommandImpl implements FxCommand {

    private final EntityManager   entityManager;
    private final FxReader        fxReader;
    private final AccountReader   accountReader;
    private final AccountCommand  accountCommand;

    @Override
    public Long registerCurrency(RegisterCurrencyCommand command) {
        if (fxReader.existsCurrencyByCode(command.code())) {
            throw new BusinessException(ErrorCode.CURRENCY_DUPLICATED, "code=" + command.code());
        }
        Currency c = Currency.register(command.code(), command.name(), command.symbol(), command.decimalPlaces());
        entityManager.persist(c);
        // TODO:REVY - EVENT 발행(CurrencyRegistered) - commit after
        return c.getId();
    }

    @Override
    public void deactivateCurrency(String code) {
        loadCurrency(code).deactivate();
        // TODO:REVY - EVENT 발행(CurrencyDeactivated) - commit after
    }

    @Override
    public void activateCurrency(String code) {
        loadCurrency(code).activate();
        // TODO:REVY - EVENT 발행(CurrencyActivated) - commit after
    }

    @Override
    public Long quoteRate(QuoteExchangeRateCommand command) {
        // 통화 존재 검증
        if (!fxReader.existsCurrencyByCode(command.baseCurrencyCode()))  throw new CurrencyNotFoundException(command.baseCurrencyCode());
        if (!fxReader.existsCurrencyByCode(command.quoteCurrencyCode())) throw new CurrencyNotFoundException(command.quoteCurrencyCode());

        // 1. 이력 테이블에 먼저 INSERT
        ExchangeRateHistory history = ExchangeRateHistory.record(
            command.baseCurrencyCode(), command.quoteCurrencyCode(),
            command.rateType(), command.rate(), command.quotedAt(), command.source()
        );
        entityManager.persist(history);

        // 2. 현재 환율 UPSERT — 있으면 refresh, 없으면 INSERT
        ExchangeRate current = entityManager.createQuery(
                "SELECT r FROM ExchangeRate r " +
                "WHERE r.baseCurrencyCode = :base AND r.quoteCurrencyCode = :quote AND r.rateType = :type",
                ExchangeRate.class)
            .setParameter("base", command.baseCurrencyCode())
            .setParameter("quote", command.quoteCurrencyCode())
            .setParameter("type", command.rateType())
            .getResultStream().findFirst()
            .orElse(null);

        if (current != null) {
            current.refresh(command.rate(), command.quotedAt(), command.source());
        } else {
            current = ExchangeRate.of(
                command.baseCurrencyCode(), command.quoteCurrencyCode(),
                command.rateType(), command.rate(), command.quotedAt(), command.source()
            );
            entityManager.persist(current);
        }

        // TODO:REVY - EVENT 발행(ExchangeRateQuoted) - commit after
        return current.getId();
    }

    @Override
    public Long convertCurrency(ConvertCurrencyCommand command) {
        // 1. 멱등성 체크
        if (fxReader.existsConversionByReferenceId(command.referenceId())) {
            log.info("Duplicate FX conversion ignored. referenceId={}", command.referenceId());
            return fxReader.findConversionByNumber(command.referenceId()).map(c -> c.id()).orElse(null);
        }

        // 2. 통화 활성 검증
        CurrencyResult fromCur = loadCurrencyDto(command.fromCurrencyCode());
        CurrencyResult toCur   = loadCurrencyDto(command.toCurrencyCode());
        if (!fromCur.isActive() || !toCur.isActive()) throw new CurrencyNotActiveException();
        if (fromCur.code().equals(toCur.code())) throw new InvalidFxPairException("same currency");

        // 3. 계좌 검증 (통화 일치)
        AccountResult fromAccount = accountReader.getAccountById(command.fromAccountId())
            .orElseThrow(AccountNotFoundException::new);
        AccountResult toAccount = accountReader.getAccountById(command.toAccountId())
            .orElseThrow(AccountNotFoundException::new);

        if (!fromAccount.currency().equals(command.fromCurrencyCode())) {
            throw new InvalidFxPairException("fromAccount currency mismatch: " + fromAccount.currency());
        }
        if (!toAccount.currency().equals(command.toCurrencyCode())) {
            throw new InvalidFxPairException("toAccount currency mismatch: " + toAccount.currency());
        }

        // 4. 현재 환율 조회
        ExchangeRateResult rate = fxReader.findCurrentRate(
                command.fromCurrencyCode(), command.toCurrencyCode(), command.rateType())
            .orElseThrow(() -> new ExchangeRateNotFoundException(command.fromCurrencyCode(), command.toCurrencyCode()));

        // 5. 환산 금액 계산 — fee 가 null 이면 0으로 처리 (수수료 없는 내부 환전 허용)
        BigDecimal fee = (command.fee() != null) ? command.fee() : BigDecimal.ZERO;
        BigDecimal grossToAmount = command.fromAmount().multiply(rate.rate())
            .setScale(toCur.decimalPlaces(), RoundingMode.HALF_UP);
        BigDecimal netToAmount   = grossToAmount.subtract(fee)
            .setScale(toCur.decimalPlaces(), RoundingMode.HALF_UP);

        // 6. FxConversion 생성
        String conversionNumber = "FX-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
        FxConversion conversion = FxConversion.request(
            conversionNumber, command.fromAccountId(), command.toAccountId(),
            command.fromCurrencyCode(), command.toCurrencyCode(),
            command.fromAmount(), netToAmount,
            rate.rate(), command.rateType(),
            fee, command.referenceId()
        );
        entityManager.persist(conversion);

        // 7. 출금 — fromAccount
        String debitRef  = command.referenceId() + "-DEBIT";
        accountCommand.withdraw(new WithdrawCommand(command.fromAccountId(), command.fromAmount(), debitRef));

        // 8. 입금 — toAccount (net 금액)
        String creditRef = command.referenceId() + "-CREDIT";
        accountCommand.deposit(new DepositCommand(command.toAccountId(), netToAmount, creditRef));

        // 9. FxConversion complete — debit/credit AccountTx ID 연결 (단순화: refId로 추후 조회)
        // 실제 운영에서는 AccountCommand가 반환하는 txId를 받도록 시그니처 확장 필요
        conversion.complete(null, null, Instant.now());

        // TODO:REVY - EVENT 발행(FxConversionCompleted) - commit after
        return conversion.getId();
    }

    // ── FxCorridor ───────────────────────────────────────────────

    @Override
    public Long createCorridor(CreateFxCorridorCommand command) {
        if (fxReader.existsCorridorByPair(command.baseCurrencyCode(), command.quoteCurrencyCode())) {
            throw new FxCorridorDuplicatedException(command.baseCurrencyCode(), command.quoteCurrencyCode());
        }
        // 통화 존재 검증
        if (!fxReader.existsCurrencyByCode(command.baseCurrencyCode()))  throw new CurrencyNotFoundException(command.baseCurrencyCode());
        if (!fxReader.existsCurrencyByCode(command.quoteCurrencyCode())) throw new CurrencyNotFoundException(command.quoteCurrencyCode());

        FxCorridor corridor = FxCorridor.create(
            command.baseCurrencyCode(), command.quoteCurrencyCode(),
            command.minAmount(), command.maxAmount(),
            command.dailyLimit(), command.spreadRate()
        );
        entityManager.persist(corridor);
        return corridor.getId();
    }

    @Override
    public void updateCorridor(Long id, UpdateFxCorridorCommand command) {
        loadCorridor(id).update(
            command.minAmount(), command.maxAmount(),
            command.dailyLimit(), command.spreadRate()
        );
    }

    @Override
    public void activateCorridor(Long id) {
        loadCorridor(id).activate();
    }

    @Override
    public void deactivateCorridor(Long id) {
        loadCorridor(id).deactivate();
    }

    @Override
    public void suspendCorridor(Long id) {
        loadCorridor(id).suspend();
    }

    @Override
    public void deleteCorridor(Long id) {
        FxCorridor corridor = loadCorridor(id);
        entityManager.remove(corridor);
    }

    // ── 내부 ─────────────────────────────────────────────────────

    private Currency loadCurrency(String code) {
        return entityManager.createQuery(
                "SELECT c FROM Currency c WHERE c.code = :code", Currency.class)
            .setParameter("code", code)
            .getResultStream().findFirst()
            .orElseThrow(() -> new CurrencyNotFoundException(code));
    }

    private CurrencyResult loadCurrencyDto(String code) {
        return fxReader.findCurrencyByCode(code)
            .orElseThrow(() -> new CurrencyNotFoundException(code));
    }

    private FxCorridor loadCorridor(Long id) {
        return entityManager.createQuery(
                "SELECT c FROM FxCorridor c WHERE c.id = :id", FxCorridor.class)
            .setParameter("id", id)
            .getResultStream().findFirst()
            .orElseThrow(() -> new FxCorridorNotFoundException(id));
    }
}

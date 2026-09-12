package com.revy.api_server.application.web.api.account.usecase.impl;

import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import com.revy.securities.domain.account.Account;
import com.revy.securities.domain.account.AccountStatementLine;
import com.revy.securities.domain.account.QAccount;
import com.revy.securities.domain.account.enums.AccountStatus;
import com.revy.securities.domain.account.enums.AccountType;
import com.revy.securities.domain.account.enums.DirectionType;
import com.revy.securities.domain.account.enums.StatementType;
import com.revy.securities.domain.account.repo.AccountRepo;
import com.revy.securities.domain.account.repo.AccountStatementLineRepo;
import com.revy.securities.domain.account.service.AccountService;
import com.revy.api_server.application.web.api.account.payload.CreateAccountPayload;
import com.revy.api_server.application.web.api.account.payload.MyAccountsPayload;
import com.revy.api_server.application.web.api.account.payload.TransferPayload;
import com.revy.api_server.application.web.api.account.usecase.AccountUseCase;
import com.revy.api_server.application.exception.AccountException;
import com.revy.api_server.application.support.AccountSupport;
import com.revy.common.enums.Currency;
import com.revy.common.utils.BigDecimalUtil;
import com.revy.securities.domain.processor.account.AccountProcessor;
import com.revy.securities.domain.processor.account.dto.AccountResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountUseCaseImpl implements AccountUseCase {
    private final AccountRepo accountRepo;
    private final AccountStatementLineRepo accountStatementLineRepo;
    private final JPAQueryFactory jpaQueryFactory;
    private final AccountSupport accountSupport;

    private final AccountProcessor AccountProcessor;
    private final String BBB = "999"; // 서비스/기관 코드(임시)
    private final String PPP = "001"; // 상품/테넌트/채널

    @Override
    @Transactional
    public CreateAccountPayload.Res create(Long userId, AccountType accountType, Currency currency) {
        Assert.notNull(userId, "userId is null");
        Assert.notNull(accountType, "accountType is null");
        Assert.notNull(currency, "currency is null");
        AccountResultDto.AccountInfo result = AccountProcessor.create(userId, accountType, currency);
        return mapper.convertCreateAccountRes(result);
    }

    @Override
    @Transactional(readOnly = true)
    public List<MyAccountsPayload.Res> getMyAccounts(Long userId, MyAccountsPayload.Req req) {
        Assert.notNull(userId, "userId is null");

        // 동적쿼리 보정
        if (req == null) {
            req = new MyAccountsPayload.Req(null, null, null);
        }

        JPAQuery<MyAccountsPayload.Res> query = jpaQueryFactory.select(
                Projections.constructor(MyAccountsPayload.Res.class, QAccount.account.type, QAccount.account.accountNo,
                                        QAccount.account.currency, QAccount.account.status,
                                        QAccount.account.cashBalance, QAccount.account.availableCash)).from(
                QAccount.account);
        query.where(QAccount.account.ownerId.eq(userId));

        // 조건부 동적쿼리
        if (req.currencies() != null && !req.currencies().isEmpty()) {
            query.where(QAccount.account.currency.in(req.currencies()));
        }

        if (req.types() != null && !req.types().isEmpty()) {
            query.where(QAccount.account.type.in(req.types()));
        }

        if (req.statuses() != null && !req.statuses().isEmpty()) {
            query.where(QAccount.account.status.in(req.statuses()));
        }

        // 만든 순서대로 정렬
        query.orderBy(QAccount.account.createdDate.asc());

        // TODO:Revy 계좌 목록 조회할때 페이징이 있던가??
        return query.fetch();
    }

    @Override
    @Transactional
    public void deposit(Long userId, String accountNo, BigDecimal amount) {
        Assert.notNull(userId, "userId is null");
        Assert.hasText(accountNo, "accountNo is empty");
        Assert.notNull(amount, "amount is empty");
        Assert.isTrue(!BigDecimalUtil.isZero(amount), "amount must be positive");

        Account account = accountSupport.findOneByOwnerIdAndAccountNo(userId, accountNo);
        BigDecimal newBalance = account.getCashBalance().add(amount);
        AccountStatementLine stmt = AccountStatementLine.create(account, DirectionType.CREDIT, StatementType.DEPOSIT,
                                                                amount, newBalance, "DEP-" + System.currentTimeMillis(),
                                                                "Deposit of " + amount + " to account " + accountNo);
        accountRepo.addAllBalance(accountNo, amount);
        accountStatementLineRepo.save(stmt);
        //TODO:Revy 현실 / 현업에서는 commit after뒤에 노티가 있겠지?
    }

    @Override
    @Transactional
    public void withdraw(Long userId, String accountNo, BigDecimal amount) {
        // 1. validation
        Assert.notNull(userId, "userId is null");
        Assert.hasText(accountNo, "accountNo is empty");
        Assert.notNull(amount, "amount is empty");
        Assert.isTrue(!BigDecimalUtil.isZero(amount), "amount must be positive");
        Account account = accountSupport.findOneByOwnerIdAndAccountNo(userId, accountNo);
        /*
        출금 타입에 따라서 체크 안해야 할수도 있다. 미수금 발생 등등
         */
        Assert.isTrue(BigDecimalUtil.isGreaterThanOrEqualTo(account.getCashBalance().subtract(amount), BigDecimal.ZERO),
                      "Insufficient funds for withdrawal");

        // 2. 출금  처리
        BigDecimal newBalance = account.getCashBalance().subtract(amount);

        BigDecimal negateAmount = amount.negate(); // 요청 금에 - 부호 추가
        AccountStatementLine stmt = AccountStatementLine.create(account, DirectionType.DEBIT, StatementType.WITHDRAW,
                                                                negateAmount, newBalance,
                                                                "WDR-" + System.currentTimeMillis(),
                                                                "Withdrawal of " + amount + " from account " + accountNo);

        accountRepo.addAllBalance(accountNo, negateAmount);
        accountStatementLineRepo.save(stmt);
        //TODO:Revy 현실 / 현업에서는 commit after뒤에 노티가 있겠지?
    }

    @Override
    @Transactional
    public TransferPayload.Res transfer(Long userId, TransferPayload.Req req) {

        // 1. valid
        Assert.notNull(userId, "userId is null");
        Assert.notNull(req, "TransferPayload.Req is null");
        Assert.hasText(req.fromAccountNo(), "fromAccountNo is empty");
        Assert.hasText(req.toAccountNo(), "toAccountNo is empty");
        Assert.notNull(req.amount(), "amount is null");
        Assert.isTrue(!BigDecimalUtil.isZero(req.amount()), "amount must be positive");

        // 2. 사용자 계좌 검증 및 잔고 확인
        Account fromAccount = accountSupport.findOneByOwnerIdAndAccountNo(userId, req.fromAccountNo());
        if (fromAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountException(AccountException.AccountErrorCode.ACCOUNT_NOT_ACTIVE);
        }
        Assert.isTrue(BigDecimalUtil.isGreaterThanOrEqualTo(fromAccount.getCashBalance().subtract(req.amount()),
                                                            BigDecimal.ZERO), "Insufficient funds for withdrawal");


        // 3. 수취 계좌 검증
        /*
         TODO:Revy 오픈뱅킹이나 은행시스템이용 계좌주 조회해서 유효성 검증 해야함(타은행은 DB에 없으니).
         */
        Account toAccount = accountSupport.findOneByAccountNo(req.toAccountNo());

        if (toAccount.getStatus() != AccountStatus.ACTIVE) {
            throw new AccountException(AccountException.AccountErrorCode.TO_ACCOUNT_NOT_ACTIVE);
        }

        if (!fromAccount.getCurrency().equals(toAccount.getCurrency())) {
            throw new AccountException(AccountException.AccountErrorCode.ACCOUNT_CURRENCY_MISMATCH);
        }


        // 4. FROM Account 출금  처리
        BigDecimal fromAccountNewBalance = fromAccount.getCashBalance().subtract(req.amount());
        BigDecimal negateAmount = req.amount().negate(); // 요청 금에 - 부호 추가
        AccountStatementLine fromStmt = AccountStatementLine.create(fromAccount, DirectionType.DEBIT,
                                                                    StatementType.TRANSFER_OUT, negateAmount,
                                                                    fromAccountNewBalance, req.referenceId(),
                                                                    req.fromDescription());
        accountRepo.addAllBalance(req.fromAccountNo(), negateAmount);
        /*

        TODO:Revy 금융권 실제면 여기서 끝나고 금융망으로 던져버리고, noti로 끝나야 하는데...
        이벤트 발행으로 바꿔버릴까?
         */

        // 5. To Account 입금
        BigDecimal toAccountNewBalance = toAccount.getCashBalance().add(req.amount());
        AccountStatementLine toStmt = AccountStatementLine.create(toAccount, DirectionType.CREDIT,
                                                                  StatementType.TRANSFER_IN, req.amount(),
                                                                  toAccountNewBalance, null, req.toDescription());
        accountRepo.addAllBalance(req.toAccountNo(), req.amount());
        accountStatementLineRepo.save(fromStmt);
        accountStatementLineRepo.save(toStmt);

        return new TransferPayload.Res(req.fromAccountNo(), req.toAccountNo(), req.amount(), fromAccountNewBalance,
                                       req.referenceId());
    }

    static class mapper {
        public static CreateAccountPayload.Res convertCreateAccountRes(AccountResultDto.AccountInfo newAccount) {
            return new CreateAccountPayload.Res(newAccount.type(),
                                                newAccount.accountNo(),
                                                newAccount.currency(),
                                                newAccount.cashBalance(),
                                                newAccount.availableCash());
        }
    }
}

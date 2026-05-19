package com.revy.example.account.command;

import com.revy.example.account.command.dto.DepositCommand;
import com.revy.example.account.command.dto.OpenAccountCommand;
import com.revy.example.account.command.dto.WithdrawCommand;

/** business-logic의 write-side 공개 API (service 역할). 모든 메서드는 DTO 만 in/out. */
public interface AccountCommand {

    /** 계좌 개설 — 계좌번호 자동 채번 */
    Long openAccount(OpenAccountCommand command);

    void updateAccountName(Long accountId, String accountName);

    void suspendAccount(Long accountId);

    void closeAccount(Long accountId);

    /** 입금 — referenceId로 멱등성 보장 */
    void deposit(DepositCommand command);

    /** 출금 — 가용잔고 부족 시 InsufficientBalanceException */
    void withdraw(WithdrawCommand command);
}

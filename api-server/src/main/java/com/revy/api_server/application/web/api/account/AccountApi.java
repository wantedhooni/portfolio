package com.revy.api_server.application.web.api.account;

import com.revy.api_server.application.web.api.account.payload.CreateAccountPayload;
import com.revy.api_server.application.web.api.account.payload.DepositAccountPayload;
import com.revy.api_server.application.web.api.account.payload.MyAccountsPayload;
import com.revy.api_server.application.web.api.account.payload.TransferPayload;
import com.revy.api_server.application.web.api.account.usecase.AccountUseCase;
import com.revy.api_server.application.infra.security.UserPrincipal;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/account")
@RequiredArgsConstructor
public class AccountApi {
    private final AccountUseCase accountUseCase;

    @PostMapping("/create")
    public CreateAccountPayload.Res createAccount(@AuthenticationPrincipal UserPrincipal currentUser, @RequestBody @Valid CreateAccountPayload.Req req) {
        log.debug("Create account {}", currentUser);
        log.debug("req {}", req);
        return accountUseCase.create(currentUser.getId(), req.accountType(), req.currency());
    }

    @GetMapping("/myAccounts")
    public List<MyAccountsPayload.Res> getMyAccounts(@AuthenticationPrincipal UserPrincipal currentUser, @ParameterObject MyAccountsPayload.Req req) {
        log.debug("Create account {}", currentUser);
        log.debug("req {}", req);
        return accountUseCase.getMyAccounts(currentUser.getId(), req);
    }

    /**
     * 입금
     *
     * @param currentUser
     * @param req
     * @return
     */
    @PostMapping("/deposit")
    public DepositAccountPayload.Res deposit(@AuthenticationPrincipal UserPrincipal currentUser, @RequestBody DepositAccountPayload.Req req) {
        accountUseCase.deposit(currentUser.getId(), req.accountNo(), req.amount());
        return new DepositAccountPayload.Res(true);
    }


    /**
     * 출금
     *
     * @param currentUser
     * @param req
     * @return
     */
    @PostMapping("/withdraw")
    public DepositAccountPayload.Res withdraw(@AuthenticationPrincipal UserPrincipal currentUser, @RequestBody DepositAccountPayload.Req req) {
        accountUseCase.withdraw(currentUser.getId(), req.accountNo(), req.amount());
        return new DepositAccountPayload.Res(true);
    }

    /**
     * 이체
     *
     * @param currentUser
     * @param req
     * @return
     */
    @PostMapping("/transfer")
    public TransferPayload.Res transfer(@AuthenticationPrincipal UserPrincipal currentUser, @RequestBody @Valid TransferPayload.Req req) {
        return accountUseCase.transfer(currentUser.getId(), req);
    }
}

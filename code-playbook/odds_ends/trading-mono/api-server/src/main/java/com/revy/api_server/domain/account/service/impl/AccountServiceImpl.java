package com.revy.api_server.domain.account.service.impl;

import com.revy.api_server.domain.account.repo.AccountRepo;
import com.revy.api_server.domain.account.service.AccountService;
import com.revy.common.utils.AccountNumberUtil;
import io.jsonwebtoken.lang.Assert;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AccountServiceImpl implements AccountService {
    private final AccountRepo accountRepo;

    @Override
    public String createBankAccountNumber(String bbb, String ppp) {
        Assert.hasText(bbb, "bbb is empty");
        Assert.hasText(ppp, "ppp is empty");
        return AccountNumberUtil.createBankAccountNumber(bbb, ppp, accountRepo.nextAccountNoSeq());
    }

    @Override
    public String createSecuritiesAccountNumber(String ppp) {
        Assert.hasText(ppp, "ppp is empty");
        return AccountNumberUtil.createSecuritiesAccountNumber(ppp, accountRepo.nextAccountNoSeq());
    }
}

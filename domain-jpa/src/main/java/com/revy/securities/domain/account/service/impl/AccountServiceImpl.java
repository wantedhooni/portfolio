package com.revy.securities.domain.account.service.impl;

import com.revy.securities.domain.account.repo.AccountRepo;
import com.revy.securities.domain.account.service.AccountService;
import com.revy.common.utils.AccountNumberUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

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

package com.revy.securities.domain.account.service;

public interface AccountService {
    String createBankAccountNumber(String bbb, String ppp);

    String createSecuritiesAccountNumber(String ppp);
}

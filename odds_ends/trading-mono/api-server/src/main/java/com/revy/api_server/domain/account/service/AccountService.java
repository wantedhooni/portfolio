package com.revy.api_server.domain.account.service;

public interface AccountService {
    String createBankAccountNumber(String bbb, String ppp);

    String createSecuritiesAccountNumber(String ppp);
}

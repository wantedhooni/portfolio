package com.revy.common.utils;


class AccountNumberUtilTest {
    public static void main(String[] args) {
        String bbb = "999"; // 은행코드 3자리
        String ppp = "099";  // 지점코드 2자리
        long seq = 1L; // (1)번 방식으로 획득
        System.out.println(AccountNumberUtil.createBankAccountNumber(bbb, ppp, 1L));
        System.out.println(AccountNumberUtil.createSecuritiesAccountNumber(ppp, 1L));
    }
}
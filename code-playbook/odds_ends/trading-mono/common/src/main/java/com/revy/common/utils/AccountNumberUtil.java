package com.revy.common.utils;

public class AccountNumberUtil {
    private AccountNumberUtil() {
    }

    public static String createBankAccountNumber(String bbb, String ppp, long seq) {
        return withLuhnCheckDigit(String.format("%s%s%05d", bbb, ppp, seq));
    }

    public static String createSecuritiesAccountNumber(String ppp, long seq) {
        return withLuhnCheckDigit(String.format("%s%04d", ppp, seq));
    }


    /**
     * 숫자 문자열 뒤에 Luhn 체크디짓 1자리를 붙여 반환
     */
    public static String withLuhnCheckDigit(String digits) {
        int check = luhnCheckDigit(digits);
        return digits + check;
    }

    /**
     * Luhn 체크디짓 계산 (0~9)
     */
    public static int luhnCheckDigit(String digits) {
        int sum = 0;
        boolean doubleIt = true; // 오른쪽부터 번갈아 2배(체크디짓 제외 코어 기준)
        for (int i = digits.length() - 1; i >= 0; i--) {
            int d = digits.charAt(i) - '0';
            int add = d;
            if (doubleIt) {
                add = d * 2;
                if (add > 9) add -= 9;
            }
            sum += add;
            doubleIt = !doubleIt;
        }
        return (10 - (sum % 10)) % 10;
    }
}

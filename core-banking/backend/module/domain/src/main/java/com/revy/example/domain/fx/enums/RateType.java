package com.revy.example.domain.fx.enums;

import com.revy.example.common.enums.ExposedEnum;

public enum RateType implements ExposedEnum {
    MID,           // 매매기준율 (중간환율)
    BUY,           // 은행이 사는 가격 (고객 매도)
    SELL,          // 은행이 파는 가격 (고객 매수)
    CASH_BUY,      // 현찰 살때
    CASH_SELL,     // 현찰 팔때
    REMIT_BUY,     // 송금 받을때
    REMIT_SELL     // 송금 보낼때
}

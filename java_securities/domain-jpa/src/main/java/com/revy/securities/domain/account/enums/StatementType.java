package com.revy.securities.domain.account.enums;


/**
 * [도메인] 통장 내역 타입(StatementType)
 * <p>
 * - 사용자가 보는 "내역 1줄"의 분류 값
 * - 원장(Ledger)의 상세 라인(수수료/세금 등)을 UI/조회 관점에서 묶거나 구분하는 용도
 * - DB에는 문자열(EnumType.STRING)로 저장하여 가독성과 마이그레이션 안정성을 확보한다.
 */
public enum StatementType {
    /**
     * 입금(잔액 증가)
     */
    DEPOSIT,

    /**
     * 출금(잔액 감소)
     */
    WITHDRAW,

    /**
     * 이체-출금(내 계좌에서 빠져나감)
     */
    TRANSFER_OUT,

    /**
     * 이체-입금(내 계좌로 들어옴)
     */
    TRANSFER_IN,

    /**
     * 수수료(잔액 감소)
     */
    FEE,

    /**
     * 세금(잔액 감소)
     */
    TAX,

    /**
     * 주식 매수 정산(현금 감소)
     */
    TRADE_BUY_SETTLEMENT,

    /**
     * 주식 매도 정산(현금 증가)
     */
    TRADE_SELL_SETTLEMENT,

    /**
     * 배당금 입금(현금 증가)
     */
    DIVIDEND,

    /**
     * 이자 입금(현금 증가)
     */
    INTEREST,

    /**
     * 예수금/현금 조정(운영/정정 등 사유로 수동 보정)
     */
    ADJUSTMENT,

    /**
     * 거래 취소/정정으로 인한 되돌림(환불/리버설)
     */
    REVERSAL
}

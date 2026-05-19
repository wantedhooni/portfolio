package com.revy.example.portfolio.reader;

import com.revy.example.portfolio.reader.dto.PortfolioResult;

public interface PortfolioReader {

    /**
     * 계좌 보유 포지션 + 실현·미실현 손익 요약.
     * 미실현 손익은 Stock.lastPrice 기준 (배치 갱신값).
     */
    PortfolioResult getPortfolio(Long accountId);
}

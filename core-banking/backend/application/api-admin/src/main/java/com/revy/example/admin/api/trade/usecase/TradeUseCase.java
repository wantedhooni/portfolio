package com.revy.example.admin.api.trade.usecase;

import com.revy.example.admin.api.trade.payload.TradePayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;

/** 체결 내역 UseCase — 조회 + 배당금 처리 */
public interface TradeUseCase {

    TradePayload.ModelResponse get(Long id);

    ApiPageResponse<TradePayload.ModelResponse> search(Pageable pageable, TradePayload.SearchRequest searchRequest);

    /** 배당금 입금 처리 */
    void processDividend(TradePayload.DividendRequest request);
}

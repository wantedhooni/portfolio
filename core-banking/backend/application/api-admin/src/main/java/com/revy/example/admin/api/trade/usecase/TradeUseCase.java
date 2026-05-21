package com.revy.example.admin.api.trade.usecase;

import com.revy.example.admin.api.trade.payload.TradePayload;
import com.revy.example.core.common.ApiPageResponse;
import org.springframework.data.domain.Pageable;

/** 체결 내역 조회 전용 UseCase — 쓰기는 OrderUseCase 에서 처리 */
public interface TradeUseCase {

    TradePayload.ModelResponse get(Long id);

    ApiPageResponse<TradePayload.ModelResponse> search(Pageable pageable, TradePayload.SearchRequest searchRequest);
}

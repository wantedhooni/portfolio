package com.revy.example.trade.reader;

import com.revy.example.trade.reader.dto.TradeResult;
import com.revy.example.trade.reader.dto.TradeSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

/** business-logic 모듈의 trade read-side API — BUY / SELL 체결만 노출 */
public interface TradeReader {

    Optional<TradeResult> findById(Long id);

    Page<TradeResult> search(Pageable pageable, TradeSearchCondition condition);
}

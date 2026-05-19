package com.revy.example.stock.reader;

import com.revy.example.stock.reader.dto.StockResult;
import com.revy.example.stock.reader.dto.StockSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface StockReader {

    Optional<StockResult> findById(Long id);

    Optional<StockResult> findByTickerAndExchange(String ticker, String exchange);

    boolean existsByTickerAndExchange(String ticker, String exchange);

    Page<StockResult> search(Pageable pageable, StockSearchCondition condition);
}

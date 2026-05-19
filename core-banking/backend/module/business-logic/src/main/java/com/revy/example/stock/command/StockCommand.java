package com.revy.example.stock.command;

import com.revy.example.stock.command.dto.CreateStockCommand;
import com.revy.example.stock.command.dto.UpdateMarketDataCommand;

public interface StockCommand {

    Long createStock(CreateStockCommand command);

    /** 시세·시가총액 갱신 — 배치/스케줄러용 */
    void updateMarketData(UpdateMarketDataCommand command);

    /** 상장 폐지 — 이후 매수 차단, 매도는 허용 */
    void delist(Long stockId);
}

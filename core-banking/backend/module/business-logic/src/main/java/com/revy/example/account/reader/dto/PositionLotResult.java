package com.revy.example.account.reader.dto;

import com.revy.example.domain.account.PositionLot;
import com.revy.example.domain.account.enums.LotStatus;

import java.math.BigDecimal;
import java.time.Instant;

public record PositionLotResult(
        Long id,
        Long buyTxId,
        BigDecimal originalQuantity,
        BigDecimal remainingQuantity,
        BigDecimal buyPrice,
        Instant boughtAt,
        LotStatus lotStatus
) {

    public static PositionLotResult from(PositionLot lot) {
        return new PositionLotResult(
            lot.getId(),
            lot.getBuyTxId(),
            lot.getOriginalQuantity(),
            lot.getRemainingQuantity(),
            lot.getBuyPrice(),
            lot.getBoughtAt(),
            lot.getLotStatus()
        );
    }
}

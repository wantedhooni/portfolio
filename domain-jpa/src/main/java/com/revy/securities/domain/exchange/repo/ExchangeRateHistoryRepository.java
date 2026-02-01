package com.revy.securities.domain.exchange.repo;

import com.revy.securities.domain.exchange.ExRateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExchangeRateHistoryRepository extends JpaRepository<ExRateHistory, UUID> {
}
package com.revy.api_server.domain.exchange.repo;

import com.revy.api_server.domain.exchange.ExRateHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExchangeRateHistoryRepository extends JpaRepository<ExRateHistory, UUID> {
}
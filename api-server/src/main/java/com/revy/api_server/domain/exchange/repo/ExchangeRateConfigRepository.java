package com.revy.api_server.domain.exchange.repo;

import com.revy.api_server.domain.exchange.ExchangeConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ExchangeRateConfigRepository extends JpaRepository<ExchangeConfig, UUID> {
}
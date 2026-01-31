package com.revy.api_server.domain.exchange.repo;

import com.revy.api_server.domain.exchange.ExchangeRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface ExchangeRateRepository extends JpaRepository<ExchangeRate, UUID> {
}
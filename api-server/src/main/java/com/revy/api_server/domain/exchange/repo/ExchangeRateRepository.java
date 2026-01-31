package com.revy.api_server.domain.exchange.repo;

import com.revy.api_server.domain.exchange.ExRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface ExchangeRateRepository extends JpaRepository<ExRate, UUID> {
}
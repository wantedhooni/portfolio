package com.revy.securities.domain.exchange.repo;

import com.revy.securities.domain.exchange.ExRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;


public interface ExchangeRateRepository extends JpaRepository<ExRate, UUID> {
}
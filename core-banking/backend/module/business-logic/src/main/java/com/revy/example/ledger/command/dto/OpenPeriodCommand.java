package com.revy.example.ledger.command.dto;

import java.time.LocalDate;

public record OpenPeriodCommand(
        Integer fiscalYear,
        Integer fiscalPeriod,
        LocalDate startDate,
        LocalDate endDate
) {}

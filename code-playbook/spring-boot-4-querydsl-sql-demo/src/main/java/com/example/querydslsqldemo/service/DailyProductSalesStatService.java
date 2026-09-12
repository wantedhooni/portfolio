package com.example.querydslsqldemo.service;

import com.example.querydslsqldemo.repository.DailyProductSalesStatSqlRepository;
import com.example.querydslsqldemo.repository.DailyProductSalesStatSqlRepository.DailyProductSalesStatRow;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DailyProductSalesStatService {

    private final DailyProductSalesStatSqlRepository repository;

    public long countOrders() {
        return repository.countOrders();
    }

    @Transactional
    public RecreateDailyStatResult recreateDailyStat(LocalDate targetDate) {
        long deletedRows = repository.deleteDailyStat(targetDate);
        long insertedRows = repository.insertDailyStatByInsertSelect(targetDate);

        return new RecreateDailyStatResult(targetDate, deletedRows, insertedRows);
    }

    public long countDailyStat(LocalDate targetDate) {
        return repository.countDailyStat(targetDate);
    }

    public List<DailyProductSalesStatRow> findDailyStats(LocalDate targetDate, long limit) {
        return repository.findDailyStats(targetDate, limit);
    }

    public record RecreateDailyStatResult(
            LocalDate statDate,
            long deletedRows,
            long insertedRows
    ) {
    }
}

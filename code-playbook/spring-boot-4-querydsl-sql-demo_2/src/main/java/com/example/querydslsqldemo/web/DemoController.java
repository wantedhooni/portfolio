package com.example.querydslsqldemo.web;

import com.example.querydslsqldemo.batch.DailyProductSalesStatJobLauncher;
import com.example.querydslsqldemo.batch.DailyProductSalesStatJobLauncher.DailyProductSalesStatJobResult;
import com.example.querydslsqldemo.repository.DailyProductSalesStatJpaQueryRepository.DailyProductSalesStatRow;
import com.example.querydslsqldemo.service.DailyProductSalesStatService;
import com.example.querydslsqldemo.service.DailyProductSalesStatService.RecreateDailyStatResult;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/demo")
@RequiredArgsConstructor
public class DemoController {

    private final DailyProductSalesStatService service;
    private final DailyProductSalesStatJobLauncher jobLauncher;

    @GetMapping("/orders/count")
    public Map<String, Long> countOrders() {
        return Map.of("orderCount", service.countOrders());
    }

    @PostMapping("/stats/daily")
    public RecreateDailyStatResult recreateDailyStat(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return service.recreateDailyStat(date);
    }


    @PostMapping("/batch/stats/daily")
    public DailyProductSalesStatJobResult runDailyStatBatchJob(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return jobLauncher.run(date);
    }

    @GetMapping("/stats/daily/count")
    public Map<String, Long> countDailyStat(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date
    ) {
        return Map.of("statCount", service.countDailyStat(date));
    }

    @GetMapping("/stats/daily")
    public List<DailyProductSalesStatRow> findDailyStats(
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date,
            @RequestParam(defaultValue = "10")
            long limit
    ) {
        return service.findDailyStats(date, limit);
    }
}

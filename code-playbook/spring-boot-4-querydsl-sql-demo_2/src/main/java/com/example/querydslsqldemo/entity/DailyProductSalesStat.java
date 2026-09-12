package com.example.querydslsqldemo.entity;

import com.example.querydslsqldemo.repository.DailyProductSalesStatJpaQueryRepository.DailyProductSalesStatRow;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Entity
@Table(
        name = "daily_product_sales_stat",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_daily_product_sales_stat",
                        columnNames = {"stat_date", "product_id"}
                )
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyProductSalesStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "stat_date", nullable = false)
    private LocalDate statDate;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "total_amount", nullable = false, precision = 19, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "order_count", nullable = false)
    private Long orderCount;

    @Column(name = "created_at", nullable = false, insertable = false, updatable = false)
    private LocalDateTime createdAt;

    private DailyProductSalesStat(
            LocalDate statDate,
            Long productId,
            BigDecimal totalAmount,
            Long orderCount
    ) {
        this.statDate = statDate;
        this.productId = productId;
        this.totalAmount = totalAmount;
        this.orderCount = orderCount;
    }

    public static DailyProductSalesStat from(DailyProductSalesStatRow row) {
        return new DailyProductSalesStat(
                row.statDate(),
                row.productId(),
                row.totalAmount(),
                row.orderCount()
        );
    }
}

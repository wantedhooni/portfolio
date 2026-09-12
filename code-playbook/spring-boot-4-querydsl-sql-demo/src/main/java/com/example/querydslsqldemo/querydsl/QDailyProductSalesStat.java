package com.example.querydslsqldemo.querydsl;

import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DatePath;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.PrimaryKey;
import com.querydsl.sql.RelationalPathBase;
import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class QDailyProductSalesStat extends RelationalPathBase<QDailyProductSalesStat> {

    public static final QDailyProductSalesStat dailyProductSalesStat =
            new QDailyProductSalesStat("daily_product_sales_stat");

    public final NumberPath<Long> id = createNumber("id", Long.class);
    public final DatePath<LocalDate> statDate = createDate("stat_date", LocalDate.class);
    public final NumberPath<Long> productId = createNumber("product_id", Long.class);
    public final NumberPath<BigDecimal> totalAmount = createNumber("total_amount", BigDecimal.class);
    public final NumberPath<Long> orderCount = createNumber("order_count", Long.class);
    public final DateTimePath<LocalDateTime> createdAt = createDateTime("created_at", LocalDateTime.class);

    public final PrimaryKey<QDailyProductSalesStat> pkDailyProductSalesStat = createPrimaryKey(id);

    public QDailyProductSalesStat(String variable) {
        super(QDailyProductSalesStat.class, PathMetadataFactory.forVariable(variable), null, "daily_product_sales_stat");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(64).notNull());
        addMetadata(statDate, ColumnMetadata.named("stat_date").withIndex(2).ofType(Types.DATE).notNull());
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(3).ofType(Types.BIGINT).withSize(64).notNull());
        addMetadata(totalAmount, ColumnMetadata.named("total_amount").withIndex(4).ofType(Types.NUMERIC).withSize(19).withDigits(2).notNull());
        addMetadata(orderCount, ColumnMetadata.named("order_count").withIndex(5).ofType(Types.BIGINT).withSize(64).notNull());
        addMetadata(createdAt, ColumnMetadata.named("created_at").withIndex(6).ofType(Types.TIMESTAMP).withSize(26).notNull());
    }
}

package com.example.querydslsqldemo.querydsl;

import com.querydsl.core.types.PathMetadataFactory;
import com.querydsl.core.types.dsl.DateTimePath;
import com.querydsl.core.types.dsl.NumberPath;
import com.querydsl.core.types.dsl.StringPath;
import com.querydsl.sql.ColumnMetadata;
import com.querydsl.sql.RelationalPathBase;
import com.querydsl.sql.PrimaryKey;
import java.math.BigDecimal;
import java.sql.Types;
import java.time.LocalDateTime;

public class QOrders extends RelationalPathBase<QOrders> {

    public static final QOrders orders = new QOrders("orders");

    public final NumberPath<Long> id = createNumber("id", Long.class);
    public final DateTimePath<LocalDateTime> orderedAt = createDateTime("ordered_at", LocalDateTime.class);
    public final NumberPath<Long> productId = createNumber("product_id", Long.class);
    public final NumberPath<BigDecimal> amount = createNumber("amount", BigDecimal.class);
    public final StringPath status = createString("status");

    public final PrimaryKey<QOrders> pkOrders = createPrimaryKey(id);

    public QOrders(String variable) {
        super(QOrders.class, PathMetadataFactory.forVariable(variable), null, "orders");
        addMetadata();
    }

    private void addMetadata() {
        addMetadata(id, ColumnMetadata.named("id").withIndex(1).ofType(Types.BIGINT).withSize(64).notNull());
        addMetadata(orderedAt, ColumnMetadata.named("ordered_at").withIndex(2).ofType(Types.TIMESTAMP).withSize(26).notNull());
        addMetadata(productId, ColumnMetadata.named("product_id").withIndex(3).ofType(Types.BIGINT).withSize(64).notNull());
        addMetadata(amount, ColumnMetadata.named("amount").withIndex(4).ofType(Types.NUMERIC).withSize(19).withDigits(2).notNull());
        addMetadata(status, ColumnMetadata.named("status").withIndex(5).ofType(Types.VARCHAR).withSize(20).notNull());
    }
}

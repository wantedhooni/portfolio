package com.revy.example.domain;

import com.revy.example.domain.core.BaseEntity;
import com.revy.example.domain.embedded.DeviceInfoEmbeddable;
import com.revy.example.domain.embedded.GeoLocationEmbeddable;
import com.revy.example.domain.embedded.MoneyEmbeddable;
import com.revy.example.enums.TransactionStatus;
import com.revy.example.enums.TransactionType;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(
    name = "transactions",
    schema = "fds",
    indexes = {
        @Index(name = "idx_txn_account_id", columnList = "account_id"),
        @Index(name = "idx_txn_created_at", columnList = "created_at"),
        @Index(name = "idx_txn_status", columnList = "status"),
        @Index(name = "idx_txn_type_created", columnList = "transaction_type, created_at")
    }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Transaction extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "transaction_id", columnDefinition = "uuid")
    private UUID transactionId;

    @Column(name = "account_id", nullable = false)
    private UUID accountId;

    @Column(name = "counterpart_account_id")
    private UUID counterpartAccountId;

    @Enumerated(EnumType.STRING)
    @Column(name = "transaction_type", nullable = false, length = 30)
    private TransactionType transactionType;

    // Money VO → Embeddable로 처리
    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "amount",   column = @Column(name = "amount", precision = 20, scale = 2)),
        @AttributeOverride(name = "currency", column = @Column(name = "currency", length = 3))
    })
    private MoneyEmbeddable amount;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private TransactionStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "countryCode", column = @Column(name = "country_code", length = 2)),
        @AttributeOverride(name = "city",        column = @Column(name = "city", length = 100)),
        @AttributeOverride(name = "latitude",    column = @Column(name = "latitude")),
        @AttributeOverride(name = "longitude",   column = @Column(name = "longitude"))
    })
    private GeoLocationEmbeddable geoLocation;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "deviceId",   column = @Column(name = "device_id", length = 100)),
        @AttributeOverride(name = "deviceType", column = @Column(name = "device_type", length = 30)),
        @AttributeOverride(name = "ipAddress",  column = @Column(name = "ip_address", length = 45)),
        @AttributeOverride(name = "userAgent",  column = @Column(name = "user_agent", length = 500))
    })
    private DeviceInfoEmbeddable deviceInfo;

    @Column(name = "channel", length = 20)
    private String channel; // MOBILE, WEB, ATM, BRANCH

    @Column(name = "description", length = 200)
    private String description;

    // 정적 팩토리
    public static Transaction create(
        UUID accountId,
        TransactionType type,
        MoneyEmbeddable amount,
        String channel
    ) {
        Transaction txn = new Transaction();
        txn.transactionId = UUID.randomUUID();
        txn.accountId = accountId;
        txn.transactionType = type;
        txn.amount = amount;
        txn.status = TransactionStatus.PENDING;
        txn.channel = channel;
        return txn;
    }

    public void approve() {
        if (this.status != TransactionStatus.PENDING) {
            throw new IllegalStateException("Only PENDING transactions can be approved");
        }
        this.status = TransactionStatus.APPROVED;
    }

    public void block(String reason) {
        this.status = TransactionStatus.BLOCKED;
    }
}
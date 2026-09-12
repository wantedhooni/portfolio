package com.revy.domain.base;

import com.revy.common.utils.UuidUtils;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;


@ToString
@Getter
@NoArgsConstructor
@MappedSuperclass
@EntityListeners({AuditingEntityListener.class})
public class BaseEntity implements Serializable {
    @Id
    @Column(name = "id")
    protected UUID id;

    @CreatedDate
    @Column(name = "created_at", updatable = false)
    protected LocalDateTime createdAt = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "updated_at")
    protected LocalDateTime updatedAt;
    @PrePersist
    void prePersist() {
        onPrePersist();
    }
    protected void onPrePersist() {
        if (id == null) {
            id = UuidUtils.getTimeOrderedEpochUuidV7();
        }
    }
}

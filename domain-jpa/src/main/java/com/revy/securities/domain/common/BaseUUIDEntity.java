package com.revy.securities.domain.common;

import com.revy.common.utils.UuidUtils;
import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.util.UUID;


@Getter
@MappedSuperclass
@ToString
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseUUIDEntity extends BaseTimeField {
    @Id
    @Column(name = "id")
    protected UUID id;

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

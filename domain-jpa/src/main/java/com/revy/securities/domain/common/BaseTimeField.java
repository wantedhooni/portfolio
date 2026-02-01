package com.revy.securities.domain.common;

import jakarta.persistence.Column;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.ToString;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Getter
@MappedSuperclass
@ToString
@EntityListeners(AuditingEntityListener.class)
public class BaseTimeField {
    @CreatedDate
    @Column(name = "created_date", updatable = false)
    protected LocalDateTime createdDate = LocalDateTime.now();

    @LastModifiedDate
    @Column(name = "last_modified_date")
    protected LocalDateTime lastModifiedDate = LocalDateTime.now();
}

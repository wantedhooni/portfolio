package com.revy.trading.adapter.output.persistence.entity.common

import com.revy.trading.utils.UuidUtil
import jakarta.persistence.Column
import jakarta.persistence.EntityListeners
import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.Hibernate
import org.springframework.data.annotation.CreatedDate
import org.springframework.data.annotation.LastModifiedDate
import org.springframework.data.jpa.domain.support.AuditingEntityListener
import java.time.Instant
import java.util.*


@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity {
    @Id
    @Column(name = "id", nullable = false, columnDefinition = "uuid")
    var id: UUID? = UuidUtil.generateUuidV7()
        protected set

    @CreatedDate
    @Column(name="created_at", nullable = false, updatable = false, columnDefinition = "TIMESTAMP")
    var createdAt: Instant = Instant.now()
        protected set

    @LastModifiedDate
    @Column(name="updated_at",nullable = false, columnDefinition = "TIMESTAMP")
    var updatedAt: Instant = Instant.now()
        protected set

    final override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other == null) return false
        // 프록시 대응: 실제 클래스 비교
        val thisClass = Hibernate.getClass(this)
        val otherClass = Hibernate.getClass(other)
        if (thisClass != otherClass) return false
        other as BaseEntity
        return id != null && id == other.id
    }

    // ★ 상수 반환: 영속화 전(id=null) → 영속화 후(id 할당) 해시가 바뀌면
    //   HashSet/HashMap에서 엔티티를 잃어버림
    final override fun hashCode(): Int = Hibernate.getClass(this).hashCode()
}
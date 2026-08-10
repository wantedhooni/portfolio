package com.revy.domain.entity.common

import jakarta.persistence.Id
import jakarta.persistence.MappedSuperclass
import org.hibernate.Hibernate
import java.util.UUID

@MappedSuperclass
abstract class BaseEntity {
    @Id
     val id: UUID? = null

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
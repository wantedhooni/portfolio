package com.revy.trading.adapter.output.persistence.entity.common

import jakarta.persistence.MappedSuperclass
import jakarta.persistence.Version

/**
 * 주문이나 잔고처럼 동시성 충돌을 검출해야 하는 엔티티에 사용한다.
 */
@MappedSuperclass
abstract class VersionedEntity : BaseEntity() {

    @Version
    var version: Long = 0
        protected set
}
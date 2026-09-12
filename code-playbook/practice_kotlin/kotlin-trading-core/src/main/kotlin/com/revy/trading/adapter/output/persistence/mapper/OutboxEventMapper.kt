package com.revy.trading.adapter.output.persistence.mapper

import com.revy.trading.adapter.output.persistence.entity.OutboxEventEntity
import com.revy.trading.adapter.output.persistence.entity.enums.OutboxStatus
import com.revy.trading.utils.UuidUtil
import java.util.UUID

fun toOutboxEventEntity(
    aggregateId: UUID,
    eventType: String,
    payload: String,
): OutboxEventEntity = OutboxEventEntity(
    eventId = UuidUtil.generateUuidV7(),
    aggregateId = aggregateId,
    eventType = eventType,
    payload = payload,
    status = OutboxStatus.PENDING,
)

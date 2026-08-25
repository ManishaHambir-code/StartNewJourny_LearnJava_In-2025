package com.scp.java.ocm.common.event;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

import lombok.Getter;

@Getter
public class DomainEvent {
    private final String eventId;
    private final String eventType;
    private final String aggregateType;
    private final String aggregateId;
    private final LocalDateTime occurredAt;
    private final Map<String, Object> payload;

    public DomainEvent(String eventType, String aggregateType, String aggregateId, Map<String, Object> payload) {
        this(UUID.randomUUID().toString(), eventType, aggregateType, aggregateId, LocalDateTime.now(), payload);
    }

    public DomainEvent(String eventId, String eventType, String aggregateType, String aggregateId,
            LocalDateTime occurredAt, Map<String, Object> payload) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.aggregateType = aggregateType;
        this.aggregateId = aggregateId;
        this.occurredAt = occurredAt;
        this.payload = payload;
    }
}

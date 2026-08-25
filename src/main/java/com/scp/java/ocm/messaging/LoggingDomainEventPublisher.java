package com.scp.java.ocm.messaging;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import com.scp.java.ocm.common.event.DomainEvent;
import com.scp.java.ocm.common.event.DomainEventPublisher;

@Component
@ConditionalOnProperty(name = "ocm.kafka.enabled", havingValue = "false", matchIfMissing = true)
public class LoggingDomainEventPublisher implements DomainEventPublisher {
    private static final Logger LOGGER = LoggerFactory.getLogger(LoggingDomainEventPublisher.class);

    @Override
    public void publish(DomainEvent event) {
        LOGGER.info("OCM domain event: type={}, aggregateType={}, aggregateId={}, payload={}",
                event.getEventType(), event.getAggregateType(), event.getAggregateId(), event.getPayload());
    }
}

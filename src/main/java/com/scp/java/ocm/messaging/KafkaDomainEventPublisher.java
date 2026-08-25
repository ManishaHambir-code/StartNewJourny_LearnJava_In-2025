package com.scp.java.ocm.messaging;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import com.scp.java.ocm.common.event.DomainEvent;
import com.scp.java.ocm.common.event.DomainEventPublisher;

@Component
@ConditionalOnProperty(name = "ocm.kafka.enabled", havingValue = "true")
public class KafkaDomainEventPublisher implements DomainEventPublisher {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final KafkaTopicProperties topics;

    public KafkaDomainEventPublisher(KafkaTemplate<String, Object> kafkaTemplate, KafkaTopicProperties topics) {
        this.kafkaTemplate = kafkaTemplate;
        this.topics = topics;
    }

    @Override
    public void publish(DomainEvent event) {
        kafkaTemplate.send(topics.topicFor(event.getAggregateType()), event.getAggregateId(), event);
    }
}

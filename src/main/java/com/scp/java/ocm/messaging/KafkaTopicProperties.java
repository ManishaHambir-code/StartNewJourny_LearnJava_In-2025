package com.scp.java.ocm.messaging;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "ocm.kafka.topics")
public class KafkaTopicProperties {
    private String memberEvents = "ocm.member-events";
    private String carePlanEvents = "ocm.care-plan-events";
    private String claimEvents = "ocm.claim-events";

    public String topicFor(String aggregateType) {
        if ("Member".equalsIgnoreCase(aggregateType)) {
            return memberEvents;
        }
        if ("CarePlan".equalsIgnoreCase(aggregateType)) {
            return carePlanEvents;
        }
        return claimEvents;
    }
}

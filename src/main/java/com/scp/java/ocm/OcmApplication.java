package com.scp.java.ocm;

import com.scp.java.ocm.messaging.KafkaTopicProperties;
import com.scp.java.ocm.security.JwtProperties;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({JwtProperties.class, KafkaTopicProperties.class})
public class OcmApplication {

    public static void main(String[] args) {
        new SpringApplicationBuilder(OcmApplication.class).profiles("ocm").run(args);
    }
}

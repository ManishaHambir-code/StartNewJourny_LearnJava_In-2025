package com.scp.java.ocm.security;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "ocm.security.jwt")
public class JwtProperties {
    private String secret = "change-this-development-secret-at-least-32-chars";
    private long expirationMs = 3600000L;
}

package com.scp.java.ocm.security;

import org.springframework.boot.context.properties.ConfigurationProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@ConfigurationProperties(prefix = "ocm.security.jwt")
public class JwtProperties {
    private String secret = "change-this-development-secret-at-least-32-chars";
    private long expirationMs = 3600000L;
}

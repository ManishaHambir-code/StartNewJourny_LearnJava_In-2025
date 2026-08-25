package com.scp.java.ocm.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

public class JwtTokenProviderTest {
    private final JwtProperties properties = properties(60000);

    @Test
    void validTokenRoundTrip() {
        JwtTokenProvider provider = new JwtTokenProvider(properties);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(
                        "admin", "x", Collections.singleton(new SimpleGrantedAuthority("ROLE_ADMIN")));
        String token = provider.createToken(auth);
        assertTrue(provider.validateToken(token));
        assertEquals("admin", provider.getUsername(token));
    }

    @Test
    void tamperedTokenIsRejected() {
        JwtTokenProvider provider = new JwtTokenProvider(properties);
        assertFalse(provider.validateToken("not.a.valid.token"));
    }

    @Test
    void expiredTokenIsRejected() throws Exception {
        JwtProperties expiring = properties(1);
        JwtTokenProvider provider = new JwtTokenProvider(expiring);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken("admin", "x");
        String token = provider.createToken(auth);
        Thread.sleep(20L);
        assertFalse(provider.validateToken(token));
    }

    @Test
    void weakSecretFailsFast() {
        JwtProperties weak = new JwtProperties();
        weak.setSecret("short");
        assertThrows(IllegalArgumentException.class, () -> new JwtTokenProvider(weak));
    }

    private JwtProperties properties(long expiration) {
        JwtProperties p = new JwtProperties();
        p.setExpirationMs(expiration);
        return p;
    }
}

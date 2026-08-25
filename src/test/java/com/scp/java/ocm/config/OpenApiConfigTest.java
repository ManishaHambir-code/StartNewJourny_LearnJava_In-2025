package com.scp.java.ocm.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.junit.jupiter.api.Test;

public class OpenApiConfigTest {
    @Test
    void exposesBearerAuthenticationScheme() {
        OpenAPI openApi = new OpenApiConfig().ocmOpenAPI();
        SecurityScheme scheme =
                openApi.getComponents().getSecuritySchemes().get("bearerAuth");

        assertNotNull(scheme);
        assertEquals(SecurityScheme.Type.HTTP, scheme.getType());
        assertEquals("bearer", scheme.getScheme());
        assertEquals("JWT", scheme.getBearerFormat());
        assertTrue(openApi.getSecurity().get(0).containsKey("bearerAuth"));
    }
}

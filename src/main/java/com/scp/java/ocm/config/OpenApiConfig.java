package com.scp.java.ocm.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {
    @Bean
    public OpenAPI ocmOpenAPI() {
        return new OpenAPI()
                .info(
                        new Info()
                                .title("Optum OCM Healthcare Management API")
                                .version("v1")
                                .description("Care management, member, provider and claims services"));
    }
}

package com.slp.studentservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI studentServiceOpenApi() {
        return new OpenAPI().info(new Info()
                .title("Student Service API")
                .description("Student registration and profile management for the Student Learning Platform")
                .version("v1")
                .contact(new Contact().name("Student Learning Platform")));
    }
}

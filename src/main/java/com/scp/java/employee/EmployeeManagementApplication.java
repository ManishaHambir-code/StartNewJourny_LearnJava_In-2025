package com.scp.java.employee;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@SpringBootApplication(exclude = SecurityAutoConfiguration.class)
public class EmployeeManagementApplication {

    @Bean
    public SecurityFilterChain employeeSecurity(HttpSecurity http) throws Exception {
        return http.csrf().disable().authorizeRequests().anyRequest().permitAll().and().build();
    }

    public static void main(String[] args) {
        SpringApplication.run(EmployeeManagementApplication.class, args);
    }
}

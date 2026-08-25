package com.scp.java.ocm.provider.controller;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.scp.java.ocm.provider.service.ProviderService;
import com.scp.java.ocm.security.SecurityConfig;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = ProviderController.class,
        excludeFilters =
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
public class ProviderControllerTest {
    @Autowired MockMvc mvc;
    @MockBean ProviderService service;

    @Test
    void invalidEnumReturnsFieldErrorDetails() throws Exception {
        mvc.perform(
                        post("/api/v1/providers")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(
                                        "{\"npi\":\"1234567890\",\"firstName\":\"A\","
                                                + "\"lastName\":\"B\",\"specialty\":\"CARE_MANAGEMENT\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for request field"))
                .andExpect(
                        jsonPath("$.fieldErrors.specialty")
                                .value(
                                        Matchers.containsString(
                                                "Invalid value 'CARE_MANAGEMENT' for type ProviderSpecialty")))
                .andExpect(
                        jsonPath("$.fieldErrors.specialty")
                                .value(
                                        Matchers.containsString(
                                                "Allowed values: PRIMARY_CARE, CARDIOLOGY, ONCOLOGY")));
    }
}

package com.scp.java.ocm.claim.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.scp.java.ocm.claim.dto.ClaimAdjudicationRequest;
import com.scp.java.ocm.claim.service.ClaimService;
import com.scp.java.ocm.common.exception.BusinessRuleViolationException;
import com.scp.java.ocm.security.SecurityConfig;
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
        controllers = ClaimController.class,
        excludeFilters =
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
public class ClaimControllerTest {
    @Autowired MockMvc mvc;
    @MockBean ClaimService service;

    @Test
    void adjudicateReturnsResponse() throws Exception {
        when(service.adjudicate(eq(1L), any(ClaimAdjudicationRequest.class))).thenReturn(null);
        mvc.perform(
                        post("/api/v1/claims/1/adjudicate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"APPROVED\",\"allowedAmount\":10}"))
                .andExpect(status().isOk());
    }

    @Test
    void businessRuleMapsTo422() throws Exception {
        when(service.adjudicate(eq(1L), any(ClaimAdjudicationRequest.class)))
                .thenThrow(new BusinessRuleViolationException("Only submitted claims can be adjudicated"));
        mvc.perform(
                        post("/api/v1/claims/1/adjudicate")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content("{\"status\":\"DENIED\",\"denialReason\":\"duplicate\"}"))
                .andExpect(status().isUnprocessableEntity())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("Only submitted claims can be adjudicated"))
                .andExpect(jsonPath("$.path").value("/api/v1/claims/1/adjudicate"));
    }
}

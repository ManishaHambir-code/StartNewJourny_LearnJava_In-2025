package com.scp.java.ocm.claim.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.*;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import com.scp.java.ocm.claim.dto.*;
import com.scp.java.ocm.claim.entity.ClaimStatus;
import com.scp.java.ocm.claim.service.ClaimService;
import com.scp.java.ocm.common.exception.BusinessRuleViolationException;

@WebMvcTest(ClaimController.class)
@AutoConfigureMockMvc(addFilters = false)
public class ClaimControllerTest {
    @Autowired MockMvc mvc;
    @MockBean ClaimService service;

    @Test void adjudicateReturnsResponse() throws Exception {
        when(service.adjudicate(eq(1L), any(ClaimAdjudicationRequest.class))).thenReturn(null);
        mvc.perform(post("/api/v1/claims/1/adjudicate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"APPROVED\",\"allowedAmount\":10}")).andExpect(status().isOk());
    }

    @Test void businessRuleMapsTo422() throws Exception {
        when(service.adjudicate(eq(1L), any(ClaimAdjudicationRequest.class)))
                .thenThrow(new BusinessRuleViolationException("Only submitted claims can be adjudicated"));
        mvc.perform(post("/api/v1/claims/1/adjudicate").contentType(MediaType.APPLICATION_JSON)
                .content("{\"status\":\"DENIED\",\"denialReason\":\"duplicate\"}"))
                .andExpect(status().isUnprocessableEntity()).andExpect(jsonPath("$.status").value(422));
    }
}

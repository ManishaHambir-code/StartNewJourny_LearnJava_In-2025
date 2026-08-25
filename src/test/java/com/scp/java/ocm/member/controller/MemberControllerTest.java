package com.scp.java.ocm.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.scp.java.ocm.member.dto.MemberRequest;
import com.scp.java.ocm.member.dto.MemberResponse;
import com.scp.java.ocm.member.entity.Gender;
import com.scp.java.ocm.member.entity.MemberStatus;
import com.scp.java.ocm.member.service.MemberService;
import com.scp.java.ocm.security.SecurityConfig;
import java.time.LocalDate;
import java.util.Collections;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(
        controllers = MemberController.class,
        excludeFilters =
                @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = SecurityConfig.class))
@AutoConfigureMockMvc(addFilters = false)
public class MemberControllerTest {
    @Autowired MockMvc mvc;
    @MockBean MemberService service;

    @Test
    void createReturns201AndLocation() throws Exception {
        MemberResponse response =
                new MemberResponse(
                        1L,
                        "MRN1",
                        "A",
                        "B",
                        LocalDate.of(1980, 1, 1),
                        Gender.OTHER,
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        "P1",
                        MemberStatus.ACTIVE,
                        LocalDate.now());
        when(service.create(any(MemberRequest.class))).thenReturn(response);
        mvc.perform(
                        post("/api/v1/members").contentType(MediaType.APPLICATION_JSON).content(validJson()))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/v1/members/1"));
    }

    @Test
    void invalidPayloadReturnsFieldErrors() throws Exception {
        mvc.perform(post("/api/v1/members").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors").exists())
                .andExpect(jsonPath("$.fieldErrors.mrn").exists());
    }

    @Test
    void pagingParametersAreAccepted() throws Exception {
        when(service.findAll(any(), any(), any(), any()))
                .thenReturn(
                        new PageImpl<MemberResponse>(
                                Collections.<MemberResponse>emptyList(), PageRequest.of(1, 5), 0));
        mvc.perform(get("/api/v1/members?page=1&size=5&sort=lastName,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.page").value(1))
                .andExpect(jsonPath("$.size").value(5));
    }

    @Test
    void unknownSortFieldReturnsBadRequest() throws Exception {
        mvc.perform(get("/api/v1/members?sort=bogus,asc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid sort parameter"))
                .andExpect(jsonPath("$.fieldErrors.sort").value("Unknown sort field: bogus"));
    }

    @Test
    void oversizedPageReturnsFieldErrorDetails() throws Exception {
        mvc.perform(get("/api/v1/members?size=101"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.size").value("must be less than or equal to 100"));
    }

    private String validJson() {
        return "{\"mrn\":\"MRN1\",\"firstName\":\"A\",\"lastName\":\"B\",\"dateOfBirth\":\"1980-01-01\","
               + "\"gender\":\"OTHER\",\"planId\":\"P1\",\"enrollmentDate\":\""
                + LocalDate.now()
                + "\"}";
    }
}

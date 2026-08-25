package com.scp.java.employee.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.scp.java.employee.dto.EmployeeDto;
import com.scp.java.employee.exception.DuplicateEmailException;
import com.scp.java.employee.exception.EmployeeNotFoundException;
import com.scp.java.employee.service.EmployeeService;

@WebMvcTest(EmployeeController.class)
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EmployeeService employeeService;

    private EmployeeDto dto(Long id, String email) {
        return new EmployeeDto(id, "Manisha", "Hambir", email, "Engineering", new BigDecimal("75000.00"),
                LocalDate.of(2025, 1, 10));
    }

    @Test
    void createReturns201WithLocation() throws Exception {
        when(employeeService.create(any(EmployeeDto.class))).thenReturn(dto(1L, "manisha@example.com"));

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(null, "manisha@example.com"))))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "/api/employees/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("manisha@example.com"));
    }

    @Test
    void createReturns400OnValidationFailure() throws Exception {
        EmployeeDto invalid = dto(null, "not-an-email");
        invalid.setFirstName("");

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.fieldErrors.firstName").exists())
                .andExpect(jsonPath("$.fieldErrors.email").exists());
    }

    @Test
    void createReturns409OnDuplicateEmail() throws Exception {
        when(employeeService.create(any(EmployeeDto.class)))
                .thenThrow(new DuplicateEmailException("manisha@example.com"));

        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(null, "manisha@example.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }

    @Test
    void findAllReturnsList() throws Exception {
        when(employeeService.findAll()).thenReturn(Collections.singletonList(dto(1L, "manisha@example.com")));

        mockMvc.perform(get("/api/employees")).andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    void findByIdReturns404WhenMissing() throws Exception {
        when(employeeService.findById(99L)).thenThrow(new EmployeeNotFoundException(99L));

        mockMvc.perform(get("/api/employees/99")).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Employee not found with id: 99"));
    }

    @Test
    void updateReturns200() throws Exception {
        when(employeeService.update(eq(1L), any(EmployeeDto.class))).thenReturn(dto(1L, "new@example.com"));

        mockMvc.perform(put("/api/employees/1").contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(dto(null, "new@example.com"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void deleteReturns204() throws Exception {
        doNothing().when(employeeService).delete(1L);

        mockMvc.perform(delete("/api/employees/1")).andExpect(status().isNoContent());
    }

    @Test
    void nonNumericIdReturns400() throws Exception {
        mockMvc.perform(get("/api/employees/abc")).andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Invalid value for parameter 'id'"));
    }

    @Test
    void malformedJsonReturns400() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON).content("{\"firstName\": \"A\", "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void unparseableDateReturns400() throws Exception {
        mockMvc.perform(post("/api/employees").contentType(MediaType.APPLICATION_JSON)
                .content("{\"firstName\":\"Manisha\",\"lastName\":\"Hambir\",\"email\":\"m@example.com\","
                        + "\"department\":\"Engineering\",\"salary\":1,\"dateOfJoining\":\"01-01-2020\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request body is missing or malformed"));
    }

    @Test
    void unsupportedMethodReturns405() throws Exception {
        mockMvc.perform(patch("/api/employees/1").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isMethodNotAllowed())
                .andExpect(jsonPath("$.message").value("Request method 'PATCH' is not supported"));
    }

    @Test
    void deleteReturns404WhenMissing() throws Exception {
        doThrow(new EmployeeNotFoundException(7L)).when(employeeService).delete(7L);

        mockMvc.perform(delete("/api/employees/7")).andExpect(status().isNotFound());
    }
}

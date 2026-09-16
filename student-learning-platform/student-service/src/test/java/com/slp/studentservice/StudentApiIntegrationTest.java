package com.slp.studentservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slp.studentservice.dto.StudentRequest;
import com.slp.studentservice.entity.StudentStatus;
import com.slp.studentservice.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * End-to-end test through controller -> service -> JPA repository -> H2.
 */
@SpringBootTest
@AutoConfigureMockMvc
class StudentApiIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private StudentRepository studentRepository;

    @BeforeEach
    void cleanDatabase() {
        studentRepository.deleteAll();
    }

    private StudentRequest request(String firstName, String email) {
        return StudentRequest.builder()
                .firstName(firstName).lastName("Tester").email(email)
                .phone("9876543210").password("Secret@123").status(StudentStatus.ACTIVE).build();
    }

    private long create(StudentRequest req) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req)))
                .andExpect(status().isCreated())
                .andReturn();
        return objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asLong();
    }

    @Test
    void fullCrudLifecycle() throws Exception {
        long id = create(request("Manisha", "manisha@example.com"));

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("manisha@example.com"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.password").doesNotExist());

        StudentRequest update = request("Manisha", "manisha.new@example.com");
        update.setStatus(StudentStatus.INACTIVE);
        mockMvc.perform(put("/api/students/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("manisha.new@example.com"))
                .andExpect(jsonPath("$.status").value("INACTIVE"));

        mockMvc.perform(delete("/api/students/{id}", id))
                .andExpect(status().isNoContent());

        mockMvc.perform(get("/api/students/{id}", id))
                .andExpect(status().isNotFound());

        assertThat(studentRepository.count()).isZero();
    }

    @Test
    void duplicateEmailReturns409() throws Exception {
        create(request("Manisha", "dup@example.com"));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request("Other", "dup@example.com"))))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Email already exists: dup@example.com"));
    }

    @Test
    void paginationAndSortingWork() throws Exception {
        create(request("Zara", "zara@example.com"));
        create(request("Amit", "amit@example.com"));
        create(request("Manisha", "manisha@example.com"));

        mockMvc.perform(get("/api/students").param("page", "0").param("size", "2").param("sort", "firstName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].firstName").value("Amit"))
                .andExpect(jsonPath("$.content[1].firstName").value("Manisha"))
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2))
                .andExpect(jsonPath("$.last").value(false));

        mockMvc.perform(get("/api/students").param("page", "1").param("size", "2").param("sort", "firstName,desc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].firstName").value("Amit"))
                .andExpect(jsonPath("$.last").value(true));
    }

    @Test
    void invalidSortPropertyReturns400() throws Exception {
        mockMvc.perform(get("/api/students").param("sort", "doesNotExist,asc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void validationErrorsReturn400WithFieldMessages() throws Exception {
        StudentRequest invalid = request("M", "bad-email");
        invalid.setPassword("123");

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.validationErrors.firstName").value("First name must be between 2 and 50 characters"))
                .andExpect(jsonPath("$.validationErrors.email").value("Email must be valid"))
                .andExpect(jsonPath("$.validationErrors.password").value("Password must be between 8 and 72 characters"));
    }
}

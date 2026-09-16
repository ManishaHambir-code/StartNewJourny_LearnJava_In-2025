package com.slp.studentservice.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.slp.studentservice.dto.PageResponse;
import com.slp.studentservice.dto.StudentRequest;
import com.slp.studentservice.dto.StudentResponse;
import com.slp.studentservice.entity.StudentStatus;
import com.slp.studentservice.exception.EmailAlreadyExistsException;
import com.slp.studentservice.exception.StudentNotFoundException;
import com.slp.studentservice.service.StudentService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.hasKey;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(StudentController.class)
class StudentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private StudentService studentService;

    private final StudentResponse response = StudentResponse.builder()
            .id(1L).firstName("Manisha").lastName("Hambir").email("manisha@example.com")
            .phone("+919876543210").status(StudentStatus.ACTIVE).build();

    private StudentRequest validRequest() {
        return StudentRequest.builder()
                .firstName("Manisha").lastName("Hambir").email("manisha@example.com")
                .phone("+919876543210").password("Secret@123").status(StudentStatus.ACTIVE).build();
    }

    @Test
    void createStudentReturns201WithLocation() throws Exception {
        when(studentService.createStudent(any(StudentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isCreated())
                .andExpect(header().string("Location", "http://localhost/api/students/1"))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("manisha@example.com"))
                .andExpect(jsonPath("$.password").doesNotExist());
    }

    @Test
    void createStudentReturns400OnValidationFailure() throws Exception {
        StudentRequest invalid = StudentRequest.builder()
                .firstName("").lastName("H").email("not-an-email").password("short").build();

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalid)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.message").value("Validation failed"))
                .andExpect(jsonPath("$.validationErrors", hasKey("firstName")))
                .andExpect(jsonPath("$.validationErrors", hasKey("lastName")))
                .andExpect(jsonPath("$.validationErrors", hasKey("email")))
                .andExpect(jsonPath("$.validationErrors", hasKey("password")))
                .andExpect(jsonPath("$.validationErrors", hasKey("status")));
    }

    @Test
    void createStudentReturns400OnMalformedJson() throws Exception {
        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"firstName\": "))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
    }

    @Test
    void createStudentReturns409OnDuplicateEmail() throws Exception {
        when(studentService.createStudent(any(StudentRequest.class)))
                .thenThrow(new EmailAlreadyExistsException("manisha@example.com"));

        mockMvc.perform(post("/api/students")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.message").value("Email already exists: manisha@example.com"))
                .andExpect(jsonPath("$.path").value("/api/students"));
    }

    @Test
    void getAllStudentsPassesPaginationAndSorting() throws Exception {
        PageResponse<StudentResponse> page = PageResponse.<StudentResponse>builder()
                .content(List.of(response)).page(0).size(10).totalElements(1).totalPages(1).last(true).build();
        when(studentService.getAllStudents(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/students").param("page", "0").param("size", "10").param("sort", "firstName,asc"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].id").value(1))
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.last").value(true));

        ArgumentCaptor<Pageable> captor = ArgumentCaptor.forClass(Pageable.class);
        verify(studentService).getAllStudents(captor.capture());
        Pageable pageable = captor.getValue();
        assertThat(pageable.getPageNumber()).isZero();
        assertThat(pageable.getPageSize()).isEqualTo(10);
        assertThat(pageable.getSort().getOrderFor("firstName")).isNotNull();
        assertThat(pageable.getSort().getOrderFor("firstName").isAscending()).isTrue();
    }

    @Test
    void getStudentByIdReturns200() throws Exception {
        when(studentService.getStudentById(1L)).thenReturn(response);

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Manisha"));
    }

    @Test
    void getStudentByIdReturns404WhenMissing() throws Exception {
        when(studentService.getStudentById(99L)).thenThrow(new StudentNotFoundException(99L));

        mockMvc.perform(get("/api/students/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Student not found with id: 99"))
                .andExpect(jsonPath("$.path").value("/api/students/99"));
    }

    @Test
    void getStudentByIdReturns400ForNonNumericId() throws Exception {
        mockMvc.perform(get("/api/students/abc"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void updateStudentReturns200() throws Exception {
        when(studentService.updateStudent(eq(1L), any(StudentRequest.class))).thenReturn(response);

        mockMvc.perform(put("/api/students/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1));
    }

    @Test
    void updateStudentReturns404WhenMissing() throws Exception {
        when(studentService.updateStudent(eq(5L), any(StudentRequest.class)))
                .thenThrow(new StudentNotFoundException(5L));

        mockMvc.perform(put("/api/students/5")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validRequest())))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteStudentReturns204() throws Exception {
        mockMvc.perform(delete("/api/students/1"))
                .andExpect(status().isNoContent());

        verify(studentService).deleteStudent(1L);
    }

    @Test
    void deleteStudentReturns404WhenMissing() throws Exception {
        doThrow(new StudentNotFoundException(3L)).when(studentService).deleteStudent(3L);

        mockMvc.perform(delete("/api/students/3"))
                .andExpect(status().isNotFound());
    }

    @Test
    void unexpectedExceptionReturns500WithoutDetails() throws Exception {
        when(studentService.getStudentById(1L)).thenThrow(new IllegalStateException("secret internal detail"));

        mockMvc.perform(get("/api/students/1"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.message").value("An unexpected error occurred"));
    }
}

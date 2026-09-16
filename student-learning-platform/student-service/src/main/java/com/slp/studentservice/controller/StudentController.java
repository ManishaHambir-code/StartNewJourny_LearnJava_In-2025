package com.slp.studentservice.controller;

import com.slp.studentservice.dto.ErrorResponse;
import com.slp.studentservice.dto.PageResponse;
import com.slp.studentservice.dto.StudentRequest;
import com.slp.studentservice.dto.StudentResponse;
import com.slp.studentservice.service.StudentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;

@Slf4j
@RestController
@RequestMapping("/api/students")
@Tag(name = "Students", description = "Student registration and profile management")
public class StudentController {

    private static final String STUDENT_REQUEST_EXAMPLE = """
            {
              "firstName": "Manisha",
              "lastName": "Hambir",
              "email": "manisha@example.com",
              "phone": "+919876543210",
              "password": "Secret@123",
              "status": "ACTIVE"
            }
            """;

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    @Operation(summary = "Create a student", description = "Registers a new student. Email must be unique.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Student created",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already exists",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PostMapping
    public ResponseEntity<StudentResponse> createStudent(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(examples = @ExampleObject(name = "New student", value = STUDENT_REQUEST_EXAMPLE)))
            @Valid @RequestBody StudentRequest request) {
        log.info("POST /api/students");
        StudentResponse response = studentService.createStudent(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}")
                .buildAndExpand(response.getId())
                .toUri();
        return ResponseEntity.created(location).body(response);
    }

    @Operation(summary = "List students (paginated)",
            description = "Returns a page of students. Example: `?page=0&size=10&sort=firstName,asc`")
    @ApiResponse(responseCode = "200", description = "Page of students")
    @GetMapping
    public ResponseEntity<PageResponse<StudentResponse>> getAllStudents(
            @ParameterObject @PageableDefault(size = 10, sort = "id", direction = Sort.Direction.ASC) Pageable pageable) {
        log.info("GET /api/students page={} size={}", pageable.getPageNumber(), pageable.getPageSize());
        return ResponseEntity.ok(studentService.getAllStudents(pageable));
    }

    @Operation(summary = "Get a student by id")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student found",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))),
            @ApiResponse(responseCode = "404", description = "Student not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<StudentResponse> getStudentById(
            @Parameter(description = "Student id", example = "1") @PathVariable Long id) {
        log.info("GET /api/students/{}", id);
        return ResponseEntity.ok(studentService.getStudentById(id));
    }

    @Operation(summary = "Update a student", description = "Full update of an existing student.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Student updated",
                    content = @Content(schema = @Schema(implementation = StudentResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "Student not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already used by another student",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<StudentResponse> updateStudent(
            @Parameter(description = "Student id", example = "1") @PathVariable Long id,
            @io.swagger.v3.oas.annotations.parameters.RequestBody(required = true,
                    content = @Content(examples = @ExampleObject(name = "Updated student", value = STUDENT_REQUEST_EXAMPLE)))
            @Valid @RequestBody StudentRequest request) {
        log.info("PUT /api/students/{}", id);
        return ResponseEntity.ok(studentService.updateStudent(id, request));
    }

    @Operation(summary = "Delete a student")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Student deleted"),
            @ApiResponse(responseCode = "404", description = "Student not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteStudent(
            @Parameter(description = "Student id", example = "1") @PathVariable Long id) {
        log.info("DELETE /api/students/{}", id);
        studentService.deleteStudent(id);
        return ResponseEntity.noContent().build();
    }
}

package com.slp.studentservice.dto;

import com.slp.studentservice.entity.StudentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(name = "StudentResponse", description = "Student representation returned by the API (never contains the password)")
public class StudentResponse {

    @Schema(example = "1")
    private Long id;

    @Schema(example = "Manisha")
    private String firstName;

    @Schema(example = "Hambir")
    private String lastName;

    @Schema(example = "manisha@example.com")
    private String email;

    @Schema(example = "+919876543210")
    private String phone;

    @Schema(example = "ACTIVE")
    private StudentStatus status;

    @Schema(example = "2025-01-15T10:15:30")
    private LocalDateTime createdAt;

    @Schema(example = "2025-01-15T10:15:30")
    private LocalDateTime updatedAt;
}

package com.slp.studentservice.service.impl;

import com.slp.studentservice.dto.PageResponse;
import com.slp.studentservice.dto.StudentRequest;
import com.slp.studentservice.dto.StudentResponse;
import com.slp.studentservice.entity.Student;
import com.slp.studentservice.entity.StudentStatus;
import com.slp.studentservice.exception.EmailAlreadyExistsException;
import com.slp.studentservice.exception.StudentNotFoundException;
import com.slp.studentservice.mapper.StudentMapper;
import com.slp.studentservice.repository.StudentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StudentServiceImplTest {

    @Mock
    private StudentRepository studentRepository;

    @Spy
    private StudentMapper studentMapper = new StudentMapper();

    @InjectMocks
    private StudentServiceImpl studentService;

    private StudentRequest request;
    private Student student;

    @BeforeEach
    void setUp() {
        request = StudentRequest.builder()
                .firstName("Manisha")
                .lastName("Hambir")
                .email("manisha@example.com")
                .phone("+919876543210")
                .password("Secret@123")
                .status(StudentStatus.ACTIVE)
                .build();

        student = Student.builder()
                .id(1L)
                .firstName("Manisha")
                .lastName("Hambir")
                .email("manisha@example.com")
                .phone("+919876543210")
                .password("Secret@123")
                .status(StudentStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("createStudent")
    class CreateStudent {

        @Test
        @DisplayName("creates student successfully and never exposes password")
        void createStudentSuccess() {
            when(studentRepository.existsByEmail("manisha@example.com")).thenReturn(false);
            when(studentRepository.save(any(Student.class))).thenReturn(student);

            StudentResponse response = studentService.createStudent(request);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getEmail()).isEqualTo("manisha@example.com");
            assertThat(response.getFirstName()).isEqualTo("Manisha");
            assertThat(response.getStatus()).isEqualTo(StudentStatus.ACTIVE);
            verify(studentRepository).existsByEmail("manisha@example.com");
            verify(studentRepository).save(any(Student.class));
            verify(studentMapper).toEntity(request);
            verify(studentMapper).toResponse(student);
        }

        @Test
        @DisplayName("throws EmailAlreadyExistsException for duplicate email")
        void createStudentDuplicateEmail() {
            when(studentRepository.existsByEmail("manisha@example.com")).thenReturn(true);

            assertThatThrownBy(() -> studentService.createStudent(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessage("Email already exists: manisha@example.com");

            verify(studentRepository, never()).save(any(Student.class));
        }

        @Test
        @DisplayName("propagates repository failures")
        void createStudentRepositoryFailure() {
            when(studentRepository.existsByEmail(any())).thenReturn(false);
            when(studentRepository.save(any(Student.class)))
                    .thenThrow(new DataAccessResourceFailureException("DB down"));

            assertThatThrownBy(() -> studentService.createStudent(request))
                    .isInstanceOf(DataAccessResourceFailureException.class)
                    .hasMessage("DB down");
        }
    }

    @Nested
    @DisplayName("getStudentById")
    class GetStudentById {

        @Test
        @DisplayName("returns student when it exists")
        void getStudentByIdSuccess() {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            StudentResponse response = studentService.getStudentById(1L);

            assertThat(response.getId()).isEqualTo(1L);
            assertThat(response.getLastName()).isEqualTo("Hambir");
            verify(studentRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("throws StudentNotFoundException when missing")
        void getStudentByIdNotFound() {
            when(studentRepository.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.getStudentById(99L))
                    .isInstanceOf(StudentNotFoundException.class)
                    .hasMessage("Student not found with id: 99");
        }
    }

    @Nested
    @DisplayName("getAllStudents")
    class GetAllStudents {

        @Test
        @DisplayName("returns a mapped page with pagination metadata")
        void getAllStudentsReturnsPage() {
            Student second = Student.builder().id(2L).firstName("Amit").lastName("Shah")
                    .email("amit@example.com").password("x").status(StudentStatus.INACTIVE).build();
            Pageable pageable = PageRequest.of(0, 10, Sort.by("firstName").ascending());
            Page<Student> page = new PageImpl<>(List.of(second, student), pageable, 2);
            when(studentRepository.findAll(pageable)).thenReturn(page);

            PageResponse<StudentResponse> result = studentService.getAllStudents(pageable);

            assertThat(result.getContent()).hasSize(2)
                    .extracting(StudentResponse::getFirstName).containsExactly("Amit", "Manisha");
            assertThat(result.getPage()).isZero();
            assertThat(result.getSize()).isEqualTo(10);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getTotalPages()).isEqualTo(1);
            assertThat(result.isLast()).isTrue();
            verify(studentRepository).findAll(pageable);
        }

        @Test
        @DisplayName("returns an empty page when there are no students")
        void getAllStudentsEmpty() {
            Pageable pageable = PageRequest.of(0, 5);
            when(studentRepository.findAll(pageable)).thenReturn(Page.empty(pageable));

            PageResponse<StudentResponse> result = studentService.getAllStudents(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("updateStudent")
    class UpdateStudent {

        @Test
        @DisplayName("updates an existing student")
        void updateStudentSuccess() {
            StudentRequest update = StudentRequest.builder()
                    .firstName("Manisha").lastName("Patil").email("manisha.patil@example.com")
                    .phone("9999999999").password("NewSecret@1").status(StudentStatus.SUSPENDED).build();
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(studentRepository.existsByEmailAndIdNot("manisha.patil@example.com", 1L)).thenReturn(false);
            when(studentRepository.saveAndFlush(student)).thenReturn(student);

            StudentResponse response = studentService.updateStudent(1L, update);

            assertThat(response.getLastName()).isEqualTo("Patil");
            assertThat(response.getEmail()).isEqualTo("manisha.patil@example.com");
            assertThat(response.getStatus()).isEqualTo(StudentStatus.SUSPENDED);
            assertThat(student.getPassword()).isEqualTo("NewSecret@1");
            verify(studentMapper).updateEntity(student, update);
            verify(studentRepository).saveAndFlush(student);
        }

        @Test
        @DisplayName("throws StudentNotFoundException for a non-existing student")
        void updateStudentNotFound() {
            when(studentRepository.findById(42L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.updateStudent(42L, request))
                    .isInstanceOf(StudentNotFoundException.class);

            verify(studentRepository, never()).save(any());
        }

        @Test
        @DisplayName("throws EmailAlreadyExistsException when email belongs to another student")
        void updateStudentDuplicateEmail() {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));
            when(studentRepository.existsByEmailAndIdNot("manisha@example.com", 1L)).thenReturn(true);

            assertThatThrownBy(() -> studentService.updateStudent(1L, request))
                    .isInstanceOf(EmailAlreadyExistsException.class);

            verify(studentRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("deleteStudent")
    class DeleteStudent {

        @Test
        @DisplayName("deletes an existing student")
        void deleteStudentSuccess() {
            when(studentRepository.findById(1L)).thenReturn(Optional.of(student));

            studentService.deleteStudent(1L);

            verify(studentRepository).findById(1L);
            verify(studentRepository).delete(student);
            verifyNoMoreInteractions(studentRepository);
        }

        @Test
        @DisplayName("throws StudentNotFoundException when deleting a missing student")
        void deleteStudentNotFound() {
            when(studentRepository.findById(anyLong())).thenReturn(Optional.empty());

            assertThatThrownBy(() -> studentService.deleteStudent(7L))
                    .isInstanceOf(StudentNotFoundException.class)
                    .hasMessage("Student not found with id: 7");

            verify(studentRepository, never()).delete(any(Student.class));
        }
    }
}

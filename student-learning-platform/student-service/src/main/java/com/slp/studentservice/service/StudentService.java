package com.slp.studentservice.service;

import com.slp.studentservice.dto.PageResponse;
import com.slp.studentservice.dto.StudentRequest;
import com.slp.studentservice.dto.StudentResponse;
import org.springframework.data.domain.Pageable;

public interface StudentService {

    StudentResponse createStudent(StudentRequest request);

    StudentResponse getStudentById(Long id);

    PageResponse<StudentResponse> getAllStudents(Pageable pageable);

    StudentResponse updateStudent(Long id, StudentRequest request);

    void deleteStudent(Long id);
}

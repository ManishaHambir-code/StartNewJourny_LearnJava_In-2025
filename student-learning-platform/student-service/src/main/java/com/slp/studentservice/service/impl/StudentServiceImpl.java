package com.slp.studentservice.service.impl;

import com.slp.studentservice.dto.PageResponse;
import com.slp.studentservice.dto.StudentRequest;
import com.slp.studentservice.dto.StudentResponse;
import com.slp.studentservice.entity.Student;
import com.slp.studentservice.exception.EmailAlreadyExistsException;
import com.slp.studentservice.exception.StudentNotFoundException;
import com.slp.studentservice.mapper.StudentMapper;
import com.slp.studentservice.repository.StudentRepository;
import com.slp.studentservice.service.StudentService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@Transactional(readOnly = true)
public class StudentServiceImpl implements StudentService {

    private final StudentRepository studentRepository;
    private final StudentMapper studentMapper;

    public StudentServiceImpl(StudentRepository studentRepository, StudentMapper studentMapper) {
        this.studentRepository = studentRepository;
        this.studentMapper = studentMapper;
    }

    @Override
    @Transactional
    public StudentResponse createStudent(StudentRequest request) {
        log.info("Creating student with email={}", request.getEmail());
        if (studentRepository.existsByEmail(request.getEmail())) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        Student saved = studentRepository.save(studentMapper.toEntity(request));
        log.info("Student created with id={}", saved.getId());
        return studentMapper.toResponse(saved);
    }

    @Override
    public StudentResponse getStudentById(Long id) {
        log.debug("Fetching student id={}", id);
        return studentMapper.toResponse(findStudentOrThrow(id));
    }

    @Override
    public PageResponse<StudentResponse> getAllStudents(Pageable pageable) {
        log.debug("Fetching students page={} size={} sort={}",
                pageable.getPageNumber(), pageable.getPageSize(), pageable.getSort());
        Page<StudentResponse> page = studentRepository.findAll(pageable).map(studentMapper::toResponse);
        return PageResponse.from(page);
    }

    @Override
    @Transactional
    public StudentResponse updateStudent(Long id, StudentRequest request) {
        log.info("Updating student id={}", id);
        Student student = findStudentOrThrow(id);
        if (studentRepository.existsByEmailAndIdNot(request.getEmail(), id)) {
            throw new EmailAlreadyExistsException(request.getEmail());
        }
        studentMapper.updateEntity(student, request);
        Student saved = studentRepository.saveAndFlush(student);
        log.info("Student updated id={}", saved.getId());
        return studentMapper.toResponse(saved);
    }

    @Override
    @Transactional
    public void deleteStudent(Long id) {
        log.info("Deleting student id={}", id);
        Student student = findStudentOrThrow(id);
        studentRepository.delete(student);
        log.info("Student deleted id={}", id);
    }

    private Student findStudentOrThrow(Long id) {
        return studentRepository.findById(id)
                .orElseThrow(() -> new StudentNotFoundException(id));
    }
}

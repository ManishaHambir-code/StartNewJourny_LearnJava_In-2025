package com.scp.java.employee.service.impl;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scp.java.employee.dto.EmployeeDto;
import com.scp.java.employee.entity.Employee;
import com.scp.java.employee.exception.DuplicateEmailException;
import com.scp.java.employee.exception.EmployeeNotFoundException;
import com.scp.java.employee.mapper.EmployeeMapper;
import com.scp.java.employee.repository.EmployeeRepository;
import com.scp.java.employee.service.EmployeeService;

@Service
@Transactional
public class EmployeeServiceImpl implements EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final EmployeeMapper employeeMapper;

    public EmployeeServiceImpl(EmployeeRepository employeeRepository, EmployeeMapper employeeMapper) {
        this.employeeRepository = employeeRepository;
        this.employeeMapper = employeeMapper;
    }

    @Override
    public EmployeeDto create(EmployeeDto employeeDto) {
        if (employeeRepository.existsByEmailIgnoreCase(employeeDto.getEmail())) {
            throw new DuplicateEmailException(employeeDto.getEmail());
        }
        Employee saved = employeeRepository.save(employeeMapper.toEntity(employeeDto));
        return employeeMapper.toDto(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EmployeeDto> findAll() {
        return employeeRepository.findAll().stream().map(employeeMapper::toDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EmployeeDto findById(Long id) {
        return employeeMapper.toDto(getExisting(id));
    }

    @Override
    public EmployeeDto update(Long id, EmployeeDto employeeDto) {
        Employee employee = getExisting(id);
        if (employeeRepository.existsByEmailIgnoreCaseAndIdNot(employeeDto.getEmail(), id)) {
            throw new DuplicateEmailException(employeeDto.getEmail());
        }
        employeeMapper.copyToEntity(employeeDto, employee);
        return employeeMapper.toDto(employeeRepository.save(employee));
    }

    @Override
    public void delete(Long id) {
        employeeRepository.delete(getExisting(id));
    }

    private Employee getExisting(Long id) {
        return employeeRepository.findById(id).orElseThrow(() -> new EmployeeNotFoundException(id));
    }
}

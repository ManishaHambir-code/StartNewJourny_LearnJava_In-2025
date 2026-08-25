package com.scp.java.employee.service;

import java.util.List;

import com.scp.java.employee.dto.EmployeeDto;

public interface EmployeeService {

    EmployeeDto create(EmployeeDto employeeDto);

    List<EmployeeDto> findAll();

    EmployeeDto findById(Long id);

    EmployeeDto update(Long id, EmployeeDto employeeDto);

    void delete(Long id);
}

package com.scp.java.employee.mapper;

import org.springframework.stereotype.Component;

import com.scp.java.employee.dto.EmployeeDto;
import com.scp.java.employee.entity.Employee;

@Component
public class EmployeeMapper {

    public EmployeeDto toDto(Employee employee) {
        return new EmployeeDto(employee.getId(), employee.getFirstName(), employee.getLastName(), employee.getEmail(),
                employee.getDepartment(), employee.getSalary(), employee.getDateOfJoining());
    }

    public Employee toEntity(EmployeeDto dto) {
        Employee employee = new Employee();
        copyToEntity(dto, employee);
        return employee;
    }

    public void copyToEntity(EmployeeDto dto, Employee employee) {
        employee.setFirstName(dto.getFirstName());
        employee.setLastName(dto.getLastName());
        employee.setEmail(dto.getEmail());
        employee.setDepartment(dto.getDepartment());
        employee.setSalary(dto.getSalary());
        employee.setDateOfJoining(dto.getDateOfJoining());
    }
}

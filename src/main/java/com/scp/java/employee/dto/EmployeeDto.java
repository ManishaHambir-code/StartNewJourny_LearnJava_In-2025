package com.scp.java.employee.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

import javax.validation.constraints.DecimalMin;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

public class EmployeeDto {

    private Long id;

    @NotBlank(message = "firstName is required")
    @Size(max = 50, message = "firstName must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @Size(max = 50, message = "lastName must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "email is required")
    @Email(message = "email must be a valid address")
    @Size(max = 120, message = "email must not exceed 120 characters")
    private String email;

    @NotBlank(message = "department is required")
    @Size(max = 60, message = "department must not exceed 60 characters")
    private String department;

    @NotNull(message = "salary is required")
    @DecimalMin(value = "0.0", inclusive = false, message = "salary must be greater than 0")
    private BigDecimal salary;

    @NotNull(message = "dateOfJoining is required")
    @PastOrPresent(message = "dateOfJoining must not be in the future")
    private LocalDate dateOfJoining;

    public EmployeeDto() {
    }

    public EmployeeDto(Long id, String firstName, String lastName, String email, String department, BigDecimal salary,
            LocalDate dateOfJoining) {
        this.id = id;
        this.firstName = firstName;
        this.lastName = lastName;
        this.email = email;
        this.department = department;
        this.salary = salary;
        this.dateOfJoining = dateOfJoining;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDepartment() {
        return department;
    }

    public void setDepartment(String department) {
        this.department = department;
    }

    public BigDecimal getSalary() {
        return salary;
    }

    public void setSalary(BigDecimal salary) {
        this.salary = salary;
    }

    public LocalDate getDateOfJoining() {
        return dateOfJoining;
    }

    public void setDateOfJoining(LocalDate dateOfJoining) {
        this.dateOfJoining = dateOfJoining;
    }
}

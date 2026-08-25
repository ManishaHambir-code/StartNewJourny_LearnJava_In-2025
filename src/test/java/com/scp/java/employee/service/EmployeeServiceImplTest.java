package com.scp.java.employee.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.scp.java.employee.dto.EmployeeDto;
import com.scp.java.employee.entity.Employee;
import com.scp.java.employee.exception.DuplicateEmailException;
import com.scp.java.employee.exception.EmployeeNotFoundException;
import com.scp.java.employee.mapper.EmployeeMapper;
import com.scp.java.employee.repository.EmployeeRepository;
import com.scp.java.employee.service.impl.EmployeeServiceImpl;

@ExtendWith(MockitoExtension.class)
class EmployeeServiceImplTest {

    @Mock
    private EmployeeRepository employeeRepository;

    private EmployeeServiceImpl employeeService;

    @BeforeEach
    void setUp() {
        employeeService = new EmployeeServiceImpl(employeeRepository, new EmployeeMapper());
    }

    private Employee employee(Long id, String email) {
        return new Employee(id, "Manisha", "Hambir", email, "Engineering", new BigDecimal("75000.00"),
                LocalDate.of(2025, 1, 10));
    }

    private EmployeeDto dto(String email) {
        return new EmployeeDto(null, "Manisha", "Hambir", email, "Engineering", new BigDecimal("75000.00"),
                LocalDate.of(2025, 1, 10));
    }

    @Test
    void createPersistsEmployeeAndReturnsDto() {
        when(employeeRepository.existsByEmailIgnoreCase("manisha@example.com")).thenReturn(false);
        when(employeeRepository.save(any(Employee.class))).thenReturn(employee(1L, "manisha@example.com"));

        EmployeeDto created = employeeService.create(dto("manisha@example.com"));

        assertEquals(1L, created.getId().longValue());
        assertEquals("manisha@example.com", created.getEmail());
        ArgumentCaptor<Employee> captor = ArgumentCaptor.forClass(Employee.class);
        verify(employeeRepository).save(captor.capture());
        assertEquals("Manisha", captor.getValue().getFirstName());
    }

    @Test
    void createRejectsDuplicateEmail() {
        when(employeeRepository.existsByEmailIgnoreCase("manisha@example.com")).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> employeeService.create(dto("manisha@example.com")));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void findAllReturnsAllEmployees() {
        when(employeeRepository.findAll())
                .thenReturn(Arrays.asList(employee(1L, "a@example.com"), employee(2L, "b@example.com")));

        List<EmployeeDto> all = employeeService.findAll();

        assertEquals(2, all.size());
        assertEquals("b@example.com", all.get(1).getEmail());
    }

    @Test
    void findByIdReturnsEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(1L, "a@example.com")));

        assertEquals("a@example.com", employeeService.findById(1L).getEmail());
    }

    @Test
    void findByIdThrowsWhenMissing() {
        when(employeeRepository.findById(99L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.findById(99L));
    }

    @Test
    void updateOverwritesExistingFields() {
        Employee existing = employee(1L, "old@example.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot("new@example.com", 1L)).thenReturn(false);
        when(employeeRepository.save(existing)).thenReturn(existing);

        EmployeeDto updated = employeeService.update(1L, dto("new@example.com"));

        assertEquals("new@example.com", updated.getEmail());
        assertEquals("new@example.com", existing.getEmail());
    }

    @Test
    void updateRejectsEmailOwnedByAnotherEmployee() {
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(employee(1L, "old@example.com")));
        when(employeeRepository.existsByEmailIgnoreCaseAndIdNot(anyString(), eq(1L))).thenReturn(true);

        assertThrows(DuplicateEmailException.class, () -> employeeService.update(1L, dto("taken@example.com")));
        verify(employeeRepository, never()).save(any(Employee.class));
    }

    @Test
    void deleteRemovesExistingEmployee() {
        Employee existing = employee(1L, "a@example.com");
        when(employeeRepository.findById(1L)).thenReturn(Optional.of(existing));

        employeeService.delete(1L);

        verify(employeeRepository, times(1)).delete(existing);
    }

    @Test
    void deleteThrowsWhenMissing() {
        when(employeeRepository.findById(5L)).thenReturn(Optional.empty());

        assertThrows(EmployeeNotFoundException.class, () -> employeeService.delete(5L));
    }
}

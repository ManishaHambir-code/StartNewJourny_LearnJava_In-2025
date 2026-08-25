package com.scp.java.employee.exception;

public class DuplicateEmailException extends RuntimeException {

    private static final long serialVersionUID = 1L;

    public DuplicateEmailException(String email) {
        super("Employee already exists with email: " + email);
    }
}

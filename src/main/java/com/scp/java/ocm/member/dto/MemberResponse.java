package com.scp.java.ocm.member.dto;

import com.scp.java.ocm.member.entity.Gender;
import com.scp.java.ocm.member.entity.MemberStatus;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class MemberResponse {
    private Long id;
    private String mrn;
    private String firstName;
    private String lastName;
    private LocalDate dateOfBirth;
    private Gender gender;
    private String email;
    private String phone;
    private String addressLine1;
    private String city;
    private String state;
    private String postalCode;
    private String planId;
    private MemberStatus status;
    private LocalDate enrollmentDate;
}

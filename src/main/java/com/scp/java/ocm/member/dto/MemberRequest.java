package com.scp.java.ocm.member.dto;

import java.time.LocalDate;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Past;
import javax.validation.constraints.PastOrPresent;
import javax.validation.constraints.Size;

import com.scp.java.ocm.member.entity.Gender;
import com.scp.java.ocm.member.entity.MemberStatus;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter @Setter @NoArgsConstructor
public class MemberRequest {
    @NotBlank @Size(max = 20) private String mrn;
    @NotBlank @Size(max = 50) private String firstName;
    @NotBlank @Size(max = 50) private String lastName;
    @NotNull @Past private LocalDate dateOfBirth;
    @NotNull private Gender gender;
    @Email @Size(max = 120) private String email;
    @Size(max = 20) private String phone;
    @Size(max = 120) private String addressLine1;
    @Size(max = 60) private String city;
    @Size(min = 2, max = 40) private String state;
    @Size(max = 10) private String postalCode;
    @NotBlank @Size(max = 40) private String planId;
    private MemberStatus status;
    @NotNull @PastOrPresent private LocalDate enrollmentDate;
}

package com.scp.java.ocm.member.entity;

import com.scp.java.ocm.common.entity.BaseAuditEntity;
import java.time.LocalDate;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
public class Member extends BaseAuditEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 20)
    private String mrn;

    @Column(name = "first_name", nullable = false, length = 50)
    private String firstName;

    @Column(name = "last_name", nullable = false, length = 50)
    private String lastName;

    @Column(name = "date_of_birth", nullable = false)
    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private Gender gender;

    @Column(unique = true, length = 120)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(name = "address_line1", length = 120)
    private String addressLine1;

    @Column(length = 60)
    private String city;

    @Column(length = 40)
    private String state;

    @Column(name = "postal_code", length = 10)
    private String postalCode;

    @Column(name = "plan_id", nullable = false, length = 40)
    private String planId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private MemberStatus status = MemberStatus.ACTIVE;

    @Column(name = "enrollment_date", nullable = false)
    private LocalDate enrollmentDate;
}

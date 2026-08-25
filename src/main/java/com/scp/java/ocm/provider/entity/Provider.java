package com.scp.java.ocm.provider.entity;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

import com.scp.java.ocm.common.entity.BaseAuditEntity;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name = "providers")
@Getter @Setter @NoArgsConstructor
public class Provider extends BaseAuditEntity {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY) private Long id;
    @Column(nullable = false, unique = true, length = 10) private String npi;
    @Column(name = "first_name", nullable = false, length = 50) private String firstName;
    @Column(name = "last_name", nullable = false, length = 50) private String lastName;
    @Enumerated(EnumType.STRING) @Column(nullable = false, length = 30) private ProviderSpecialty specialty;
    @Column(name = "facility_name", length = 120) private String facilityName;
    @Column(unique = true, length = 120) private String email;
    @Column(length = 20) private String phone;
    @Column(nullable = false) private boolean active = true;
}

package com.scp.java.ocm.claim.entity;

import java.math.BigDecimal;
import java.time.LocalDate;
import javax.persistence.*;
import com.scp.java.ocm.common.entity.BaseAuditEntity;
import com.scp.java.ocm.member.entity.Member;
import com.scp.java.ocm.provider.entity.Provider;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="claims")
@Getter @Setter @NoArgsConstructor
public class Claim extends BaseAuditEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @Column(name="claim_number",nullable=false,unique=true,length=30) private String claimNumber;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="member_id",nullable=false) private Member member;
    @ManyToOne(fetch=FetchType.LAZY,optional=false) @JoinColumn(name="provider_id",nullable=false) private Provider provider;
    @Column(name="service_date",nullable=false) private LocalDate serviceDate;
    @Column(name="billed_amount",nullable=false,precision=12,scale=2) private BigDecimal billedAmount;
    @Column(name="allowed_amount",precision=12,scale=2) private BigDecimal allowedAmount;
    @Enumerated(EnumType.STRING) @Column(nullable=false,length=20) private ClaimStatus status=ClaimStatus.SUBMITTED;
    @Column(name="denial_reason",length=255) private String denialReason;
    @Column(name="diagnosis_code",nullable=false,length=10) private String diagnosisCode;
}

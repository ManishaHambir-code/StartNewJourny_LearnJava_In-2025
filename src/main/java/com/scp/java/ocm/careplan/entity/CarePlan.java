package com.scp.java.ocm.careplan.entity;

import java.time.LocalDate;
import javax.persistence.*;
import com.scp.java.ocm.common.entity.BaseAuditEntity;
import com.scp.java.ocm.member.entity.Member;
import com.scp.java.ocm.provider.entity.Provider;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity @Table(name="care_plans")
@Getter @Setter @NoArgsConstructor
public class CarePlan extends BaseAuditEntity {
    @Id @GeneratedValue(strategy=GenerationType.IDENTITY) private Long id;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="member_id", nullable=false) private Member member;
    @ManyToOne(fetch=FetchType.LAZY, optional=false) @JoinColumn(name="care_manager_id", nullable=false) private Provider careManager;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=40) private ProgramType program;
    @Enumerated(EnumType.STRING) @Column(name="risk_level", nullable=false, length=20) private RiskLevel riskLevel;
    @Enumerated(EnumType.STRING) @Column(nullable=false, length=20) private CarePlanStatus status = CarePlanStatus.DRAFT;
    @Column(columnDefinition="TEXT", length=2000) private String goals;
    @Column(name="start_date", nullable=false) private LocalDate startDate;
    @Column(name="end_date") private LocalDate endDate;
}

package com.scp.java.ocm.careplan.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import com.scp.java.ocm.careplan.entity.*;

public interface CarePlanRepository extends JpaRepository<CarePlan, Long>, JpaSpecificationExecutor<CarePlan> {
    boolean existsByMemberId(Long memberId);
    boolean existsByCareManagerId(Long providerId);
    boolean existsByMemberIdAndStatus(Long memberId, CarePlanStatus status);
    boolean existsByCareManagerIdAndStatus(Long providerId, CarePlanStatus status);
    boolean existsByMemberIdAndProgramAndStatusIn(Long memberId, ProgramType program, java.util.Collection<CarePlanStatus> statuses);
    Page<CarePlan> findByMemberId(Long memberId, Pageable pageable);
}

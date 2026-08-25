package com.scp.java.ocm.careplan.repository;

import com.scp.java.ocm.careplan.entity.CarePlan;
import com.scp.java.ocm.careplan.entity.CarePlanStatus;
import com.scp.java.ocm.careplan.entity.ProgramType;
import java.util.Collection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CarePlanRepository
        extends JpaRepository<CarePlan, Long>, JpaSpecificationExecutor<CarePlan> {
    boolean existsByMemberId(Long memberId);

    boolean existsByCareManagerId(Long providerId);

    boolean existsByMemberIdAndStatus(Long memberId, CarePlanStatus status);

    boolean existsByCareManagerIdAndStatus(Long providerId, CarePlanStatus status);

    boolean existsByMemberIdAndProgramAndStatusIn(
            Long memberId, ProgramType program, Collection<CarePlanStatus> statuses);

    Page<CarePlan> findByMemberId(Long memberId, Pageable pageable);
}

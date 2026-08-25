package com.scp.java.ocm.careplan.service;

import com.scp.java.ocm.careplan.dto.CarePlanRequest;
import com.scp.java.ocm.careplan.dto.CarePlanResponse;
import com.scp.java.ocm.careplan.entity.CarePlanStatus;
import com.scp.java.ocm.careplan.entity.ProgramType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CarePlanService {
    CarePlanResponse create(CarePlanRequest request);

    Page<CarePlanResponse> findAll(
            Pageable pageable, Long memberId, CarePlanStatus status, ProgramType program);

    Page<CarePlanResponse> findByMember(Long memberId, Pageable pageable);

    CarePlanResponse findById(Long id);

    CarePlanResponse update(Long id, CarePlanRequest request);

    CarePlanResponse activate(Long id);

    CarePlanResponse complete(Long id);
}

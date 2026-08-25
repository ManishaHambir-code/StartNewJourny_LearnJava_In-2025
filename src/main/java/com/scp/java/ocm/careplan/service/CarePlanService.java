package com.scp.java.ocm.careplan.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import com.scp.java.ocm.careplan.dto.*;
import com.scp.java.ocm.careplan.entity.*;

public interface CarePlanService {
    CarePlanResponse create(CarePlanRequest request);
    Page<CarePlanResponse> findAll(Pageable pageable, Long memberId, CarePlanStatus status, ProgramType program);
    Page<CarePlanResponse> findByMember(Long memberId, Pageable pageable);
    CarePlanResponse findById(Long id);
    CarePlanResponse update(Long id, CarePlanRequest request);
    CarePlanResponse activate(Long id);
    CarePlanResponse complete(Long id);
}

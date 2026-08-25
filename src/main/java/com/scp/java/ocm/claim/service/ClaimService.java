package com.scp.java.ocm.claim.service;

import org.springframework.data.domain.*;
import com.scp.java.ocm.claim.dto.*;
import com.scp.java.ocm.claim.entity.ClaimStatus;

public interface ClaimService {
    ClaimResponse create(ClaimRequest request);
    Page<ClaimResponse> findAll(Pageable pageable,Long memberId,Long providerId,ClaimStatus status);
    Page<ClaimResponse> findByMember(Long memberId,Pageable pageable);
    ClaimResponse findById(Long id);
    ClaimResponse update(Long id,ClaimRequest request);
    ClaimResponse adjudicate(Long id,ClaimAdjudicationRequest request);
    void delete(Long id);
}

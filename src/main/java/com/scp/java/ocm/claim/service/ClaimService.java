package com.scp.java.ocm.claim.service;

import com.scp.java.ocm.claim.dto.ClaimAdjudicationRequest;
import com.scp.java.ocm.claim.dto.ClaimRequest;
import com.scp.java.ocm.claim.dto.ClaimResponse;
import com.scp.java.ocm.claim.entity.ClaimStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ClaimService {
    ClaimResponse create(ClaimRequest request);

    Page<ClaimResponse> findAll(
            Pageable pageable, Long memberId, Long providerId, ClaimStatus status);

    Page<ClaimResponse> findByMember(Long memberId, Pageable pageable);

    ClaimResponse findById(Long id);

    ClaimResponse update(Long id, ClaimRequest request);

    ClaimResponse adjudicate(Long id, ClaimAdjudicationRequest request);

    void delete(Long id);
}

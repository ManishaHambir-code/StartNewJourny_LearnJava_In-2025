package com.scp.java.ocm.claim.repository;

import com.scp.java.ocm.claim.entity.Claim;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ClaimRepository
        extends JpaRepository<Claim, Long>, JpaSpecificationExecutor<Claim> {
    boolean existsByClaimNumberIgnoreCase(String claimNumber);

    boolean existsByClaimNumberIgnoreCaseAndIdNot(String claimNumber, Long id);

    boolean existsByMemberId(Long memberId);

    boolean existsByProviderId(Long providerId);

    Page<Claim> findByMemberId(Long memberId, Pageable pageable);
}

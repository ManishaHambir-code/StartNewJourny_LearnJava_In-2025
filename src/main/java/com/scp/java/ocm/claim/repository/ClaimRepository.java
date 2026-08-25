package com.scp.java.ocm.claim.repository;

import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.*;
import com.scp.java.ocm.claim.entity.Claim;

public interface ClaimRepository extends JpaRepository<Claim,Long>, JpaSpecificationExecutor<Claim> {
    boolean existsByClaimNumberIgnoreCase(String claimNumber);
    boolean existsByClaimNumberIgnoreCaseAndIdNot(String claimNumber,Long id);
    boolean existsByMemberId(Long memberId);
    boolean existsByProviderId(Long providerId);
    Page<Claim> findByMemberId(Long memberId,Pageable pageable);
}

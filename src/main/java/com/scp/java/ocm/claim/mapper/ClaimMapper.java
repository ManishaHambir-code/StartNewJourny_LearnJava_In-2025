package com.scp.java.ocm.claim.mapper;

import org.springframework.stereotype.Component;
import com.scp.java.ocm.claim.dto.ClaimRequest;
import com.scp.java.ocm.claim.dto.ClaimResponse;
import com.scp.java.ocm.claim.entity.Claim;
import com.scp.java.ocm.member.entity.Member;
import com.scp.java.ocm.provider.entity.Provider;

@Component
public class ClaimMapper {
    public ClaimResponse toResponse(Claim e){return new ClaimResponse(e.getId(),e.getClaimNumber(),e.getMember().getId(),e.getProvider().getId(),e.getServiceDate(),e.getBilledAmount(),e.getAllowedAmount(),e.getStatus(),e.getDenialReason(),e.getDiagnosisCode());}
    public Claim toEntity(ClaimRequest r,Member m,Provider p){Claim e=new Claim();e.setMember(m);e.setProvider(p);e.setStatus(com.scp.java.ocm.claim.entity.ClaimStatus.SUBMITTED);copy(r,e);return e;}
    public void copy(ClaimRequest r,Claim e){e.setClaimNumber(r.getClaimNumber());e.setServiceDate(r.getServiceDate());e.setBilledAmount(r.getBilledAmount());e.setDiagnosisCode(r.getDiagnosisCode());}
}

package com.scp.java.ocm.claim.service.impl;

import java.math.BigDecimal;
import java.util.*;
import javax.persistence.criteria.Predicate;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.scp.java.ocm.claim.dto.*;
import com.scp.java.ocm.claim.entity.*;
import com.scp.java.ocm.claim.mapper.ClaimMapper;
import com.scp.java.ocm.claim.repository.ClaimRepository;
import com.scp.java.ocm.claim.service.ClaimService;
import com.scp.java.ocm.common.event.*;
import com.scp.java.ocm.common.exception.*;
import com.scp.java.ocm.member.entity.Member;
import com.scp.java.ocm.member.entity.MemberStatus;
import com.scp.java.ocm.member.repository.MemberRepository;
import com.scp.java.ocm.provider.entity.Provider;
import com.scp.java.ocm.provider.repository.ProviderRepository;

@Service @Transactional(readOnly=true)
public class ClaimServiceImpl implements ClaimService {
    private final ClaimRepository repository; private final ClaimMapper mapper; private final MemberRepository members;
    private final ProviderRepository providers; private final DomainEventPublisher events;
    public ClaimServiceImpl(ClaimRepository repository,ClaimMapper mapper,MemberRepository members,ProviderRepository providers,DomainEventPublisher events){this.repository=repository;this.mapper=mapper;this.members=members;this.providers=providers;this.events=events;}
    @Override @Transactional public ClaimResponse create(ClaimRequest r){
        if(repository.existsByClaimNumberIgnoreCase(r.getClaimNumber()))throw new DuplicateResourceException("Claim already exists: "+r.getClaimNumber());
        Member m=members.findById(r.getMemberId()).orElseThrow(()->new ResourceNotFoundException("Member not found: "+r.getMemberId()));
        Provider p=providers.findById(r.getProviderId()).orElseThrow(()->new ResourceNotFoundException("Provider not found: "+r.getProviderId()));
        if(m.getStatus()==MemberStatus.TERMINATED)throw new BusinessRuleViolationException("Claims cannot be created for terminated members");
        Claim saved=repository.save(mapper.toEntity(r,m,p));events.publish(new DomainEvent("CLAIM_SUBMITTED","Claim",String.valueOf(saved.getId()),Collections.<String,Object>singletonMap("claimNumber",saved.getClaimNumber())));return mapper.toResponse(saved);
    }
    @Override public Page<ClaimResponse> findAll(Pageable pageable,Long memberId,Long providerId,ClaimStatus status){
        Specification<Claim> spec=(root,q,cb)->{List<Predicate> ps=new ArrayList<Predicate>();if(memberId!=null)ps.add(cb.equal(root.get("member").get("id"),memberId));if(providerId!=null)ps.add(cb.equal(root.get("provider").get("id"),providerId));if(status!=null)ps.add(cb.equal(root.get("status"),status));return cb.and(ps.toArray(new Predicate[ps.size()]));};
        return repository.findAll(spec,pageable).map(mapper::toResponse);
    }
    @Override public Page<ClaimResponse> findByMember(Long memberId,Pageable pageable){if(!members.existsById(memberId))throw new ResourceNotFoundException("Member not found: "+memberId);return repository.findByMemberId(memberId,pageable).map(mapper::toResponse);}
    @Override public ClaimResponse findById(Long id){return mapper.toResponse(existing(id));}
    @Override @Transactional public ClaimResponse update(Long id,ClaimRequest r){Claim e=existing(id);if(e.getStatus()!=ClaimStatus.SUBMITTED)throw new BusinessRuleViolationException("Only submitted claims can be updated");if(repository.existsByClaimNumberIgnoreCaseAndIdNot(r.getClaimNumber(),id))throw new DuplicateResourceException("Claim already exists: "+r.getClaimNumber());Member m=members.findById(r.getMemberId()).orElseThrow(()->new ResourceNotFoundException("Member not found: "+r.getMemberId()));Provider p=providers.findById(r.getProviderId()).orElseThrow(()->new ResourceNotFoundException("Provider not found: "+r.getProviderId()));if(m.getStatus()==MemberStatus.TERMINATED)throw new BusinessRuleViolationException("Claims cannot be updated for terminated members");mapper.copy(r,e);e.setMember(m);e.setProvider(p);return mapper.toResponse(repository.save(e));}
    @Override @Transactional public ClaimResponse adjudicate(Long id,ClaimAdjudicationRequest r){Claim e=existing(id);if(e.getStatus()!=ClaimStatus.SUBMITTED)throw new BusinessRuleViolationException("Only submitted claims can be adjudicated");if(r.getStatus()!=ClaimStatus.APPROVED&&r.getStatus()!=ClaimStatus.DENIED)throw new BusinessRuleViolationException("Claim status must be APPROVED or DENIED");if(r.getStatus()==ClaimStatus.APPROVED){if(r.getAllowedAmount()==null||r.getAllowedAmount().compareTo(BigDecimal.ZERO)<=0||r.getAllowedAmount().compareTo(e.getBilledAmount())>0)throw new BusinessRuleViolationException("Approved allowedAmount must be greater than zero and no more than billedAmount");e.setAllowedAmount(r.getAllowedAmount());e.setDenialReason(null);}else{if(r.getDenialReason()==null||r.getDenialReason().trim().isEmpty())throw new BusinessRuleViolationException("Denial reason is required");e.setAllowedAmount(BigDecimal.ZERO);e.setDenialReason(r.getDenialReason());}e.setStatus(r.getStatus());Claim saved=repository.save(e);events.publish(new DomainEvent("CLAIM_ADJUDICATED","Claim",String.valueOf(id),Collections.<String,Object>singletonMap("status",r.getStatus().name())));return mapper.toResponse(saved);}
    @Override @Transactional public void delete(Long id){Claim e=existing(id);if(e.getStatus()!=ClaimStatus.SUBMITTED)throw new BusinessRuleViolationException("Only submitted claims can be deleted");repository.delete(e);}
    private Claim existing(Long id){return repository.findById(id).orElseThrow(()->new ResourceNotFoundException("Claim not found: "+id));}
}

package com.scp.java.ocm.member.service.impl;

import java.util.Collections;

import javax.persistence.criteria.Predicate;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.scp.java.ocm.careplan.entity.CarePlanStatus;
import com.scp.java.ocm.careplan.repository.CarePlanRepository;
import com.scp.java.ocm.claim.repository.ClaimRepository;
import com.scp.java.ocm.common.event.DomainEvent;
import com.scp.java.ocm.common.event.DomainEventPublisher;
import com.scp.java.ocm.common.exception.BusinessRuleViolationException;
import com.scp.java.ocm.common.exception.DuplicateResourceException;
import com.scp.java.ocm.common.exception.ResourceNotFoundException;
import com.scp.java.ocm.member.dto.MemberRequest;
import com.scp.java.ocm.member.dto.MemberResponse;
import com.scp.java.ocm.member.entity.Member;
import com.scp.java.ocm.member.entity.MemberStatus;
import com.scp.java.ocm.member.mapper.MemberMapper;
import com.scp.java.ocm.member.repository.MemberRepository;
import com.scp.java.ocm.member.service.MemberService;

@Service
@Transactional(readOnly = true)
public class MemberServiceImpl implements MemberService {
    private final MemberRepository repository;
    private final MemberMapper mapper;
    private final CarePlanRepository carePlanRepository;
    private final ClaimRepository claimRepository;
    private final DomainEventPublisher events;

    public MemberServiceImpl(MemberRepository repository, MemberMapper mapper, CarePlanRepository carePlanRepository,
            ClaimRepository claimRepository, DomainEventPublisher events) {
        this.repository = repository; this.mapper = mapper; this.carePlanRepository = carePlanRepository;
        this.claimRepository = claimRepository; this.events = events;
    }

    @Override @Transactional
    public MemberResponse create(MemberRequest request) {
        checkDuplicate(request, null);
        Member saved = repository.save(mapper.toEntity(request));
        events.publish(new DomainEvent("MEMBER_ENROLLED", "Member", String.valueOf(saved.getId()),
                Collections.<String, Object>singletonMap("mrn", saved.getMrn())));
        return mapper.toResponse(saved);
    }

    @Override
    public Page<MemberResponse> findAll(Pageable pageable, MemberStatus status, String lastName, String planId) {
        Specification<Member> spec = (root, query, cb) -> {
            java.util.List<Predicate> ps = new java.util.ArrayList<Predicate>();
            if (status != null) ps.add(cb.equal(root.get("status"), status));
            if (lastName != null) ps.add(cb.like(cb.lower(root.get("lastName")), "%" + lastName.toLowerCase() + "%"));
            if (planId != null) ps.add(cb.equal(root.get("planId"), planId));
            return cb.and(ps.toArray(new Predicate[ps.size()]));
        };
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override public MemberResponse findById(Long id) { return mapper.toResponse(existing(id)); }

    @Override @Transactional
    public MemberResponse update(Long id, MemberRequest request) {
        Member member = existing(id);
        checkDuplicate(request, id);
        mapper.copy(request, member);
        return mapper.toResponse(repository.save(member));
    }

    @Override @Transactional
    public MemberResponse changeStatus(Long id, MemberStatus status) {
        Member member = existing(id);
        if (member.getStatus() == MemberStatus.TERMINATED && status != MemberStatus.TERMINATED) {
            throw new BusinessRuleViolationException("Terminated members cannot change status");
        }
        if (status == MemberStatus.TERMINATED && carePlanRepository.existsByMemberIdAndStatus(id, CarePlanStatus.ACTIVE)) {
            throw new BusinessRuleViolationException("Cancel or complete active care plans first");
        }
        member.setStatus(status);
        Member saved = repository.save(member);
        events.publish(new DomainEvent("MEMBER_STATUS_CHANGED", "Member", String.valueOf(id),
                Collections.<String, Object>singletonMap("status", status.name())));
        return mapper.toResponse(saved);
    }

    @Override @Transactional
    public void delete(Long id) {
        Member member = existing(id);
        if (carePlanRepository.existsByMemberId(id) || claimRepository.existsByMemberId(id)) {
            throw new BusinessRuleViolationException("Member cannot be deleted while care plans or claims reference it");
        }
        repository.delete(member);
    }

    private void checkDuplicate(MemberRequest r, Long id) {
        if (id == null ? repository.existsByMrnIgnoreCase(r.getMrn()) : repository.existsByMrnIgnoreCaseAndIdNot(r.getMrn(), id)) {
            throw new DuplicateResourceException("Member already exists with MRN: " + r.getMrn());
        }
        if (r.getEmail() != null && (id == null ? repository.existsByEmailIgnoreCase(r.getEmail())
                : repository.existsByEmailIgnoreCaseAndIdNot(r.getEmail(), id))) {
            throw new DuplicateResourceException("Member already exists with email: " + r.getEmail());
        }
    }

    private Member existing(Long id) {
        return repository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Member not found: " + id));
    }
}

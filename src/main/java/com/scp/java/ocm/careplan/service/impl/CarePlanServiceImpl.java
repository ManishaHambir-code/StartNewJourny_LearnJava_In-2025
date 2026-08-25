package com.scp.java.ocm.careplan.service.impl;

import com.scp.java.ocm.careplan.dto.CarePlanRequest;
import com.scp.java.ocm.careplan.dto.CarePlanResponse;
import com.scp.java.ocm.careplan.entity.CarePlan;
import com.scp.java.ocm.careplan.entity.CarePlanStatus;
import com.scp.java.ocm.careplan.entity.ProgramType;
import com.scp.java.ocm.careplan.mapper.CarePlanMapper;
import com.scp.java.ocm.careplan.repository.CarePlanRepository;
import com.scp.java.ocm.careplan.service.CarePlanService;
import com.scp.java.ocm.common.event.DomainEvent;
import com.scp.java.ocm.common.event.DomainEventPublisher;
import com.scp.java.ocm.common.exception.BusinessRuleViolationException;
import com.scp.java.ocm.common.exception.DuplicateResourceException;
import com.scp.java.ocm.common.exception.ResourceNotFoundException;
import com.scp.java.ocm.member.entity.Member;
import com.scp.java.ocm.member.entity.MemberStatus;
import com.scp.java.ocm.member.repository.MemberRepository;
import com.scp.java.ocm.provider.entity.Provider;
import com.scp.java.ocm.provider.repository.ProviderRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import javax.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class CarePlanServiceImpl implements CarePlanService {
    private final CarePlanRepository repository;
    private final CarePlanMapper mapper;
    private final MemberRepository members;
    private final ProviderRepository providers;
    private final DomainEventPublisher events;

    public CarePlanServiceImpl(
            CarePlanRepository repository,
            CarePlanMapper mapper,
            MemberRepository members,
            ProviderRepository providers,
            DomainEventPublisher events) {
        this.repository = repository;
        this.mapper = mapper;
        this.members = members;
        this.providers = providers;
        this.events = events;
    }

    @Override
    @Transactional
    public CarePlanResponse create(CarePlanRequest r) {
        Member m = member(r.getMemberId());
        Provider p = provider(r.getCareManagerId());
        if (m.getStatus() != MemberStatus.ACTIVE)
            throw new BusinessRuleViolationException("Care plans require an active member");
        if (!p.isActive())
            throw new BusinessRuleViolationException("Care manager provider must be active");
        if (r.getEndDate() != null && r.getEndDate().isBefore(r.getStartDate()))
            throw new BusinessRuleViolationException("endDate cannot be before startDate");
        if (repository.existsByMemberIdAndProgramAndStatusIn(
                m.getId(), r.getProgram(), Arrays.asList(CarePlanStatus.DRAFT, CarePlanStatus.ACTIVE)))
            throw new DuplicateResourceException(
                    "An active or draft care plan already exists for this member and program");
        CarePlan saved = repository.save(mapper.toEntity(r, m, p));
        events.publish(
                new DomainEvent(
                        "CARE_PLAN_CREATED",
                        "CarePlan",
                        String.valueOf(saved.getId()),
                        Collections.<String, Object>singletonMap("memberId", m.getId())));
        return mapper.toResponse(saved);
    }

    @Override
    public Page<CarePlanResponse> findAll(
            Pageable pageable, Long memberId, CarePlanStatus status, ProgramType program) {
        Specification<CarePlan> spec =
                (root, q, cb) -> {
                    List<Predicate> ps = new ArrayList<Predicate>();
                    if (memberId != null) ps.add(cb.equal(root.get("member").get("id"), memberId));
                    if (status != null) ps.add(cb.equal(root.get("status"), status));
                    if (program != null) ps.add(cb.equal(root.get("program"), program));
                    return cb.and(ps.toArray(new Predicate[ps.size()]));
                };
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    public Page<CarePlanResponse> findByMember(Long memberId, Pageable pageable) {
        if (!members.existsById(memberId))
            throw new ResourceNotFoundException("Member not found: " + memberId);
        return repository.findByMemberId(memberId, pageable).map(mapper::toResponse);
    }

    @Override
    public CarePlanResponse findById(Long id) {
        return mapper.toResponse(existing(id));
    }

    @Override
    @Transactional
    public CarePlanResponse update(Long id, CarePlanRequest r) {
        CarePlan e = existing(id);
        if (e.getStatus() == CarePlanStatus.COMPLETED || e.getStatus() == CarePlanStatus.CANCELLED)
            throw new BusinessRuleViolationException(
                    "Completed or cancelled care plans cannot be updated");
        Member m = member(r.getMemberId());
        Provider p = provider(r.getCareManagerId());
        if (m.getStatus() != MemberStatus.ACTIVE)
            throw new BusinessRuleViolationException("Care plans require an active member");
        if (!p.isActive())
            throw new BusinessRuleViolationException("Care manager provider must be active");
        if (r.getEndDate() != null && r.getEndDate().isBefore(r.getStartDate()))
            throw new BusinessRuleViolationException("endDate cannot be before startDate");
        mapper.copy(r, e);
        e.setMember(m);
        e.setCareManager(p);
        return mapper.toResponse(repository.save(e));
    }

    @Override
    @Transactional
    public CarePlanResponse activate(Long id) {
        CarePlan e = existing(id);
        transition(e, CarePlanStatus.DRAFT, CarePlanStatus.ACTIVE);
        CarePlan x = repository.save(e);
        events.publish(
                new DomainEvent(
                        "CARE_PLAN_ACTIVATED",
                        "CarePlan",
                        String.valueOf(id),
                        Collections.<String, Object>emptyMap()));
        return mapper.toResponse(x);
    }

    @Override
    @Transactional
    public CarePlanResponse complete(Long id) {
        CarePlan e = existing(id);
        transition(e, CarePlanStatus.ACTIVE, CarePlanStatus.COMPLETED);
        if (e.getEndDate() == null) e.setEndDate(LocalDate.now());
        CarePlan x = repository.save(e);
        events.publish(
                new DomainEvent(
                        "CARE_PLAN_COMPLETED",
                        "CarePlan",
                        String.valueOf(id),
                        Collections.<String, Object>emptyMap()));
        return mapper.toResponse(x);
    }

    private void transition(CarePlan e, CarePlanStatus from, CarePlanStatus to) {
        if (e.getStatus() != from) {
            throw new BusinessRuleViolationException(
                    "Care plan can transition to " + to + " only from " + from);
        }
        e.setStatus(to);
    }

    private CarePlan existing(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Care plan not found: " + id));
    }

    private Member member(Long id) {
        return members
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Member not found: " + id));
    }

    private Provider provider(Long id) {
        return providers
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + id));
    }
}

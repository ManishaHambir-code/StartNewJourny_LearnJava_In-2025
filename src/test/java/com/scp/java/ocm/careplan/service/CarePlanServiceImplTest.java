package com.scp.java.ocm.careplan.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.scp.java.ocm.careplan.dto.CarePlanRequest;
import com.scp.java.ocm.careplan.dto.CarePlanResponse;
import com.scp.java.ocm.careplan.entity.CarePlan;
import com.scp.java.ocm.careplan.entity.CarePlanStatus;
import com.scp.java.ocm.careplan.entity.ProgramType;
import com.scp.java.ocm.careplan.entity.RiskLevel;
import com.scp.java.ocm.careplan.mapper.CarePlanMapper;
import com.scp.java.ocm.careplan.repository.CarePlanRepository;
import com.scp.java.ocm.careplan.service.impl.CarePlanServiceImpl;
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
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CarePlanServiceImplTest {
    @Mock CarePlanRepository repository;
    @Mock MemberRepository members;
    @Mock ProviderRepository providers;
    @Mock DomainEventPublisher events;
    CarePlanServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new CarePlanServiceImpl(repository, new CarePlanMapper(), members, providers, events);
    }

    @Test
    void unknownMemberIsNotFound() {
        when(members.findById(1L)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class, () -> service.create(request()));
    }

    @Test
    void inactiveMemberRejected() {
        Member m = member();
        m.setStatus(MemberStatus.INACTIVE);
        when(members.findById(1L)).thenReturn(Optional.of(m));
        when(providers.findById(2L)).thenReturn(Optional.of(provider()));
        assertThrows(BusinessRuleViolationException.class, () -> service.create(request()));
    }

    @Test
    void secondDraftRejected() {
        Member m = member();
        Provider p = provider();
        when(members.findById(1L)).thenReturn(Optional.of(m));
        when(providers.findById(2L)).thenReturn(Optional.of(p));
        when(repository.existsByMemberIdAndProgramAndStatusIn(
                        eq(1L), eq(ProgramType.DIABETES_MANAGEMENT), anyCollection()))
                .thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.create(request()));
    }

    @Test
    void activateRequiresDraft() {
        CarePlan p = plan();
        p.setStatus(CarePlanStatus.ACTIVE);
        when(repository.findById(1L)).thenReturn(Optional.of(p));
        assertThrows(BusinessRuleViolationException.class, () -> service.activate(1L));
    }

    @Test
    void activateChangesDraftToActiveAndPublishesEvent() {
        CarePlan plan = plan();
        plan.setStatus(CarePlanStatus.DRAFT);
        when(repository.findById(1L)).thenReturn(Optional.of(plan));
        when(repository.save(plan)).thenReturn(plan);

        CarePlanResponse response = service.activate(1L);

    assertEquals(CarePlanStatus.ACTIVE, plan.getStatus());
    assertEquals(CarePlanStatus.ACTIVE, response.getStatus());
    ArgumentCaptor<DomainEvent> event = ArgumentCaptor.forClass(DomainEvent.class);
    verify(events).publish(event.capture());
    assertEquals("CARE_PLAN_ACTIVATED", event.getValue().getEventType());
    }

    @Test
    void completeChangesActiveToCompletedDefaultsEndDateAndPublishesEvent() {
        CarePlan plan = plan();
        plan.setStatus(CarePlanStatus.ACTIVE);
        plan.setEndDate(null);
        when(repository.findById(1L)).thenReturn(Optional.of(plan));
        when(repository.save(plan)).thenReturn(plan);

        CarePlanResponse response = service.complete(1L);

        assertEquals(CarePlanStatus.COMPLETED, plan.getStatus());
    assertEquals(CarePlanStatus.COMPLETED, response.getStatus());
    assertEquals(LocalDate.now(), plan.getEndDate());
    ArgumentCaptor<DomainEvent> event = ArgumentCaptor.forClass(DomainEvent.class);
    verify(events).publish(event.capture());
    assertEquals("CARE_PLAN_COMPLETED", event.getValue().getEventType());
    }

    @Test
    void endDateBeforeStartDateRejected() {
        Member m = member();
        Provider p = provider();
        CarePlanRequest r = request();
        r.setEndDate(r.getStartDate().minusDays(1));
        when(members.findById(1L)).thenReturn(Optional.of(m));
        when(providers.findById(2L)).thenReturn(Optional.of(p));

        assertThrows(BusinessRuleViolationException.class, () -> service.create(r));
    }

    @Test
    void completedPlanCannotBeUpdated() {
        CarePlan plan = plan();
        plan.setStatus(CarePlanStatus.COMPLETED);
        when(repository.findById(1L)).thenReturn(Optional.of(plan));

        assertThrows(BusinessRuleViolationException.class, () -> service.update(1L, request()));
    }

    @Test
    void cancelledPlanCannotBeUpdated() {
        CarePlan plan = plan();
        plan.setStatus(CarePlanStatus.CANCELLED);
        when(repository.findById(1L)).thenReturn(Optional.of(plan));

        assertThrows(BusinessRuleViolationException.class, () -> service.update(1L, request()));
    }

    private CarePlanRequest request() {
        CarePlanRequest r = new CarePlanRequest();
        r.setMemberId(1L);
        r.setCareManagerId(2L);
        r.setProgram(ProgramType.DIABETES_MANAGEMENT);
        r.setRiskLevel(RiskLevel.LOW);
        r.setStartDate(LocalDate.now());
        return r;
    }

    private Member member() {
        Member m = new Member();
        m.setId(1L);
        m.setStatus(MemberStatus.ACTIVE);
        return m;
    }

    private Provider provider() {
        Provider p = new Provider();
        p.setId(2L);
        p.setActive(true);
        return p;
    }

    private CarePlan plan() {
        CarePlan p = new CarePlan();
        p.setId(1L);
        p.setMember(member());
        p.setCareManager(provider());
        p.setProgram(ProgramType.DIABETES_MANAGEMENT);
        p.setRiskLevel(RiskLevel.LOW);
        p.setStartDate(LocalDate.now());
        return p;
    }
}

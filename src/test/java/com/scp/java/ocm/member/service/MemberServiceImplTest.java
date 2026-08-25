package com.scp.java.ocm.member.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDate;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import com.scp.java.ocm.careplan.entity.CarePlanStatus;
import com.scp.java.ocm.careplan.repository.CarePlanRepository;
import com.scp.java.ocm.claim.repository.ClaimRepository;
import com.scp.java.ocm.common.event.DomainEventPublisher;
import com.scp.java.ocm.common.exception.*;
import com.scp.java.ocm.member.dto.MemberRequest;
import com.scp.java.ocm.member.entity.*;
import com.scp.java.ocm.member.mapper.MemberMapper;
import com.scp.java.ocm.member.repository.MemberRepository;
import com.scp.java.ocm.member.service.impl.MemberServiceImpl;

public class MemberServiceImplTest {
    @Mock private MemberRepository repository;
    @Mock private CarePlanRepository carePlans;
    @Mock private ClaimRepository claims;
    @Mock private DomainEventPublisher events;
    private MemberServiceImpl service;

    @BeforeEach void setUp() { MockitoAnnotations.initMocks(this); service = new MemberServiceImpl(repository,
            new MemberMapper(), carePlans, claims, events); }

    @Test void createsAndPublishesEnrollment() {
        MemberRequest request = request();
        when(repository.save(any(Member.class))).thenAnswer(i -> { Member m=i.getArgument(0); m.setId(1L); return m; });
        assertEquals("MRN1", service.create(request).getMrn());
        verify(events).publish(any());
    }

    @Test void rejectsDuplicateMrn() {
        when(repository.existsByMrnIgnoreCase("MRN1")).thenReturn(true);
        assertThrows(DuplicateResourceException.class, () -> service.create(request()));
    }

    @Test void terminatedMemberCannotLeaveTerminalState() {
        Member m = requestEntity(); m.setId(1L); m.setStatus(MemberStatus.TERMINATED);
        when(repository.findById(1L)).thenReturn(Optional.of(m));
        assertThrows(BusinessRuleViolationException.class, () -> service.changeStatus(1L, MemberStatus.ACTIVE));
    }

    @Test void activePlanPreventsTermination() {
        Member m = requestEntity(); m.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(carePlans.existsByMemberIdAndStatus(1L, CarePlanStatus.ACTIVE)).thenReturn(true);
        assertThrows(BusinessRuleViolationException.class, () -> service.changeStatus(1L, MemberStatus.TERMINATED));
    }

    @Test void referencesPreventDelete() {
        Member m = requestEntity(); m.setId(1L);
        when(repository.findById(1L)).thenReturn(Optional.of(m));
        when(claims.existsByMemberId(1L)).thenReturn(true);
        assertThrows(BusinessRuleViolationException.class, () -> service.delete(1L));
    }

    private MemberRequest request() { MemberRequest r=new MemberRequest(); r.setMrn("MRN1");r.setFirstName("A");r.setLastName("B");r.setDateOfBirth(LocalDate.of(1980,1,1));r.setGender(Gender.OTHER);r.setPlanId("P1");r.setEnrollmentDate(LocalDate.now());return r; }
    private Member requestEntity(){return new MemberMapper().toEntity(request());}
}

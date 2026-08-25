package com.scp.java.ocm.careplan.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.scp.java.ocm.careplan.dto.*;
import com.scp.java.ocm.careplan.entity.*;
import com.scp.java.ocm.careplan.mapper.CarePlanMapper;
import com.scp.java.ocm.careplan.repository.CarePlanRepository;
import com.scp.java.ocm.careplan.service.impl.CarePlanServiceImpl;
import com.scp.java.ocm.common.event.DomainEventPublisher;
import com.scp.java.ocm.common.exception.*;
import com.scp.java.ocm.member.entity.*;
import com.scp.java.ocm.member.repository.MemberRepository;
import com.scp.java.ocm.provider.entity.*;
import com.scp.java.ocm.provider.repository.ProviderRepository;

public class CarePlanServiceImplTest {
    @Mock CarePlanRepository repository; @Mock MemberRepository members; @Mock ProviderRepository providers; @Mock DomainEventPublisher events;
    CarePlanServiceImpl service;
    @BeforeEach void setUp(){MockitoAnnotations.initMocks(this);service=new CarePlanServiceImpl(repository,new CarePlanMapper(),members,providers,events);}
    @Test void unknownMemberIsNotFound(){when(members.findById(1L)).thenReturn(Optional.empty());assertThrows(ResourceNotFoundException.class,()->service.create(request()));}
    @Test void inactiveMemberRejected(){Member m=member();m.setStatus(MemberStatus.INACTIVE);when(members.findById(1L)).thenReturn(Optional.of(m));when(providers.findById(2L)).thenReturn(Optional.of(provider()));assertThrows(BusinessRuleViolationException.class,()->service.create(request()));}
    @Test void secondDraftRejected(){Member m=member();Provider p=provider();when(members.findById(1L)).thenReturn(Optional.of(m));when(providers.findById(2L)).thenReturn(Optional.of(p));when(repository.existsByMemberIdAndProgramAndStatusIn(eq(1L),eq(ProgramType.DIABETES_MANAGEMENT),anyCollection())).thenReturn(true);assertThrows(DuplicateResourceException.class,()->service.create(request()));}
    @Test void activateRequiresDraft(){CarePlan p=plan();p.setStatus(CarePlanStatus.ACTIVE);when(repository.findById(1L)).thenReturn(Optional.of(p));assertThrows(BusinessRuleViolationException.class,()->service.activate(1L));}
    private CarePlanRequest request(){CarePlanRequest r=new CarePlanRequest();r.setMemberId(1L);r.setCareManagerId(2L);r.setProgram(ProgramType.DIABETES_MANAGEMENT);r.setRiskLevel(RiskLevel.LOW);r.setStartDate(LocalDate.now());return r;}
    private Member member(){Member m=new Member();m.setId(1L);m.setStatus(MemberStatus.ACTIVE);return m;}
    private Provider provider(){Provider p=new Provider();p.setId(2L);p.setActive(true);return p;}
    private CarePlan plan(){CarePlan p=new CarePlan();p.setId(1L);return p;}
}

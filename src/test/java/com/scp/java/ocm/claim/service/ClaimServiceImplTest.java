package com.scp.java.ocm.claim.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.*;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.scp.java.ocm.claim.dto.*;
import com.scp.java.ocm.claim.entity.*;
import com.scp.java.ocm.claim.mapper.ClaimMapper;
import com.scp.java.ocm.claim.repository.ClaimRepository;
import com.scp.java.ocm.claim.service.impl.ClaimServiceImpl;
import com.scp.java.ocm.common.event.DomainEventPublisher;
import com.scp.java.ocm.common.exception.*;
import com.scp.java.ocm.member.entity.*;
import com.scp.java.ocm.member.repository.MemberRepository;
import com.scp.java.ocm.provider.entity.Provider;
import com.scp.java.ocm.provider.repository.ProviderRepository;

public class ClaimServiceImplTest {
    @Mock ClaimRepository repository; @Mock MemberRepository members; @Mock ProviderRepository providers; @Mock DomainEventPublisher events;
    ClaimServiceImpl service;
    @BeforeEach void setUp(){MockitoAnnotations.initMocks(this);service=new ClaimServiceImpl(repository,new ClaimMapper(),members,providers,events);}
    @Test void createForTerminatedMemberRejected(){Member m=new Member();m.setStatus(MemberStatus.TERMINATED);when(members.findById(1L)).thenReturn(Optional.of(m));when(providers.findById(2L)).thenReturn(Optional.of(new Provider()));assertThrows(BusinessRuleViolationException.class,()->service.create(request()));}
    @Test void approvedClaimRequiresAllowedAmount(){Claim c=claim();when(repository.findById(1L)).thenReturn(Optional.of(c));ClaimAdjudicationRequest r=new ClaimAdjudicationRequest();r.setStatus(ClaimStatus.APPROVED);assertThrows(BusinessRuleViolationException.class,()->service.adjudicate(1L,r));}
    @Test void deniedClaimRequiresReason(){Claim c=claim();when(repository.findById(1L)).thenReturn(Optional.of(c));ClaimAdjudicationRequest r=new ClaimAdjudicationRequest();r.setStatus(ClaimStatus.DENIED);assertThrows(BusinessRuleViolationException.class,()->service.adjudicate(1L,r));}
    @Test void onlySubmittedClaimsCanDelete(){Claim c=claim();c.setStatus(ClaimStatus.PAID);when(repository.findById(1L)).thenReturn(Optional.of(c));assertThrows(BusinessRuleViolationException.class,()->service.delete(1L));}
    private ClaimRequest request(){ClaimRequest r=new ClaimRequest();r.setClaimNumber("C1");r.setMemberId(1L);r.setProviderId(2L);r.setServiceDate(LocalDate.now());r.setBilledAmount(BigDecimal.TEN);r.setDiagnosisCode("E11.9");return r;}
    private Claim claim(){Claim c=new Claim();c.setId(1L);c.setStatus(ClaimStatus.SUBMITTED);c.setBilledAmount(BigDecimal.TEN);c.setMember(new Member());c.setProvider(new Provider());return c;}
}

package com.scp.java.ocm.claim.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.scp.java.ocm.claim.dto.ClaimAdjudicationRequest;
import com.scp.java.ocm.claim.dto.ClaimRequest;
import com.scp.java.ocm.claim.dto.ClaimResponse;
import com.scp.java.ocm.claim.entity.Claim;
import com.scp.java.ocm.claim.entity.ClaimStatus;
import com.scp.java.ocm.claim.mapper.ClaimMapper;
import com.scp.java.ocm.claim.repository.ClaimRepository;
import com.scp.java.ocm.claim.service.impl.ClaimServiceImpl;
import com.scp.java.ocm.common.event.DomainEventPublisher;
import com.scp.java.ocm.common.exception.BusinessRuleViolationException;
import com.scp.java.ocm.member.entity.Member;
import com.scp.java.ocm.member.entity.MemberStatus;
import com.scp.java.ocm.member.repository.MemberRepository;
import com.scp.java.ocm.provider.entity.Provider;
import com.scp.java.ocm.provider.repository.ProviderRepository;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClaimServiceImplTest {
    @Mock ClaimRepository repository;
    @Mock MemberRepository members;
    @Mock ProviderRepository providers;
    @Mock DomainEventPublisher events;
    ClaimServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new ClaimServiceImpl(repository, new ClaimMapper(), members, providers, events);
    }

    @Test
    void createForTerminatedMemberRejected() {
        Member m = new Member();
        m.setStatus(MemberStatus.TERMINATED);
        when(members.findById(1L)).thenReturn(Optional.of(m));
        when(providers.findById(2L)).thenReturn(Optional.of(new Provider()));
        assertThrows(BusinessRuleViolationException.class, () -> service.create(request()));
    }

    @Test
    void createForcesSubmittedAndIgnoresAdjudicationFields() {
        Member member = new Member();
        member.setId(1L);
        member.setStatus(MemberStatus.ACTIVE);
        Provider provider = new Provider();
        provider.setId(2L);
        ClaimRequest request = request();
        request.setAllowedAmount(BigDecimal.ONE);
        request.setDenialReason("client supplied");
        when(repository.existsByClaimNumberIgnoreCase("C1")).thenReturn(false);
        when(members.findById(1L)).thenReturn(Optional.of(member));
        when(providers.findById(2L)).thenReturn(Optional.of(provider));
        when(repository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));

        ClaimResponse response = service.create(request);

        assertEquals(ClaimStatus.SUBMITTED, response.getStatus());
        assertNull(response.getAllowedAmount());
        assertNull(response.getDenialReason());
    }

    @Test
    void approvedClaimRequiresAllowedAmount() {
        Claim c = claim();
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        ClaimAdjudicationRequest r = new ClaimAdjudicationRequest();
        r.setStatus(ClaimStatus.APPROVED);
        assertThrows(BusinessRuleViolationException.class, () -> service.adjudicate(1L, r));
    }

    @Test
    void deniedClaimRequiresReason() {
        Claim c = claim();
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        ClaimAdjudicationRequest r = new ClaimAdjudicationRequest();
        r.setStatus(ClaimStatus.DENIED);
        assertThrows(BusinessRuleViolationException.class, () -> service.adjudicate(1L, r));
    }

    @Test
    void adjudicationRequiresSubmittedClaim() {
        Claim claim = claim();
        claim.setStatus(ClaimStatus.APPROVED);
        when(repository.findById(1L)).thenReturn(Optional.of(claim));
        ClaimAdjudicationRequest request = new ClaimAdjudicationRequest();
        request.setStatus(ClaimStatus.DENIED);
        request.setDenialReason("not covered");

        assertThrows(BusinessRuleViolationException.class, () -> service.adjudicate(1L, request));
    }

    @Test
    void approvedAmountCannotExceedBilledAmount() {
        Claim claim = claim();
        when(repository.findById(1L)).thenReturn(Optional.of(claim));
        ClaimAdjudicationRequest request = new ClaimAdjudicationRequest();
        request.setStatus(ClaimStatus.APPROVED);
        request.setAllowedAmount(BigDecimal.valueOf(11));

        assertThrows(BusinessRuleViolationException.class, () -> service.adjudicate(1L, request));
    }

    @Test
    void approvedClaimStoresStatusAndAllowedAmount() {
        Claim claim = claim();
        when(repository.findById(1L)).thenReturn(Optional.of(claim));
        when(repository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));
        ClaimAdjudicationRequest request = new ClaimAdjudicationRequest();
        request.setStatus(ClaimStatus.APPROVED);
        request.setAllowedAmount(BigDecimal.valueOf(8));

        ClaimResponse response = service.adjudicate(1L, request);

        assertEquals(ClaimStatus.APPROVED, response.getStatus());
        assertEquals(BigDecimal.valueOf(8), response.getAllowedAmount());
    }

    @Test
    void deniedClaimStoresReasonAndZeroAllowedAmount() {
        Claim claim = claim();
        when(repository.findById(1L)).thenReturn(Optional.of(claim));
        when(repository.save(any(Claim.class))).thenAnswer(i -> i.getArgument(0));
        ClaimAdjudicationRequest request = new ClaimAdjudicationRequest();
        request.setStatus(ClaimStatus.DENIED);
        request.setDenialReason("not covered");

        ClaimResponse response = service.adjudicate(1L, request);

        assertEquals(ClaimStatus.DENIED, response.getStatus());
        assertEquals(BigDecimal.ZERO, response.getAllowedAmount());
        assertEquals("not covered", response.getDenialReason());
    }

    @Test
    void onlySubmittedClaimsCanDelete() {
        Claim c = claim();
        c.setStatus(ClaimStatus.PAID);
        when(repository.findById(1L)).thenReturn(Optional.of(c));
        assertThrows(BusinessRuleViolationException.class, () -> service.delete(1L));
    }

    private ClaimRequest request() {
        ClaimRequest r = new ClaimRequest();
        r.setClaimNumber("C1");
        r.setMemberId(1L);
        r.setProviderId(2L);
        r.setServiceDate(LocalDate.now());
        r.setBilledAmount(BigDecimal.TEN);
        r.setDiagnosisCode("E11.9");
        return r;
    }

    private Claim claim() {
        Claim c = new Claim();
        c.setId(1L);
        c.setStatus(ClaimStatus.SUBMITTED);
        c.setBilledAmount(BigDecimal.TEN);
        c.setMember(new Member());
        c.setProvider(new Provider());
        return c;
    }
}

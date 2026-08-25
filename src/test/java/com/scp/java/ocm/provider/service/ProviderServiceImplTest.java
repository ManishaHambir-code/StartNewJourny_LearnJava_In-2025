package com.scp.java.ocm.provider.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import java.util.Optional;
import org.junit.jupiter.api.*;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import com.scp.java.ocm.careplan.entity.CarePlanStatus;
import com.scp.java.ocm.careplan.repository.CarePlanRepository;
import com.scp.java.ocm.claim.repository.ClaimRepository;
import com.scp.java.ocm.common.exception.*;
import com.scp.java.ocm.provider.dto.*;
import com.scp.java.ocm.provider.entity.*;
import com.scp.java.ocm.provider.mapper.ProviderMapper;
import com.scp.java.ocm.provider.repository.ProviderRepository;
import com.scp.java.ocm.provider.service.impl.ProviderServiceImpl;

public class ProviderServiceImplTest {
    @Mock ProviderRepository repository; @Mock CarePlanRepository plans; @Mock ClaimRepository claims;
    ProviderServiceImpl service;
    @BeforeEach void setUp(){MockitoAnnotations.initMocks(this);service=new ProviderServiceImpl(repository,new ProviderMapper(),plans,claims);}
    @Test void rejectsDuplicateNpi(){when(repository.existsByNpi("1234567890")).thenReturn(true);assertThrows(DuplicateResourceException.class,()->service.create(request()));}
    @Test void preventsDeactivationWithActivePlan(){Provider p=provider();p.setId(1L);when(repository.findById(1L)).thenReturn(Optional.of(p));when(plans.existsByCareManagerIdAndStatus(1L,CarePlanStatus.ACTIVE)).thenReturn(true);ProviderRequest r=request();r.setActive(false);assertThrows(BusinessRuleViolationException.class,()->service.update(1L,r));}
    @Test void preventsDeleteWhenReferenced(){Provider p=provider();p.setId(1L);when(repository.findById(1L)).thenReturn(Optional.of(p));when(claims.existsByProviderId(1L)).thenReturn(true);assertThrows(BusinessRuleViolationException.class,()->service.delete(1L));}
    private ProviderRequest request(){ProviderRequest r=new ProviderRequest();r.setNpi("1234567890");r.setFirstName("A");r.setLastName("B");r.setSpecialty(ProviderSpecialty.PRIMARY_CARE);return r;}
    private Provider provider(){return new ProviderMapper().toEntity(request());}
}

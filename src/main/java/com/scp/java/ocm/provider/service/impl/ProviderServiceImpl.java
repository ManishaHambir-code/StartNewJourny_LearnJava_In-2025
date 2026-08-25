package com.scp.java.ocm.provider.service.impl;

import com.scp.java.ocm.careplan.entity.CarePlanStatus;
import com.scp.java.ocm.careplan.repository.CarePlanRepository;
import com.scp.java.ocm.claim.repository.ClaimRepository;
import com.scp.java.ocm.common.exception.BusinessRuleViolationException;
import com.scp.java.ocm.common.exception.DuplicateResourceException;
import com.scp.java.ocm.common.exception.ResourceNotFoundException;
import com.scp.java.ocm.provider.dto.ProviderRequest;
import com.scp.java.ocm.provider.dto.ProviderResponse;
import com.scp.java.ocm.provider.entity.Provider;
import com.scp.java.ocm.provider.entity.ProviderSpecialty;
import com.scp.java.ocm.provider.mapper.ProviderMapper;
import com.scp.java.ocm.provider.repository.ProviderRepository;
import com.scp.java.ocm.provider.service.ProviderService;
import java.util.ArrayList;
import java.util.List;
import javax.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ProviderServiceImpl implements ProviderService {
    private final ProviderRepository repository;
    private final ProviderMapper mapper;
    private final CarePlanRepository carePlans;
    private final ClaimRepository claims;

    public ProviderServiceImpl(
            ProviderRepository repository,
            ProviderMapper mapper,
            CarePlanRepository carePlans,
            ClaimRepository claims) {
        this.repository = repository;
        this.mapper = mapper;
        this.carePlans = carePlans;
        this.claims = claims;
    }

    @Override
    @Transactional
    public ProviderResponse create(ProviderRequest r) {
        check(r, null);
        return mapper.toResponse(repository.save(mapper.toEntity(r)));
    }

    @Override
    public Page<ProviderResponse> findAll(
            Pageable pageable, ProviderSpecialty specialty, Boolean active) {
        Specification<Provider> spec =
                (root, q, cb) -> {
                    List<Predicate> p = new ArrayList<Predicate>();
                    if (specialty != null) p.add(cb.equal(root.get("specialty"), specialty));
                    if (active != null) p.add(cb.equal(root.get("active"), active));
                    return cb.and(p.toArray(new Predicate[p.size()]));
                };
        return repository.findAll(spec, pageable).map(mapper::toResponse);
    }

    @Override
    public ProviderResponse findById(Long id) {
        return mapper.toResponse(existing(id));
    }

    @Override
    @Transactional
    public ProviderResponse update(Long id, ProviderRequest r) {
        Provider p = existing(id);
        check(r, id);
        if (p.isActive()
                && Boolean.FALSE.equals(r.getActive())
                && carePlans.existsByCareManagerIdAndStatus(id, CarePlanStatus.ACTIVE)) {
            throw new BusinessRuleViolationException(
                    "Provider cannot be deactivated while managing active care plans");
        }
        mapper.copy(r, p);
        return mapper.toResponse(repository.save(p));
    }

    @Override
    @Transactional
    public void delete(Long id) {
        Provider p = existing(id);
        if (carePlans.existsByCareManagerId(id) || claims.existsByProviderId(id)) {
            throw new BusinessRuleViolationException(
                    "Provider cannot be deleted while care plans or claims reference it");
        }
        repository.delete(p);
    }

    private void check(ProviderRequest r, Long id) {
        boolean npi =
                id == null
                        ? repository.existsByNpi(r.getNpi())
                        : repository.existsByNpiAndIdNot(r.getNpi(), id);
        boolean email =
                r.getEmail() != null
                        && (id == null
                                ? repository.existsByEmailIgnoreCase(r.getEmail())
                                : repository.existsByEmailIgnoreCaseAndIdNot(r.getEmail(), id));
        if (npi)
            throw new DuplicateResourceException("Provider already exists with NPI: " + r.getNpi());
        if (email)
            throw new DuplicateResourceException("Provider already exists with email: " + r.getEmail());
    }

    private Provider existing(Long id) {
        return repository
                .findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Provider not found: " + id));
    }
}

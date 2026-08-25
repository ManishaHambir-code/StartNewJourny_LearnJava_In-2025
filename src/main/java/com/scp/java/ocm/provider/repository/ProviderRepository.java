package com.scp.java.ocm.provider.repository;

import com.scp.java.ocm.provider.entity.Provider;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ProviderRepository
        extends JpaRepository<Provider, Long>, JpaSpecificationExecutor<Provider> {
    boolean existsByNpi(String npi);

    boolean existsByNpiAndIdNot(String npi, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}

package com.scp.java.ocm.security.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.scp.java.ocm.security.entity.AppUser;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findByUsernameIgnoreCase(String username);
    boolean existsByUsernameIgnoreCase(String username);
}

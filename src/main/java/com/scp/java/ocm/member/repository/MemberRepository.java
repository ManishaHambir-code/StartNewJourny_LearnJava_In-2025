package com.scp.java.ocm.member.repository;

import com.scp.java.ocm.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MemberRepository
        extends JpaRepository<Member, Long>, JpaSpecificationExecutor<Member> {
    boolean existsByMrnIgnoreCase(String mrn);

    boolean existsByMrnIgnoreCaseAndIdNot(String mrn, Long id);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndIdNot(String email, Long id);
}

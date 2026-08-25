package com.scp.java.ocm.member.service;

import com.scp.java.ocm.member.dto.MemberRequest;
import com.scp.java.ocm.member.dto.MemberResponse;
import com.scp.java.ocm.member.entity.MemberStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberService {
    MemberResponse create(MemberRequest request);

    Page<MemberResponse> findAll(
            Pageable pageable, MemberStatus status, String lastName, String planId);

    MemberResponse findById(Long id);

    MemberResponse update(Long id, MemberRequest request);

    MemberResponse changeStatus(Long id, MemberStatus status);

    void delete(Long id);
}

package com.scp.java.ocm.member.mapper;

import org.springframework.stereotype.Component;

import com.scp.java.ocm.member.dto.MemberRequest;
import com.scp.java.ocm.member.dto.MemberResponse;
import com.scp.java.ocm.member.entity.Member;
import com.scp.java.ocm.member.entity.MemberStatus;

@Component
public class MemberMapper {
    public MemberResponse toResponse(Member e) {
        return new MemberResponse(e.getId(), e.getMrn(), e.getFirstName(), e.getLastName(), e.getDateOfBirth(),
                e.getGender(), e.getEmail(), e.getPhone(), e.getAddressLine1(), e.getCity(), e.getState(),
                e.getPostalCode(), e.getPlanId(), e.getStatus(), e.getEnrollmentDate());
    }
    public Member toEntity(MemberRequest r) { Member e = new Member(); copy(r, e); return e; }
    public void copy(MemberRequest r, Member e) {
        e.setMrn(r.getMrn()); e.setFirstName(r.getFirstName()); e.setLastName(r.getLastName());
        e.setDateOfBirth(r.getDateOfBirth()); e.setGender(r.getGender()); e.setEmail(r.getEmail());
        e.setPhone(r.getPhone()); e.setAddressLine1(r.getAddressLine1()); e.setCity(r.getCity());
        e.setState(r.getState()); e.setPostalCode(r.getPostalCode()); e.setPlanId(r.getPlanId());
        e.setEnrollmentDate(r.getEnrollmentDate());
        if (e.getStatus() == null) e.setStatus(r.getStatus() == null ? MemberStatus.ACTIVE : r.getStatus());
    }
}

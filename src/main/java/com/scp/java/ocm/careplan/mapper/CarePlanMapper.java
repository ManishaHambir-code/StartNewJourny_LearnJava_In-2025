package com.scp.java.ocm.careplan.mapper;

import org.springframework.stereotype.Component;
import com.scp.java.ocm.careplan.dto.CarePlanRequest;
import com.scp.java.ocm.careplan.dto.CarePlanResponse;
import com.scp.java.ocm.careplan.entity.CarePlan;
import com.scp.java.ocm.careplan.entity.CarePlanStatus;
import com.scp.java.ocm.member.entity.Member;
import com.scp.java.ocm.provider.entity.Provider;

@Component
public class CarePlanMapper {
    public CarePlanResponse toResponse(CarePlan e) {
        return new CarePlanResponse(e.getId(), e.getMember().getId(), e.getCareManager().getId(), e.getProgram(),
                e.getRiskLevel(), e.getStatus(), e.getGoals(), e.getStartDate(), e.getEndDate());
    }
    public CarePlan toEntity(CarePlanRequest r, Member member, Provider provider) {
        CarePlan e = new CarePlan(); e.setMember(member); e.setCareManager(provider); copy(r,e); return e;
    }
    public void copy(CarePlanRequest r, CarePlan e) {
        e.setProgram(r.getProgram()); e.setRiskLevel(r.getRiskLevel()); e.setGoals(r.getGoals());
        e.setStartDate(r.getStartDate()); e.setEndDate(r.getEndDate());
        if (e.getStatus() == null) e.setStatus(CarePlanStatus.DRAFT);
    }
}

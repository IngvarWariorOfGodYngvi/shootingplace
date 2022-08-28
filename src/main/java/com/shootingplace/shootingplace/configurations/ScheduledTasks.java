package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.barCodeCards.BarCodeCardService;
import com.shootingplace.shootingplace.domain.enums.UserSubType;
import com.shootingplace.shootingplace.member.MemberService;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ScheduledTasks {

    private final WorkingTimeEvidenceService workServ;
    private final MemberService memberServ;
    @Autowired
    BarCodeCardService barCodeCardServ;

    public ScheduledTasks(WorkingTimeEvidenceService workRepo, MemberService memberServ) {
        this.workServ = workRepo;
        this.memberServ = memberServ;
    }

    @Scheduled(cron = "0 0 23,0-6 * * *")
    public void sendAllWorkersGoHome(){
        workServ.closeAllActiveWorkTime(UserSubType.WORKER.getName());
    }

    @Scheduled(cron = "0 0 1-9 * * *")
    public void sendAllManagementGoHome(){
        workServ.closeAllActiveWorkTime(UserSubType.MANAGEMENT.getName());
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkMembers() {memberServ.checkMembers();}

    @Scheduled(cron = "0 0 21-23 ? * *")
    @Transactional
    public void deactivateCard() {
        barCodeCardServ.deactivateNotMasterCard();
    }
}

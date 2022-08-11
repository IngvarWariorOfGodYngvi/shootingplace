package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.member.MemberService;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ScheduledTasks {

    private final WorkingTimeEvidenceService workServ;
    private final MemberService memberServ;

    public ScheduledTasks(WorkingTimeEvidenceService workRepo, MemberService memberServ) {
        this.workServ = workRepo;
        this.memberServ = memberServ;
    }

    @Scheduled(cron = "0 0 23,0-6 * * *")
    public void sendAllUserGoHome(){
        workServ.closeAllActiveWorkTime();
    }

    @Scheduled(cron = "0 0/15 * * * *")
    @Transactional
    public void checkMembers() {memberServ.checkMembers();}
}

package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceService;
import com.shootingplace.shootingplace.armory.AmmoUsedService;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardService;
import com.shootingplace.shootingplace.enums.UserSubType;
import com.shootingplace.shootingplace.member.MemberService;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class ScheduledTasks {

    private final WorkingTimeEvidenceService workServ;
    private final MemberService memberServ;
    private final BarCodeCardService barCodeCardServ;
    private final AmmoEvidenceService ammoEvidenceService;
    private final AmmoUsedService ammoUsedService;

    public ScheduledTasks(WorkingTimeEvidenceService workRepo, MemberService memberServ, BarCodeCardService barCodeCardServ, AmmoEvidenceService ammoEvidenceService, AmmoUsedService ammoUsedService) {
        this.workServ = workRepo;
        this.memberServ = memberServ;
        this.barCodeCardServ = barCodeCardServ;
        this.ammoEvidenceService = ammoEvidenceService;
        this.ammoUsedService = ammoUsedService;
    }
    @Transactional
    @Scheduled(cron = "0 30 20 * * * ")
    public void recountAmmo() {
        ammoUsedService.recountAmmo();
    }
    @Scheduled(cron = "0 1 23 * * *")
    public void closeOpenedAmmoList() {
        ammoEvidenceService.automationCloseEvidence();
    }

    @Scheduled(cron = "0 0 23,0-6 * * *")
    public void sendAllWorkersGoHome() {
        workServ.closeAllActiveWorkTime(UserSubType.WORKER.getName());
    }

    @Scheduled(cron = "0 0 1-9 * * *")
    public void sendAllManagementGoHome() {
        workServ.closeAllActiveWorkTime(UserSubType.MANAGEMENT.getName());
    }

    @Scheduled(cron = "0 0 0 * * *")
    @Transactional
    public void checkMembers() {
        memberServ.checkMembers();
    }

    @Scheduled(cron = "0 0 21-23 ? * *")
    @Transactional
    public void deactivateCard() {
        barCodeCardServ.deactivateNotMasterCard();
    }
}

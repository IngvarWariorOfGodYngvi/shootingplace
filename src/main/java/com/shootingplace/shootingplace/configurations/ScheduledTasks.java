package com.shootingplace.shootingplace.configurations;

import com.shootingplace.shootingplace.BookOfRegistrationOfStayAtTheShootingPlace.RegistrationRecordsService;
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
    private final RegistrationRecordsService registrationRecordsService;

    public ScheduledTasks(WorkingTimeEvidenceService workRepo, MemberService memberServ, BarCodeCardService barCodeCardServ, AmmoEvidenceService ammoEvidenceService, AmmoUsedService ammoUsedService, RegistrationRecordsService registrationRecordsService) {
        this.workServ = workRepo;
        this.memberServ = memberServ;
        this.barCodeCardServ = barCodeCardServ;
        this.ammoEvidenceService = ammoEvidenceService;
        this.ammoUsedService = ammoUsedService;
        this.registrationRecordsService = registrationRecordsService;
    }
    @Transactional
    @Scheduled(cron = "0/5 * * * * *")
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
    @Scheduled(cron = "0 30 11 ? * *")
    @Transactional
    public void setEndTimeToAllRegistrationRecordEntity() {
        registrationRecordsService.setEndTimeToAllRegistrationRecordEntity();
    }
//    @Scheduled(cron = "0 0 0 * * *")
//    public void checkAdult() {
//        memberServ.automateChangeAdult();
//    }
}

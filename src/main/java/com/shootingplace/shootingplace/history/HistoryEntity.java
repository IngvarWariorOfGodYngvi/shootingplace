package com.shootingplace.shootingplace.history;

import com.shootingplace.shootingplace.domain.entities.CompetitionHistoryEntity;
import com.shootingplace.shootingplace.contributions.ContributionEntity;
import com.shootingplace.shootingplace.domain.entities.JudgingHistoryEntity;
import com.shootingplace.shootingplace.domain.entities.LicensePaymentHistoryEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HistoryEntity {
    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("paymentDay DESC,validThru DESC")
    private List<ContributionEntity> contributionList;
    private String[] licenseHistory;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("date DESC")
    private List<LicensePaymentHistoryEntity> licensePaymentHistory;

    private Boolean patentFirstRecord = false;
    private LocalDate[] patentDay;

    private Integer pistolCounter;
    private Integer rifleCounter;
    private Integer shotgunCounter;
    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("date DESC")
    private List<CompetitionHistoryEntity> competitionHistory;

    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("date DESC")
    private List<JudgingHistoryEntity> judgingHistory;

    public String getUuid() {
        return uuid;
    }

    public List<ContributionEntity> getContributionList() {
        return contributionList;
    }

    public void setContributionsList(List<ContributionEntity> contributionsList) {
        this.contributionList = contributionsList;
    }

    public String[] getLicenseHistory() {
        return licenseHistory;
    }

    public void setLicenseHistory(String[] licenseHistory) {
        this.licenseHistory = licenseHistory;
    }

    public List<LicensePaymentHistoryEntity> getLicensePaymentHistory() {
        return licensePaymentHistory;
    }

    public void setLicensePaymentHistory(List<LicensePaymentHistoryEntity> licensePaymentHistory) {
        this.licensePaymentHistory = licensePaymentHistory;
    }

    public Boolean getPatentFirstRecord() {
        return patentFirstRecord;
    }

    public void setPatentFirstRecord(Boolean patentFirstRecord) {
        this.patentFirstRecord = patentFirstRecord;
    }

    public LocalDate[] getPatentDay() {
        return patentDay;
    }

    public void setPatentDay(LocalDate[] patentDay) {
        this.patentDay = patentDay;
    }

    public Integer getPistolCounter() {
        return pistolCounter;
    }

    public void setPistolCounter(Integer pistolCounter) {
        this.pistolCounter = pistolCounter;
    }

    public Integer getRifleCounter() {
        return rifleCounter;
    }

    public void setRifleCounter(Integer rifleCounter) {
        this.rifleCounter = rifleCounter;
    }

    public Integer getShotgunCounter() {
        return shotgunCounter;
    }

    public void setShotgunCounter(Integer shotgunCounter) {
        this.shotgunCounter = shotgunCounter;
    }

    public List<CompetitionHistoryEntity> getCompetitionHistory() {
        return competitionHistory;
    }

    public void setCompetitionHistory(List<CompetitionHistoryEntity> competitionHistory) {
        this.competitionHistory = competitionHistory;
    }

    public List<JudgingHistoryEntity> getJudgingHistory() {
        return judgingHistory;
    }

    public void setJudgingHistory(List<JudgingHistoryEntity> judgingHistory) {
        this.judgingHistory = judgingHistory;
    }
}

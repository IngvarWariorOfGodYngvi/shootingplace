package com.shootingplace.shootingplace.domain.models;

import com.shootingplace.shootingplace.domain.entities.OtherPersonEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.OneToOne;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Score {

    private String uuid;

    private float score;

    private float innerTen;
    private float outerTen;
    private float hf;


    private String name;

    private int metricNumber;

    private boolean ammunition;
    private boolean gun;

    private String competitionMembersListEntityUUID;
    @OneToOne(orphanRemoval = true)
    private MemberDTO member;
    @OneToOne(orphanRemoval = true)
    private OtherPersonEntity otherPersonEntity;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public float getScore() {
        return score;
    }

    public void setScore(float score) {
        this.score = score;
    }

    public float getInnerTen() {
        return innerTen;
    }

    public void setInnerTen(float innerTen) {
        this.innerTen = innerTen;
    }

    public float getOuterTen() {
        return outerTen;
    }

    public void setOuterTen(float outerTen) {
        this.outerTen = outerTen;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getMetricNumber() {
        return metricNumber;
    }

    public void setMetricNumber(int metricNumber) {
        this.metricNumber = metricNumber;
    }

    public boolean isAmmunition() {
        return ammunition;
    }

    public void setAmmunition(boolean ammunition) {
        this.ammunition = ammunition;
    }

    public boolean isGun() {
        return gun;
    }

    public void setGun(boolean gun) {
        this.gun = gun;
    }

    public String getCompetitionMembersListEntityUUID() {
        return competitionMembersListEntityUUID;
    }

    public void setCompetitionMembersListEntityUUID(String competitionMembersListEntityUUID) {
        this.competitionMembersListEntityUUID = competitionMembersListEntityUUID;
    }

    public MemberDTO getMember() {
        return member;
    }

    public void setMember(MemberDTO member) {
        this.member = member;
    }

    public OtherPersonEntity getOtherPersonEntity() {
        return otherPersonEntity;
    }

    public void setOtherPersonEntity(OtherPersonEntity otherPersonEntity) {
        this.otherPersonEntity = otherPersonEntity;
    }

    public float getHf() {
        return hf;
    }

    public void setHf(float hf) {
        this.hf = hf;
    }
}

package com.shootingplace.shootingplace.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ScoreEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
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
    private MemberEntity member;
    @OneToOne(orphanRemoval = true)
    private OtherPersonEntity otherPersonEntity;

    public String getUuid() {
        return uuid;
    }

    public float getScore() {
        return score;
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

    public void setScore(float score) {
        this.score = score;
    }

    public String getCompetitionMembersListEntityUUID() {
        return competitionMembersListEntityUUID;
    }

    public void setCompetitionMembersListEntityUUID(String competitionMembersListEntityUUID) {
        this.competitionMembersListEntityUUID = competitionMembersListEntityUUID;
    }

    public MemberEntity getMember() {
        return member;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }

    public OtherPersonEntity getOtherPersonEntity() {
        return otherPersonEntity;
    }

    public void setOtherPersonEntity(OtherPersonEntity otherPersonEntity) {
        this.otherPersonEntity = otherPersonEntity;
    }

    public boolean isAmmunition() {
        return ammunition;
    }

    public void toggleAmmunition() {
        this.ammunition = !this.ammunition;
    }

    public boolean isGun() {
        return gun;
    }

    public void toggleGun() {
        this.gun = !this.gun;
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

    public float getHf() {
        return hf;
    }

    public void setHf(float hf) {
        this.hf = hf;
    }
}

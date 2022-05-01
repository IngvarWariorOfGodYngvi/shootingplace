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
    private int procedures;

    private float alfa;
    private float charlie;
    private float delta;

    private String name;

    private int metricNumber;

    private boolean ammunition;
    private boolean gun;

    private boolean dnf;
    private boolean dsq;
    private boolean pk;


    private String competitionMembersListEntityUUID;
    @OneToOne(orphanRemoval = true)
    private MemberDTO member;
    @OneToOne(orphanRemoval = true)
    private OtherPersonEntity otherPersonEntity;


    public boolean isDnf() {
        return dnf;
    }

    public void setDnf(boolean dnf) {
        this.dnf = dnf;
    }

    public boolean isDsq() {
        return dsq;
    }

    public void setDsq(boolean dsq) {
        this.dsq = dsq;
    }

    public float getAlfa() {
        return alfa;
    }

    public void setAlfa(float alfa) {
        this.alfa = alfa;
    }

    public float getCharlie() {
        return charlie;
    }

    public void setCharlie(float charlie) {
        this.charlie = charlie;
    }

    public float getDelta() {
        return delta;
    }

    public void setDelta(float delta) {
        this.delta = delta;
    }

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

    public int getProcedures() {
        return procedures;
    }

    public void setProcedures(int procedures) {
        this.procedures = procedures;
    }

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }
}

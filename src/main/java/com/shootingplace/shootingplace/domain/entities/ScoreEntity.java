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

    private float alfa;
    private float charlie;
    private float delta;

    private float innerTen;
    private float outerTen;
    private float hf;
    private int procedures;

    private String name;

    private int metricNumber;

    private boolean ammunition;
    private boolean gun;

    private boolean dnf;
    private boolean dsq;
    private boolean pk;

    private String competitionMembersListEntityUUID;
    @OneToOne(orphanRemoval = true)
    private MemberEntity member;
    @OneToOne(orphanRemoval = true)
    private OtherPersonEntity otherPersonEntity;

    public String getUuid() {
        return uuid;
    }

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

    public void toggleDnf() {
        this.dnf = !this.dnf;
    }

    public void toggleDsq() {
        this.dsq = !this.dsq;
    }

    public void togglePk() {
        this.pk = !this.pk;
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

    public int getProcedures() {
        return procedures;
    }

    public void setProcedures(int procedures) {
        this.procedures = procedures;
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

    public boolean isPk() {
        return pk;
    }

    public void setPk(boolean pk) {
        this.pk = pk;
    }
}

package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardDTO;
import com.shootingplace.shootingplace.domain.entities.ClubEntity;
import com.shootingplace.shootingplace.domain.entities.ErasedEntity;
import com.shootingplace.shootingplace.domain.models.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.List;


@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Member extends Person{

    private LocalDate joinDate;
    private Integer legitimationNumber;
    private String firstName;
    private String secondName;
    private License license;
    private ShootingPatent shootingPatent;
    private String email = "";

    private String pesel;
    private String IDCard;
    private ClubEntity club;
    private Address address;
    private String phoneNumber;
    private WeaponPermission weaponPermission;

    private Boolean active = true;
    private Boolean adult = true;
    private Boolean erased = false;

    private History history;

    private MemberPermissions memberPermissions;

    private PersonalEvidence personalEvidence;
    private boolean pzss;
    private ErasedEntity erasedEntity;

    private List<BarCodeCardDTO> barCodeCardList;

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public Integer getLegitimationNumber() {
        return legitimationNumber;
    }

    public void setLegitimationNumber(Integer legitimationNumber) {
        this.legitimationNumber = legitimationNumber;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public License getLicense() {
        return license;
    }

    public void setLicense(License license) {
        this.license = license;
    }

    public ShootingPatent getShootingPatent() {
        return shootingPatent;
    }

    public void setShootingPatent(ShootingPatent shootingPatent) {
        this.shootingPatent = shootingPatent;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPesel() {
        return pesel;
    }

    public void setPesel(String pesel) {
        this.pesel = pesel;
    }

    public String getIDCard() {
        return IDCard;
    }

    public void setIDCard(String IDCard) {
        this.IDCard = IDCard;
    }

    public ClubEntity getClub() {
        return club;
    }

    public void setClub(ClubEntity club) {
        this.club = club;
    }

    public Address getAddress() {
        return address;
    }

    public void setAddress(Address address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public WeaponPermission getWeaponPermission() {
        return weaponPermission;
    }

    public void setWeaponPermission(WeaponPermission weaponPermission) {
        this.weaponPermission = weaponPermission;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getAdult() {
        return adult;
    }

    public void setAdult(Boolean adult) {
        this.adult = adult;
    }

    public Boolean getErased() {
        return erased;
    }

    public void setErased(Boolean erased) {
        this.erased = erased;
    }

    public History getHistory() {
        return history;
    }

    public void setHistory(History history) {
        this.history = history;
    }

    public MemberPermissions getMemberPermissions() {
        return memberPermissions;
    }

    public void setMemberPermissions(MemberPermissions memberPermissions) {
        this.memberPermissions = memberPermissions;
    }

    public PersonalEvidence getPersonalEvidence() {
        return personalEvidence;
    }

    public void setPersonalEvidence(PersonalEvidence personalEvidence) {
        this.personalEvidence = personalEvidence;
    }

    public void setPzss(boolean pzss) {
        this.pzss = pzss;
    }

    public boolean getPzss() {
        return pzss;
    }

    public void setErasedEntity(ErasedEntity erasedEntity) {
        this.erasedEntity = erasedEntity;
    }

    public ErasedEntity getErasedEntity() {
        return erasedEntity;
    }

    public boolean isPzss() {
        return pzss;
    }

    public List<BarCodeCardDTO> getBarCodeCardList() {
        return barCodeCardList;
    }

    public void setBarCodeCardList(List<BarCodeCardDTO> barCodeCardList) {
        this.barCodeCardList = barCodeCardList;
    }
}

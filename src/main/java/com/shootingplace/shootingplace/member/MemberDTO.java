package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.club.Club;
import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.enums.ErasedType;
import com.shootingplace.shootingplace.license.License;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MemberDTO extends Person {
    private String uuid;
    private LocalDate joinDate;
    private Integer legitimationNumber;
    private String firstName;
    private String secondName;
    private ErasedType erasedType;
    private Erased erasedEntity;
    private License license;
    private Boolean pzss;
    private MemberPermissions memberPermissions;
    private String image;
    private Boolean adult = true;
    private Boolean active = true;
    private Boolean erased = false;
    private Club club;
    private Boolean declarationLOK = false;

    public Club getClub() {
        return club;
    }

    public void setClub(Club club) {
        this.club = club;
    }


    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public LocalDate getJoinDate() {
        return joinDate;
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

    public Boolean getAdult() {
        return adult;
    }

    public void setAdult(Boolean adult) {
        this.adult = adult;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public Boolean getErased() {
        return erased;
    }

    public void setErased(Boolean erased) {
        this.erased = erased;
    }

    public Boolean getPzss() {
        return pzss;
    }

    public void setPzss(Boolean pzss) {
        this.pzss = pzss;
    }

    public Erased getErasedEntity() {
        return erasedEntity;
    }

    public ErasedType getErasedType() {
        return erasedType;
    }

    public void setErasedType(ErasedType erasedType) {
        this.erasedType = erasedType;
    }

    public String getImage() {
        return image;
    }

    public MemberPermissions getMemberPermissions() {
        return memberPermissions;
    }

    public void setMemberPermissions(MemberPermissions memberPermissions) {
        this.memberPermissions = memberPermissions;
    }

    public boolean getDeclarationLOK() {
        return declarationLOK;
    }

    /**
     * Return secondName plus firstName of Member
     */
    public String getFullName() {
        return this.getSecondName().replaceAll(" ", "") + ' ' +
                this.getFirstName().replaceAll(" ", "");
    }

}

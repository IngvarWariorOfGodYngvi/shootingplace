package com.shootingplace.shootingplace.member;

import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.history.HistoryEntity;
import com.shootingplace.shootingplace.license.LicenseEntity;
import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.barCodeCards.BarCodeCardEntity;
import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.shootingPatent.ShootingPatentEntity;
import com.shootingplace.shootingplace.validators.ValidPESEL;
import com.shootingplace.shootingplace.weaponPermission.WeaponPermissionEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.hibernate.validator.constraints.Email;
import org.hibernate.validator.constraints.NotBlank;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MemberEntity extends Person {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private LocalDate joinDate;
    private Integer legitimationNumber;
    private String clubCardBarCode;
    @OneToMany
    private List<BarCodeCardEntity> barCodeCardList;
    @NotBlank
    private String firstName;
    @NotBlank
    private String secondName;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private LicenseEntity license;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ShootingPatentEntity shootingPatent;
    @Email
    private String email = "";
    @NotBlank
    @ValidPESEL
    @Pattern(regexp = "[0-9]*")
    private String pesel;
    @NotBlank
    private String IDCard;
    @ManyToOne( cascade = CascadeType.ALL)
    private ClubEntity club;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private AddressEntity address;
    @NotBlank
    @Pattern(regexp = "^\\+[0-9]{11}$")
    private String phoneNumber;
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private WeaponPermissionEntity weaponPermission;

    private String imageUUID;

    private Boolean active = true;
    private Boolean adult = true;
    private Boolean erased = false;
    private Boolean pzss = false;
    @Nullable
    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private ErasedEntity erasedEntity;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private HistoryEntity history;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private MemberPermissionsEntity memberPermissions;

    @OneToOne(orphanRemoval = true, cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private PersonalEvidenceEntity personalEvidence;

    public String getImageUUID() {
        return imageUUID;
    }

    public void setImageUUID(String imageUUID) {
        this.imageUUID = imageUUID;
    }

    public String getUuid() {
        return uuid;
    }

    public LocalDate getJoinDate() {
        return joinDate;
    }

    public void setJoinDate(LocalDate joinDate) {
        this.joinDate = joinDate;
    }

    public Integer getLegitimationNumber() {
        return legitimationNumber;
    }

    public String getClubCardBarCode() {
        return clubCardBarCode;
    }

    public void setClubCardBarCode(String clubCardBarCode) {
        this.clubCardBarCode = clubCardBarCode;
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

    public LicenseEntity getLicense() {
        return license;
    }

    public void setLicense(LicenseEntity license) {
        this.license = license;
    }

    public ShootingPatentEntity getShootingPatent() {
        return shootingPatent;
    }

    public void setShootingPatent(ShootingPatentEntity shootingPatent) {
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

    public AddressEntity getAddress() {
        return address;
    }

    public void setAddress(AddressEntity address) {
        this.address = address;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public WeaponPermissionEntity getWeaponPermission() {
        return weaponPermission;
    }

    public void setWeaponPermission(WeaponPermissionEntity weaponPermission) {
        this.weaponPermission = weaponPermission;
    }

    public ErasedEntity getErasedEntity() {
        return erasedEntity;
    }

    public void setErasedEntity(ErasedEntity erasedEntity) {
        this.erasedEntity = erasedEntity;
    }

    public List<BarCodeCardEntity> getBarCodeCardList() {
        return barCodeCardList;
    }

    public void setBarCodeCardList(List<BarCodeCardEntity> barCodeCardList) {
        this.barCodeCardList = barCodeCardList;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public void toggleActive() {
        this.active = !this.active;
    }

    public Boolean getAdult() {
        return adult;
    }

    public Boolean getErased() {
        return erased;
    }

    public void toggleErase() {
        this.erased = !this.erased;
    }

    public HistoryEntity getHistory() {
        return history;
    }

    public void setHistory(HistoryEntity history) {
        this.history = history;
    }

    public MemberPermissionsEntity getMemberPermissions() {
        return memberPermissions;
    }

    public void setMemberPermissions(MemberPermissionsEntity memberPermissions) {
        this.memberPermissions = memberPermissions;
    }

    public PersonalEvidenceEntity getPersonalEvidence() {
        return personalEvidence;
    }

    public void setPersonalEvidence(PersonalEvidenceEntity personalEvidence) {
        this.personalEvidence = personalEvidence;
    }

    public ClubEntity getClub() {
        return club;
    }

    public void setClub(ClubEntity club) {
        this.club = club;
    }

    public void setAdult(Boolean adult) {
        this.adult = adult;
    }

    public Boolean getPzss() {
        return pzss;
    }

    public void setPzss(Boolean pzss) {
        this.pzss = pzss;
    }

    /**
     * Return secondName plus firstName of Member
     */
    public String getMemberName() {
        return this.getSecondName().replaceAll(" ", "") +
                this.getFirstName().replaceAll(" ", "");
    }

}

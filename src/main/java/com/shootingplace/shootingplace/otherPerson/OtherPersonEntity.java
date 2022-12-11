package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.member.MemberPermissionsEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class OtherPersonEntity {


    @Id
    private Integer id;

    private String firstName;
    private String secondName;
    private String phoneNumber;
    private String email;
    private boolean active;
    @ManyToOne
    private ClubEntity club;
    @OneToOne(orphanRemoval = true)
    private MemberPermissionsEntity permissionsEntity;

    public Integer getId() {
        return id;
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

    public ClubEntity getClub() {
        return club;
    }

    public void setClub(ClubEntity club) {
        this.club = club;
    }

    public MemberPermissionsEntity getPermissionsEntity() {
        return permissionsEntity;
    }

    public void setPermissionsEntity(MemberPermissionsEntity permissionsEntity) {
        this.permissionsEntity = permissionsEntity;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }
}

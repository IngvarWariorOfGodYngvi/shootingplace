package com.shootingplace.shootingplace.users;

import com.shootingplace.shootingplace.barCodeCards.BarCodeCardEntity;
import com.shootingplace.shootingplace.history.ChangeHistoryEntity;
import com.shootingplace.shootingplace.domain.Person;
import com.shootingplace.shootingplace.member.MemberEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserEntity extends Person {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @OneToOne
    private MemberEntity member;

    private String firstName;

    private String secondName;
    private Integer otherID;

    private String pinCode;
    @OneToMany
    private List<BarCodeCardEntity> barCodeCardList;

    private boolean superUser;

    private boolean active;
    @OrderBy("dayNow DESC, timeNow DESC")
    @ManyToMany
    private List<ChangeHistoryEntity> changeHistoryEntities;

    private String subType;

    @Nullable
    public MemberEntity getMember() {
        return member;
    }

    public void setMember(MemberEntity member) {
        this.member = member;
    }

    public String getSubType() {
        return subType;
    }

    public void setSubType(String subType) {
        this.subType = subType;
    }

    public String getUuid() {
        return uuid;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    @Override
    public String getFirstName() {
        return firstName;
    }

    @Override
    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public List<BarCodeCardEntity> getBarCodeCardList() {
        return barCodeCardList;
    }

    public void setBarCodeCardList(List<BarCodeCardEntity> barCodeCardList) {
        this.barCodeCardList = barCodeCardList;
    }

    public List<ChangeHistoryEntity> getList() {
        return changeHistoryEntities;
    }

    public void setList(List<ChangeHistoryEntity> changeHistoryEntities) {
        this.changeHistoryEntities = changeHistoryEntities;
    }

    public Integer getOtherID() {
        return otherID;
    }

    public void setOtherID(Integer otherID) {
        this.otherID = otherID;
    }

    public boolean isSuperUser() {
        return superUser;
    }

    public void setSuperUser(boolean superUser) {
        this.superUser = superUser;
    }

    public String getPinCode() {
        return pinCode;
    }

    public void setPinCode(String pinCode) {
        this.pinCode = pinCode;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    @Override
    public String toString() {
        return "UserEntity{" +
                "firstName='" + firstName + '\'' +
                ", secondName='" + secondName + '\'' +
                ", legitimationNumber=" + otherID +
                ", superUser=" + superUser +
                ", active=" + active +
                ", subType='" + subType + '\'' +
                '}';
    }
}

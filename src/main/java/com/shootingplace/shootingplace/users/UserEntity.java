package com.shootingplace.shootingplace.users;

import com.shootingplace.shootingplace.barCodeCards.BarCodeCardEntity;
import com.shootingplace.shootingplace.history.ChangeHistoryEntity;
import com.shootingplace.shootingplace.domain.Person;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

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

    private String firstName;

    private String secondName;

    private int legitimationNumber;

    private String pinCode;
@OneToMany
    private List<BarCodeCardEntity> barCodeCardList;

    private boolean superUser;

    private boolean active;
    @OrderBy("dayNow DESC, timeNow DESC")
    @ManyToMany
    private List<ChangeHistoryEntity> changeHistoryEntities;

    private String subType;

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

    public int getLegitimationNumber() {
        return legitimationNumber;
    }

    public void setLegitimationNumber(int legitimationNumber) {
        this.legitimationNumber = legitimationNumber;
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
}

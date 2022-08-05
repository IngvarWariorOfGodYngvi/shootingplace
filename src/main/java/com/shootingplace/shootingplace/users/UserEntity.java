package com.shootingplace.shootingplace.users;

import com.shootingplace.shootingplace.domain.entities.ChangeHistoryEntity;
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
public class UserEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String name;

    private String secondName;

    private String pinCode;

    private String cardNumber;

    private boolean superUser;

    private boolean active;
    @OrderBy("dayNow DESC, timeNow DESC")
    @ManyToMany
    private List<ChangeHistoryEntity> changeHistoryEntities;

    public String getUuid() {
        return uuid;
    }

    public String getSecondName() {
        return secondName;
    }

    public void setSecondName(String secondName) {
        this.secondName = secondName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public List<ChangeHistoryEntity> getList() {
        return changeHistoryEntities;
    }

    public void setList(List<ChangeHistoryEntity> changeHistoryEntities) {
        this.changeHistoryEntities = changeHistoryEntities;
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

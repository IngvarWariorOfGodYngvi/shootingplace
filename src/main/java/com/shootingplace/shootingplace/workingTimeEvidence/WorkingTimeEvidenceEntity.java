package com.shootingplace.shootingplace.workingTimeEvidence;

import com.shootingplace.shootingplace.users.UserEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
public class WorkingTimeEvidenceEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private LocalDateTime start;
    private LocalDateTime stop;
    private String cardNumber;
    private String workTime;
    private boolean isClose;
    private boolean isAutomatedClosed;
    private boolean toClarify;
    @OneToOne(orphanRemoval = true)
    private UserEntity user;
    private String workType;

    public String getWorkType() {
        return workType;
    }

    public void setWorkType(String workType) {
        this.workType = workType;
    }

    public void closeWTE(){
        this.isClose = true;
    }

    public boolean isClose() {
        return isClose;
    }

    public void setClose(boolean close) {
        isClose = close;
    }

    public boolean isAutomatedClosed() {
        return isAutomatedClosed;
    }

    public boolean isToClarify() {
        return toClarify;
    }

    public void setToClarify(boolean toClarify) {
        this.toClarify = toClarify;
    }

    public void setAutomatedClosed(boolean automatedClosed) {
        isAutomatedClosed = automatedClosed;
    }

    public LocalDateTime getStart() {
        return start;
    }

    public void setStart(LocalDateTime start) {
        this.start = start;
    }

    public LocalDateTime getStop() {
        return stop;
    }

    public void setStop(LocalDateTime stop) {
        this.stop = stop;
    }

    public String getCardNumber() {
        return cardNumber;
    }

    public void setCardNumber(String cardNumber) {
        this.cardNumber = cardNumber;
    }

    public UserEntity getUser() {
        return user;
    }

    public void setUser(UserEntity user) {
        this.user = user;
    }

    public String getWorkTime() {
        return workTime;
    }

    public void setWorkTime(String workTime) {
        this.workTime = workTime;
    }
}

package com.shootingplace.shootingplace.contributions;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ContributionEntity {


    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private LocalDate paymentDay;
    private LocalDate validThru;

    private String historyUUID;

    public String getUuid() {
        return uuid;
    }

    public LocalDate getPaymentDay() {
        return paymentDay;
    }

    public void setPaymentDay(LocalDate paymentDay) {
        this.paymentDay = paymentDay;
    }

    public LocalDate getValidThru() {
        return validThru;
    }

    public void setValidThru(LocalDate validThru) {
        this.validThru = validThru;
    }

    public String getHistoryUUID() {
        return historyUUID;
    }

    public void setHistoryUUID(String historyUUID) {
        this.historyUUID = historyUUID;
    }
}

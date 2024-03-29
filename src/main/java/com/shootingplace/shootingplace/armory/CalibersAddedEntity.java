package com.shootingplace.shootingplace.armory;

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
public class CalibersAddedEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String belongTo;
    private String caliberName;
    private String description;

    private LocalDate date;
    private Integer ammoAdded;

    private Integer stateForAddedDay;
    private Integer finalStateForAddedDay;

    public String getUuid() {
        return uuid;
    }

    public String getBelongTo() {
        return belongTo;
    }

    public void setBelongTo(String belongTo) {
        this.belongTo = belongTo;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public Integer getAmmoAdded() {
        return ammoAdded;
    }

    public void setAmmoAdded(Integer ammoAdded) {
        this.ammoAdded = ammoAdded;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getStateForAddedDay() {
        return stateForAddedDay;
    }

    public void setStateForAddedDay(Integer stateForAddedDay) {
        this.stateForAddedDay = stateForAddedDay;
    }

    public Integer getFinalStateForAddedDay() {
        return finalStateForAddedDay;
    }

    public void setFinalStateForAddedDay(Integer finalStateForAddedDay) {
        this.finalStateForAddedDay = finalStateForAddedDay;
    }

    public String getCaliberName() {
        return caliberName;
    }

    public void setCaliberName(String caliberName) {
        this.caliberName = caliberName;
    }
}

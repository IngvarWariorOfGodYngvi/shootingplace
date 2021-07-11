package com.shootingplace.shootingplace.domain.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Competition {

    private String name;

    private String discipline;

    private String[] disciplines;

    private Integer numberOfShots;

    private String type;

    private String countingMethod;

    private Integer ordering;

    public String getCountingMethod() {
        return countingMethod;
    }

    public void setCountingMethod(String countingMethod) {
        this.countingMethod = countingMethod;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

    public Integer getNumberOfShots() {
        return numberOfShots;
    }

    public void setNumberOfShots(Integer numberOfShots) {
        this.numberOfShots = numberOfShots;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    public String[] getDisciplines() {
        return disciplines;
    }

    public void setDisciplines(String[] disciplines) {
        this.disciplines = disciplines;
    }
}

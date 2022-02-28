package com.shootingplace.shootingplace.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.util.Arrays;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetitionEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String name;

    private String discipline;

    private String[] disciplines;

    private Integer numberOfShots;

    private Integer[] numberOfManyShots;

    private String type;

    private String countingMethod;

    private Integer ordering;

    private Integer practiceShots;

    private String caliberUUID;

    public String getCaliberUUID() {
        return caliberUUID;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
    }

    public Integer getPracticeShots() {
        return practiceShots;
    }

    public void setPracticeShots(Integer practiceShots) {
        this.practiceShots = practiceShots;
    }

    public Integer getNumberOfShots() {
        return numberOfShots;
    }

    public void setNumberOfShots(Integer numberOfShots) {
        this.numberOfShots = numberOfShots;
    }

    public String getUuid() {
        return uuid;
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


    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCountingMethod() {
        return countingMethod;
    }

    public void setCountingMethod(String countingMethod) {
        this.countingMethod = countingMethod;
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

    public Integer[] getNumberOfManyShots() {
        return numberOfManyShots;
    }

    public void setNumberOfManyShots(Integer[] numberOfManyShots) {
        this.numberOfManyShots = numberOfManyShots;
    }

    @Override
    public String toString() {
        return "CompetitionEntity{" +
                "uuid='" + uuid + '\'' +
                ", name='" + name + '\'' +
                ", discipline='" + discipline + '\'' +
                ", disciplines=" + Arrays.toString(disciplines) +
                ", numberOfShots=" + numberOfShots +
                ", numberOfManyShots=" + Arrays.toString(numberOfManyShots) +
                ", type='" + type + '\'' +
                ", countingMethod='" + countingMethod + '\'' +
                ", ordering=" + ordering +
                ", practiceShots=" + practiceShots +
                ", caliberUUID='" + caliberUUID + '\'' +
                '}';
    }
}

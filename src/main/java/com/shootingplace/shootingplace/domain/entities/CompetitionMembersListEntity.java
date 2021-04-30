package com.shootingplace.shootingplace.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetitionMembersListEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String name;
    private String attachedToTournament;
    private LocalDate date;

    private String discipline;
    private Integer numberOfShots;

    private String type;

    private String countingMethod;

    private boolean WZSS;

    @ManyToMany
    private List<ScoreEntity> scoreList = new ArrayList<>();

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAttachedToTournament() {
        return attachedToTournament;
    }

    public void setAttachedToTournament(String attachedToTournament) {
        this.attachedToTournament = attachedToTournament;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public List<ScoreEntity> getScoreList() {
        return scoreList;
    }

    public void setScoreList(List<ScoreEntity> scoreList) {
        this.scoreList = scoreList;
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

    public String getCountingMethod() {
        return countingMethod;
    }

    public void setCountingMethod(String countingMethod) {
        this.countingMethod = countingMethod;
    }

    public boolean isWZSS() {
        return WZSS;
    }

    public void setWZSS(boolean WZSS) {
        this.WZSS = WZSS;
    }
}

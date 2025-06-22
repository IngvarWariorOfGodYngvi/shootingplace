package com.shootingplace.shootingplace.tournament;

import com.shootingplace.shootingplace.score.ScoreEntity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.ManyToMany;
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
    private String abbreviation;

    private String attachedToTournament;
    private LocalDate date;

    private String discipline;
    private Integer numberOfShots;
    private String[] disciplines;
    private String disciplineList;
    private Integer[] numberOfManyShots;
    private String numberOfManyShotsList;

    private String type;

    private String countingMethod;

    private boolean WZSS;

    private Integer ordering;

    private Integer practiceShots;

    private String competitionUUID;

    private String caliberUUID;

    @ManyToMany
    private List<ScoreEntity> scoreList = new ArrayList<>();

    public List<String> getDisciplineList() {
        List<String> vals = new ArrayList<>();
        if (disciplineList != null) {
            for (String s : disciplineList.split(";")) {
                vals.add(String.valueOf(s));
            }
        }
        return vals;
    }

    public void setDisciplineList(List<String> disciplineList) {
        String value = "";
        for (String f : disciplineList) {
            value = value.concat(f + ";");
        }
        this.disciplineList = value;
    }

    public List<String> getNumberOfManyShotsList() {
        List<String> vals = new ArrayList<>();
        if (numberOfManyShotsList != null) {
            for (String s : numberOfManyShotsList.split(";")) {
                vals.add(String.valueOf(s));
            }
        }
        return vals;
    }

    public void setNumberOfManyShotsList(List<String> numberOfManyShotsList) {
        String value = "";
        for (String f : numberOfManyShotsList) {
            value = value.concat(f + ";");
        }
        this.numberOfManyShotsList = value;
    }

    public String getCaliberUUID() {
        return caliberUUID;
    }

    public void setCaliberUUID(String caliberUUID) {
        this.caliberUUID = caliberUUID;
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

    public Integer getOrdering() {
        return ordering;
    }

    public void setOrdering(Integer ordering) {
        this.ordering = ordering;
    }

    public String getCompetitionUUID() {
        return competitionUUID;
    }

    public void setCompetitionUUID(String competitionUUID) {
        this.competitionUUID = competitionUUID;
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

    public Integer getPracticeShots() {
        return practiceShots;
    }

    public void setPracticeShots(Integer practiceShots) {
        this.practiceShots = practiceShots;
    }

    public String getAbbreviation() {
        return abbreviation;
    }

    public void setAbbreviation(String abbreviation) {
        this.abbreviation = abbreviation;
    }
}

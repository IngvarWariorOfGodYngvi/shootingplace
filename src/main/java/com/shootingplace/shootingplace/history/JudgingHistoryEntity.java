package com.shootingplace.shootingplace.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDate;
import java.time.LocalTime;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class JudgingHistoryEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    private String name;

    private String tournamentUUID;
    private String judgingFunction;
    private LocalDate date;
    private LocalTime time;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTournamentUUID() {
        return tournamentUUID;
    }

    public void setTournamentUUID(String tournamentUUID) {
        this.tournamentUUID = tournamentUUID;
    }

    public String getJudgingFunction() {
        return judgingFunction;
    }

    public void setJudgingFunction(String judgingFunction) {
        this.judgingFunction = judgingFunction;
    }

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }

    public LocalTime getTime() {
        return time;
    }

    public void setTime(LocalTime time) {
        this.time = time;
    }
}

package com.shootingplace.shootingplace.history;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CompetitionHistory {

    private String name;

    private UUID attachedToList;

    private String discipline;

    private String disciplineList;

    private LocalDate date;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getAttachedToList() {
        return attachedToList;
    }

    public void setAttachedToList(UUID attachedToList) {
        this.attachedToList = attachedToList;
    }

    public String getDiscipline() {
        return discipline;
    }

    public void setDiscipline(String discipline) {
        this.discipline = discipline;
    }

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

    public LocalDate getDate() {
        return date;
    }

    public void setDate(LocalDate date) {
        this.date = date;
    }
}

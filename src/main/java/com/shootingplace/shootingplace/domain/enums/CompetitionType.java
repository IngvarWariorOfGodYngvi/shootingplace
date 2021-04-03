package com.shootingplace.shootingplace.domain.enums;

public enum CompetitionType {
    OPEN("OPEN"),
    YOUTH("Młodzieżowa");

    private final String name;

    CompetitionType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

}

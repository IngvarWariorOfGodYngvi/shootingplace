package com.shootingplace.shootingplace.enums;

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

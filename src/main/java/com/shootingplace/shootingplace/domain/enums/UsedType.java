package com.shootingplace.shootingplace.domain.enums;

public enum UsedType {

    TRAINING("Trening"),
    CLEANING("Czyszczenie"),
    CLUB_COMPETITION("Zawody Klubowe"),
    OUTSIDE("Poza Klubem"),
    OTHER("Inne");
    private final String name;

    UsedType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}

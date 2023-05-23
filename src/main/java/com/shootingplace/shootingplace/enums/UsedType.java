package com.shootingplace.shootingplace.enums;

public enum UsedType {

    TRAINING("Trening"),
    CLEANING("Czyszczenie"),
    CLUB_COMPETITION("Zawody Klubowe"),
    OUTSIDE("Poza Klubem"),
    REPAIR("Naprawa"),
    OTHER("Inne");
    private final String name;

    UsedType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}

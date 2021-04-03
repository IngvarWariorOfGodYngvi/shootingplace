package com.shootingplace.shootingplace.domain.enums;

public enum CountingMethod {
    NORMAL("NORMAL"),
    COMSTOCK("COMSTOCK");

    private final String name;

    CountingMethod(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

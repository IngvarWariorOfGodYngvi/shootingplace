package com.shootingplace.shootingplace.enums;

public enum ProfilesEnum {
    DZIESIATKA("prod"),
    TEST("test"),
    PANASZEW("rcs"),
    MECHANIK("uks");

    private final String name;

    ProfilesEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}

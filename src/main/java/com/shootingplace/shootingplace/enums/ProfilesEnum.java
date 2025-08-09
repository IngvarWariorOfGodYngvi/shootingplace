package com.shootingplace.shootingplace.enums;

public enum ProfilesEnum {
    DZIESIATKA("prod"),
    TEST("test"),
    PANASZEW("rcs");

    private final String name;

    ProfilesEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}

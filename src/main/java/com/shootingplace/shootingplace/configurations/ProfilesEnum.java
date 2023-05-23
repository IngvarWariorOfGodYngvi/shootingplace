package com.shootingplace.shootingplace.configurations;

public enum ProfilesEnum {
    DZIESIATKA("prod"),
    PANASZEW("rcs");

    private final String name;

    ProfilesEnum(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }


}

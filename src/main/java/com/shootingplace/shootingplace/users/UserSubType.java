package com.shootingplace.shootingplace.users;

public enum UserSubType {
    ADMIN("Admin"),
    MANAGEMENT("Zarząd"),
    WORKER("Pracownik"),
    MANAGEMENT_WORKER("Pracownik/Zarząd");
    private final String name;

    UserSubType(String name) {this.name = name;}


    public String getName() {
        return name;
    }
}

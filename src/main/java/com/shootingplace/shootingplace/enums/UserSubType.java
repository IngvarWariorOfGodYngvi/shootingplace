package com.shootingplace.shootingplace.enums;

public enum UserSubType {
    ADMIN("Admin"),
    MANAGEMENT("Zarząd"),
    WORKER("Pracownik"),
    REVISION_COMMITTEE("Komisja Rewizyjna"),
    VISITOR("Gość"),
    MANAGEMENT_CEO("Prezes/Zarząd"),
    MANAGEMENT_WORKER("Pracownik/Zarząd");
    private final String name;

    UserSubType(String name) {this.name = name;}


    public String getName() {
        return name;
    }
}

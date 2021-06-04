package com.shootingplace.shootingplace.domain.entities;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.validation.constraints.NotNull;

@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GunEntity {

    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;
    @NotNull
    private String modelName;
    @NotNull
    private String caliber;
    @NotNull
    private String serialNumber;

    private String productionYear;
    @NotNull
    private String gunType;

    private String numberOfMagazines;
    private String gunCertificateSerialNumber;

    private String additionalEquipment;
    private String recordInEvidenceBook;

    private String basisForPurchaseOrAssignment;

    private String comment;

    private boolean inStock;
    private String imgUUID;

    public String getImgUUID() {
        return imgUUID;
    }

    public void setImgUUID(String imgUUID) {
        this.imgUUID = imgUUID;
    }

    public String getUuid() {
        return uuid;
    }

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getCaliber() {
        return caliber;
    }

    public void setCaliber(String caliber) {
        this.caliber = caliber;
    }

    public String getGunType() {
        return gunType;
    }

    public void setGunType(String gunType) {
        this.gunType = gunType;
    }

    public String getSerialNumber() {
        return serialNumber;
    }

    public void setSerialNumber(String serialNumber) {
        this.serialNumber = serialNumber;
    }

    public String getProductionYear() {
        return productionYear;
    }

    public void setProductionYear(String productionYear) {
        this.productionYear = productionYear;
    }


    public String getNumberOfMagazines() {
        return numberOfMagazines;
    }

    public void setNumberOfMagazines(String numberOfMagazines) {
        this.numberOfMagazines = numberOfMagazines;
    }

    public String getGunCertificateSerialNumber() {
        return gunCertificateSerialNumber;
    }

    public void setGunCertificateSerialNumber(String gunCertificateSerialNumber) {
        this.gunCertificateSerialNumber = gunCertificateSerialNumber;
    }

    public String getAdditionalEquipment() {
        return additionalEquipment;
    }

    public void setAdditionalEquipment(String additionalEquipment) {
        this.additionalEquipment = additionalEquipment;
    }

    public String getRecordInEvidenceBook() {
        return recordInEvidenceBook;
    }

    public void setRecordInEvidenceBook(String recordInEvidenceBook) {
        this.recordInEvidenceBook = recordInEvidenceBook;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public boolean isInStock() {
        return inStock;
    }

    public void setInStock(boolean inStock) {
        this.inStock = inStock;
    }

    public String getBasisForPurchaseOrAssignment() {
        return basisForPurchaseOrAssignment;
    }

    public void setBasisForPurchaseOrAssignment(String basisForPurchaseOrAssignment) {
        this.basisForPurchaseOrAssignment = basisForPurchaseOrAssignment;
    }
}

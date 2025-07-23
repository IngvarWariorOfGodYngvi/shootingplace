package com.shootingplace.shootingplace.armory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

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
    @ManyToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @OrderBy("usedDate DESC")
    private List<GunUsedEntity> gunUsedList;

    private String numberOfMagazines;
    private String gunCertificateSerialNumber;

    private String additionalEquipment;
    private String recordInEvidenceBook;

    private String basisForPurchaseOrAssignment;

    private String comment;
    private boolean inStock;
    private boolean available;
    private String inUseStatus;
    private String imgUUID;
    private String barcode;

    private LocalDate addedDate;
    private String addedSign;
    private String addedBy;
    private String addedUserUUID;
    private String removedBy;
    private String removedSign;
    private String removedUserUUID;
    private LocalDate removedDate;
    private String basisOfRemoved;

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

    public String getInUseStatus() {
        return inUseStatus;
    }

    public void setInUseStatus(String inUseStatus) {
        this.inUseStatus = inUseStatus;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getBasisForPurchaseOrAssignment() {
        return basisForPurchaseOrAssignment;
    }

    public void setBasisForPurchaseOrAssignment(String basisForPurchaseOrAssignment) {
        this.basisForPurchaseOrAssignment = basisForPurchaseOrAssignment;
    }

    public String getBarcode() {
        return barcode;
    }

    public void setBarcode(String barcode) {
        this.barcode = barcode;
    }

    public List<GunUsedEntity> getGunUsedList() {
        return gunUsedList;
    }

    public void setGunUsedList(List<GunUsedEntity> gunUsedList) {
        this.gunUsedList = gunUsedList;
    }

    public LocalDate getAddedDate() {
        return addedDate;
    }

    public void setAddedDate(LocalDate addedDate) {
        this.addedDate = addedDate;
    }

    public void setAddedSign(String addedSign) {
        this.addedSign = addedSign;
    }
    public String getAddedSign() {
        return addedSign;
    }

    public void setAddedBy(String addedBy) {
        this.addedBy = addedBy;
    }

    public String getAddedBy() {
        return addedBy;
    }

    public void setAddedUserUUID(String addedUserUUID) {
        this.addedUserUUID = addedUserUUID;
    }

    public String getAddedUserUUID() {
        return addedUserUUID;
    }

    public void setRemovedBy(String removedBy) {
        this.removedBy = removedBy;
    }

    public String getRemovedBy() {
        return removedBy;
    }

    public void setRemovedSign(String removedSign) {
        this.removedSign = removedSign;
    }

    public String getRemovedSign() {
        return removedSign;
    }

    public void setRemovedUserUUID(String userUUID) {
        this.removedUserUUID = userUUID;
    }

    public String getRemovedUserUUID() {
        return removedUserUUID;
    }

    public LocalDate getRemovedDate() {
        return removedDate;
    }

    public void setRemovedDate(LocalDate removedDate) {
        this.removedDate = removedDate;
    }

    public String getBasisOfRemoved() {
        return basisOfRemoved;
    }

    public void setBasisOfRemoved(String basisOfRemoved) {
        this.basisOfRemoved = basisOfRemoved;
    }
}

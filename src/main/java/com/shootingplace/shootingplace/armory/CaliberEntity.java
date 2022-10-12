package com.shootingplace.shootingplace.armory;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.List;


@Entity
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CaliberEntity {
    @Id
    @GeneratedValue(generator = "id")
    @GenericGenerator(name = "id", strategy = "org.hibernate.id.UUIDGenerator")
    private String uuid;

    private String name;
    private Integer quantity;
    @OneToMany
    private List<CaliberUsedEntity> ammoUsed;
    @OneToMany
    private List<CalibersAddedEntity> ammoAdded;

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public void setQuantity(Integer quantity) {
        this.quantity = quantity;
    }

    public List<CaliberUsedEntity> getAmmoUsed() {
        return ammoUsed;
    }

    public void setAmmoUsed(List<CaliberUsedEntity> ammoUsed) {
        this.ammoUsed = ammoUsed;
    }

    public List<CalibersAddedEntity> getAmmoAdded() {
        return ammoAdded;
    }

    public void setAmmoAdded(List<CalibersAddedEntity> ammoAdded) {
        this.ammoAdded = ammoAdded;
    }
}

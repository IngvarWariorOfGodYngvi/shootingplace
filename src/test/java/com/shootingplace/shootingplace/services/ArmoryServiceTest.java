package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.ammoEvidence.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.armory.ArmoryService;
import com.shootingplace.shootingplace.armory.CaliberService;
import org.junit.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;

public class ArmoryServiceTest {

    @Mock
    AmmoEvidenceRepository ammoEvidenceRepository;

    @Mock
    CaliberService caliberService;

    @InjectMocks
    ArmoryService armoryService;

    @Test
    public void getSumFromAllAmmoList() {
    }

    @Test
    public void updateAmmo() {
    }

    @Test
    public void substratAmmo() {
    }

    @Test
    public void getHistoryOfCaliber() {
    }

    @Test
    public void addGunEntity() {
    }

    @Test
    public void getGunTypeList() {
    }

    @Test
    public void getAllGuns() {
    }

    @Test
    public void editGunEntity() {
    }

    @Test
    public void removeGun() {
    }

    @Test
    public void createNewGunStore() {
    }

    @Test
    public void addImageToGun() {
    }
}
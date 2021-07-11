package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.AmmoEvidenceEntity;
import com.shootingplace.shootingplace.domain.entities.AmmoInEvidenceEntity;
import com.shootingplace.shootingplace.domain.entities.AmmoUsedToEvidenceEntity;
import com.shootingplace.shootingplace.repositories.AmmoEvidenceRepository;
import com.shootingplace.shootingplace.repositories.AmmoInEvidenceRepository;
import com.shootingplace.shootingplace.repositories.AmmoUsedToEvidenceEntityRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AmmoInEvidenceService {
    private final AmmoInEvidenceRepository ammoInEvidenceRepository;
    private final AmmoUsedToEvidenceEntityRepository ammoUsedToEvidenceEntityRepository;
    private final ArmoryService armoryService;

    private final AmmoEvidenceRepository ammoEvidenceRepository;

    private final Logger LOG = LogManager.getLogger();

    public AmmoInEvidenceService(AmmoInEvidenceRepository ammoInEvidenceRepository, AmmoUsedToEvidenceEntityRepository ammoUsedToEvidenceEntityRepository, ArmoryService armoryService, AmmoEvidenceRepository ammoEvidenceRepository) {
        this.ammoInEvidenceRepository = ammoInEvidenceRepository;
        this.ammoUsedToEvidenceEntityRepository = ammoUsedToEvidenceEntityRepository;
        this.armoryService = armoryService;
        this.ammoEvidenceRepository = ammoEvidenceRepository;
    }

    boolean addAmmoUsedEntityToAmmoInEvidenceEntity(AmmoUsedToEvidenceEntity ammoUsedToEvidenceEntity) {

        List<AmmoEvidenceEntity> collect = ammoEvidenceRepository.findAll().stream().filter(AmmoEvidenceEntity::isOpen).collect(Collectors.toList());
//      Nie znaleziono żadnej listy
        if (collect.size() < 1) {
            if (ammoUsedToEvidenceEntity.getCounter() < 0) {
                LOG.info("nie można dodać ujemnej wartości");
            } else {
//                nadawanie numeru listy
                int number;
//                nadawanie numeru od zera
                if (ammoEvidenceRepository.findAll().size() < 1) {
                    number = 1;
//                    nadawanie kolejnego numeru
                } else {
                    List<AmmoEvidenceEntity> all = ammoEvidenceRepository.findAll();
                    number = all.size() + 1;
                }
                String evidenceNumber = number + "-LA-" + LocalDate.now().getYear();
                AmmoEvidenceEntity buildEvidence = AmmoEvidenceEntity.builder()
                        .date(LocalDate.now())
                        .open(true)
                        .ammoInEvidenceEntityList(new ArrayList<>())
                        .number(evidenceNumber)
                        .build();
                ammoEvidenceRepository.saveAndFlush(buildEvidence);

                AmmoInEvidenceEntity build = AmmoInEvidenceEntity.builder()
                        .caliberName(ammoUsedToEvidenceEntity.getCaliberName())
                        .caliberUUID(ammoUsedToEvidenceEntity.getCaliberUUID())
                        .evidenceUUID(buildEvidence.getUuid())
                        .quantity(0)
                        .ammoUsedToEvidenceEntityList(new ArrayList<>())
                        .build();

                build.getAmmoUsedToEvidenceEntityList().add(ammoUsedToEvidenceEntity);
                build.setQuantity(ammoUsedToEvidenceEntity.getCounter());
                ammoInEvidenceRepository.saveAndFlush(build);

                buildEvidence.getAmmoInEvidenceEntityList().add(build);
                ammoEvidenceRepository.saveAndFlush(buildEvidence);
                LOG.info("otworzono nową listę");
            }

        }
//      Znaleziono jakąś otwartą listę
        else {
            ammoEvidenceRepository.findAll();
            AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository
                    .findAll()
                    .stream()
                    .filter(AmmoEvidenceEntity::isOpen)
                    .findFirst()
                    .orElseThrow(EntityNotFoundException::new);

            List<AmmoInEvidenceEntity> ammoInEvidenceEntityList = ammoEvidenceEntity.getAmmoInEvidenceEntityList();
//       Znaleziono jakąś listę z podanym kalibrem w ewidencji
            if (ammoInEvidenceEntityList
                    .stream()
                    .anyMatch(a -> a.getCaliberUUID().equals(ammoUsedToEvidenceEntity.getCaliberUUID()))) {


                AmmoInEvidenceEntity ammoInEvidenceEntity = ammoEvidenceEntity.getAmmoInEvidenceEntityList()
                        .stream()
                        .filter(f -> f.getCaliberUUID().equals(ammoUsedToEvidenceEntity.getCaliberUUID()))
                        .findFirst()
                        .orElseThrow(EntityNotFoundException::new);

                List<AmmoUsedToEvidenceEntity> ammoUsedToEvidenceEntityList = ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList();
                if (ammoUsedToEvidenceEntityList.stream().noneMatch(f -> f.getName().equals(ammoUsedToEvidenceEntity.getName()))) {
                    if (ammoUsedToEvidenceEntity.getCounter() <= 0) {
                        LOG.info("nie można dodać ujemnej wartości");
                        return false;
                    } else {
                        ammoUsedToEvidenceEntityList.add(ammoUsedToEvidenceEntity);
                        ammoInEvidenceEntity.setQuantity(ammoInEvidenceEntity.getQuantity() + ammoUsedToEvidenceEntity.getCounter());

                        ammoInEvidenceRepository.saveAndFlush(ammoInEvidenceEntity);
                    }
                }
//        Znaleziono podanego member
                else {
                    AmmoUsedToEvidenceEntity ammoUsedToEvidenceEntity1 = ammoUsedToEvidenceEntityList
                            .stream()
                            .filter(f -> f.getName()
                                    .equals(ammoUsedToEvidenceEntity.getName()))
                            .findFirst()
                            .orElseThrow(EntityNotFoundException::new);
                    if (ammoUsedToEvidenceEntity1.getCounter() + ammoUsedToEvidenceEntity.getCounter() <= 0) {
//                   to jest to co należy dodać do magazynu amunicji     -ammoUsedToEvidenceEntity1.getCounter()
                        armoryService.substratAmmo(ammoUsedToEvidenceEntity1.getCaliberUUID(), -ammoUsedToEvidenceEntity1.getCounter());
                        ammoUsedToEvidenceEntity1.setCounter(-ammoUsedToEvidenceEntity1.getCounter());
                    } else {
                        ammoUsedToEvidenceEntity1.setCounter(ammoUsedToEvidenceEntity1.getCounter() + ammoUsedToEvidenceEntity.getCounter());
                    }

                    ammoInEvidenceEntity.setQuantity(ammoInEvidenceEntity.getQuantity() + ammoUsedToEvidenceEntity.getCounter());


                    if (ammoUsedToEvidenceEntity1.getCounter() <= 0) {
                        ammoInEvidenceEntity.getAmmoUsedToEvidenceEntityList().remove(ammoUsedToEvidenceEntity1);
                        ammoInEvidenceRepository.delete(ammoInEvidenceEntity);
                    } else {
                        ammoUsedToEvidenceEntityRepository.saveAndFlush(ammoUsedToEvidenceEntity1);
                        ammoInEvidenceRepository.saveAndFlush(ammoInEvidenceEntity);
                    }

                }

            }
//       Nie znaleziono żadnej listy z podanym kalibrem
            else {
                AmmoInEvidenceEntity build = AmmoInEvidenceEntity.builder()
                        .caliberName(ammoUsedToEvidenceEntity.getCaliberName())
                        .caliberUUID(ammoUsedToEvidenceEntity.getCaliberUUID())
                        .evidenceUUID(ammoEvidenceEntity.getUuid())
                        .quantity(0)
                        .ammoUsedToEvidenceEntityList(new ArrayList<>())
                        .build();

                build.getAmmoUsedToEvidenceEntityList().add(ammoUsedToEvidenceEntity);
                build.setQuantity(ammoUsedToEvidenceEntity.getCounter());
                ammoInEvidenceRepository.saveAndFlush(build);
                ammoEvidenceEntity.getAmmoInEvidenceEntityList().add(build);
                ammoEvidenceRepository.saveAndFlush(ammoEvidenceEntity);
            }

        }
//          Usuwanie listy jeśli ilość sztuk wynosi 0
        AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository
                .findAll()
                .stream().filter(AmmoEvidenceEntity::isOpen)
                .findFirst()
                .orElse(null);
        if (ammoEvidenceEntity == null) {
            return false;
        }

        if (ammoEvidenceEntity
                .getAmmoInEvidenceEntityList()
                .stream()
                .anyMatch(a -> a.getQuantity() <= 0)) {

            AmmoInEvidenceEntity ammoInEvidenceEntity = ammoEvidenceEntity
                    .getAmmoInEvidenceEntityList()
                    .stream()
                    .filter(f -> f.getQuantity() <= 0)
                    .findFirst()
                    .orElseThrow(EntityNotFoundException::new);

            ammoEvidenceEntity.getAmmoInEvidenceEntityList().remove(ammoInEvidenceEntity);
            ammoInEvidenceRepository.delete(ammoInEvidenceEntity);
            //        Usuwanie ewidencji jeśli nie ma żadnej listy z amunicją
            if (ammoEvidenceEntity.getAmmoInEvidenceEntityList().isEmpty()) {
                ammoEvidenceRepository.delete(ammoEvidenceEntity);
            }
        }
        return true;

    }

}

package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.*;
import com.shootingplace.shootingplace.domain.models.AmmoUsedEvidence;
import com.shootingplace.shootingplace.domain.models.AmmoUsedPersonal;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import com.shootingplace.shootingplace.repositories.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.Objects;

@Service
public class AmmoUsedService {
    private final PersonalEvidenceRepository personalEvidenceRepository;
    private final AmmoUsedToEvidenceEntityRepository ammoUsedToEvidenceEntityRepository;
    private final AmmoInEvidenceService ammoInEvidenceService;
    private final AmmoUsedRepository ammoUsedRepository;
    private final CaliberRepository caliberRepository;
    private final MemberRepository memberRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final ArmoryService armoryService;
    private final AmmoEvidenceRepository ammoEvidenceRepository;

    private final Logger LOG = LogManager.getLogger();

    public AmmoUsedService(PersonalEvidenceRepository personalEvidenceRepository,
                           AmmoUsedToEvidenceEntityRepository ammoUsedToEvidenceEntityRepository,
                           AmmoInEvidenceService ammoInEvidenceService,
                           AmmoUsedRepository ammoUsedRepository,
                           CaliberRepository caliberRepository,
                           MemberRepository memberRepository, OtherPersonRepository otherPersonRepository, ArmoryService armoryService, AmmoEvidenceRepository ammoEvidenceRepository) {
        this.personalEvidenceRepository = personalEvidenceRepository;
        this.ammoUsedToEvidenceEntityRepository = ammoUsedToEvidenceEntityRepository;
        this.ammoInEvidenceService = ammoInEvidenceService;
        this.ammoUsedRepository = ammoUsedRepository;
        this.caliberRepository = caliberRepository;
        this.memberRepository = memberRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.armoryService = armoryService;
        this.ammoEvidenceRepository = ammoEvidenceRepository;
    }

    @Transactional
    public ResponseEntity<String> addAmmoUsedEntity(String caliberUUID, Integer legitimationNumber, int otherID, Integer quantity) {
        if (ammoEvidenceRepository.findAll().stream().anyMatch(f -> f.isOpen() && !f.isForceOpen())) {
            AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findAll().stream().filter(f -> f.isOpen() && !f.isForceOpen()).findFirst().orElseThrow(EntityNotFoundException::new);
            if (ammoEvidenceEntity.getDate().isBefore(LocalDate.now())) {
                ammoEvidenceEntity.setOpen(false);
                ammoEvidenceEntity.setForceOpen(false);
                ammoEvidenceRepository.saveAndFlush(ammoEvidenceEntity);
                LOG.info("zamknięto starą listę");
            }
        }
        String caliberName = caliberRepository
                .findById(caliberUUID)
                .orElseThrow(EntityNotFoundException::new)
                .getName();
        boolean substratAmmo;
        if (quantity > 0) {
            substratAmmo = armoryService.substratAmmo(caliberUUID, quantity);
            LOG.info("dodaję amunicję do listy");
            if (substratAmmo) {

                String name;
                if (legitimationNumber > 0) {
                    LOG.info("member");
                    MemberEntity memberEntity = memberRepository.findAll().stream().filter(f -> f.getLegitimationNumber().equals(legitimationNumber)).findFirst().orElseThrow(EntityNotFoundException::new);
                    name = memberEntity.getSecondName() + " " + memberEntity.getFirstName();
                    AmmoUsedPersonal ammoUsedPersonal = AmmoUsedPersonal.builder()
                            .caliberName(caliberName)
                            .counter(quantity)
                            .memberUUID(memberEntity.getUuid())
                            .caliberUUID(caliberUUID)
                            .memberName(memberEntity.getFirstName() + " " + memberEntity.getSecondName())
                            .date(LocalDate.now())
                            .build();


                    AmmoUsedEvidence ammoUsedEvidence = AmmoUsedEvidence.builder()
                            .caliberName(caliberName)
                            .counter(quantity)
                            .memberEntity(memberEntity)
                            .otherPersonEntity(null)
                            .userName(name)
                            .caliberUUID(caliberUUID)
                            .date(LocalDate.now())
                            .build();
                    validateAmmo(ammoUsedPersonal);
                    if (starEvidence(ammoUsedEvidence)) {
                        return ResponseEntity.ok("\"Dodano do listy " + name + " " + caliberName + "\"");
                    }

                }
                else {
                    LOG.info("not member");
                    System.out.println(quantity);
                    OtherPersonEntity otherPersonEntity = otherPersonRepository
                            .findById(otherID)
                            .orElseThrow(EntityNotFoundException::new);
                    name = otherPersonEntity.getSecondName() + " " + otherPersonEntity.getFirstName();


                    AmmoUsedEvidence ammoUsedEvidence = AmmoUsedEvidence.builder()
                            .caliberName(caliberName)
                            .counter(quantity)
                            .memberEntity(null)
                            .otherPersonEntity(otherPersonEntity)
                            .userName(name)
                            .caliberUUID(caliberUUID)
                            .date(LocalDate.now())
                            .build();
                    boolean b = starEvidence(ammoUsedEvidence);
                    if (b) {
                        return ResponseEntity.ok("\"Dodano do listy " + name + " " + caliberName + "\"");
                    }
                }
            }
        } else {
            LOG.info("odejmuję amunicję z listy");

            String name;
            if (legitimationNumber > 0) {
                MemberEntity memberEntity = memberRepository.findAll().stream().filter(f -> f.getLegitimationNumber().equals(legitimationNumber)).findFirst().orElseThrow(EntityNotFoundException::new);
                name = memberEntity.getSecondName() + " " + memberEntity.getFirstName();
                AmmoUsedPersonal ammoUsedPersonal = AmmoUsedPersonal.builder()
                        .caliberName(caliberName)
                        .counter(quantity)
                        .memberUUID(memberEntity.getUuid())
                        .caliberUUID(caliberUUID)
                        .memberName(memberEntity.getFirstName() + " " + memberEntity.getSecondName())
                        .date(LocalDate.now())
                        .build();


                AmmoUsedEvidence ammoUsedEvidence = AmmoUsedEvidence.builder()
                        .caliberName(caliberName)
                        .counter(quantity)
                        .memberEntity(memberEntity)
                        .otherPersonEntity(null)
                        .userName(name)
                        .caliberUUID(caliberUUID)
                        .date(LocalDate.now())
                        .build();
                validateAmmo(ammoUsedPersonal);
                if (starEvidence(ammoUsedEvidence)) {
                    return ResponseEntity.ok("\"Zwrócono do magazynu " + name + " " + caliberName + "\"");
                }
            }
            else {
                OtherPersonEntity otherPersonEntity = otherPersonRepository
                        .findById(otherID)
                        .orElseThrow(EntityNotFoundException::new);
                name = otherPersonEntity.getSecondName() + " " + otherPersonEntity.getFirstName();


                AmmoUsedEvidence ammoUsedEvidence = AmmoUsedEvidence.builder()
                        .caliberName(caliberName)
                        .counter(quantity)
                        .memberEntity(null)
                        .otherPersonEntity(otherPersonEntity)
                        .userName(name)
                        .caliberUUID(caliberUUID)
                        .date(LocalDate.now())
                        .build();
                if (starEvidence(ammoUsedEvidence)) {
                    return ResponseEntity.ok("\"Zwrócono do magazynu " + name + " " + caliberName + "\"");
                }
            }

        }
        return ResponseEntity.badRequest().body("\"Coś poszło nie tak - Sprawdź stany magazynowe " + caliberName + "\"");
    }

    private void validateAmmo(AmmoUsedPersonal ammoUsedpersonal) {
        PersonalEvidenceEntity personalEvidence = memberRepository
                .findById(ammoUsedpersonal.getMemberUUID())
                .orElseThrow(EntityNotFoundException::new)
                .getPersonalEvidence();

        boolean match = personalEvidence
                .getAmmoList()
                .stream()
                .filter(Objects::nonNull)
                .anyMatch(e -> e.getCaliberUUID()
                        .equals(ammoUsedpersonal.getCaliberUUID()) &&
                        e.getCaliberName()
                                .equals(ammoUsedpersonal.getCaliberName()));
        if (!match) {
            AmmoUsedEntity ammoUsedEntity = createAmmoUsedEntity(ammoUsedpersonal);
            if (ammoUsedEntity.getCounter() < 0) {
                ammoUsedEntity.setCounter(0);
            }
            ammoUsedRepository.saveAndFlush(ammoUsedEntity);
            personalEvidence.getAmmoList().add(ammoUsedEntity);
            personalEvidence.getAmmoList().sort(Comparator.comparing(AmmoUsedEntity::getCaliberName));
            personalEvidenceRepository.saveAndFlush(personalEvidence);
        } else {
            AmmoUsedEntity ammoUsedEntity = personalEvidence
                    .getAmmoList()
                    .stream()
                    .filter(e -> e.getCaliberUUID().equals(ammoUsedpersonal.getCaliberUUID()))
                    .findFirst()
                    .orElseThrow(EntityNotFoundException::new);

            Integer counter = ammoUsedEntity.getCounter();

            ammoUsedEntity.setCounter(counter + ammoUsedpersonal.getCounter());
            if (ammoUsedEntity.getCounter() < 0) {
                ammoUsedEntity.setCounter(0);
            }

            ammoUsedRepository.saveAndFlush(ammoUsedEntity);
        }

    }

    private boolean starEvidence(AmmoUsedEvidence ammoUsedEvidence) {

        AmmoUsedToEvidenceEntity ammoUsedToEvidenceEntity = createAmmoUsedToEvidenceEntity(ammoUsedEvidence);

        ammoUsedToEvidenceEntityRepository.saveAndFlush(ammoUsedToEvidenceEntity);
        return ammoInEvidenceService.addAmmoUsedEntityToAmmoInEvidenceEntity(ammoUsedToEvidenceEntity);

    }

    private AmmoUsedEntity createAmmoUsedEntity(AmmoUsedPersonal ammoUsedPersonal) {

        return ammoUsedRepository.saveAndFlush(Mapping.map(ammoUsedPersonal));

    }

    private AmmoUsedToEvidenceEntity createAmmoUsedToEvidenceEntity(AmmoUsedEvidence ammoUsedEvidence) {
        return ammoUsedToEvidenceEntityRepository.saveAndFlush(Mapping.map(ammoUsedEvidence));
    }

}

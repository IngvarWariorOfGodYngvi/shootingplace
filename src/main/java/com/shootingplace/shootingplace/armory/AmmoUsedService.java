package com.shootingplace.shootingplace.armory;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.ammoEvidence.*;
import com.shootingplace.shootingplace.member.MemberEntity;
import com.shootingplace.shootingplace.member.MemberRepository;
import com.shootingplace.shootingplace.member.PersonalEvidenceEntity;
import com.shootingplace.shootingplace.member.PersonalEvidenceRepository;
import com.shootingplace.shootingplace.otherPerson.OtherPersonEntity;
import com.shootingplace.shootingplace.otherPerson.OtherPersonRepository;
import com.shootingplace.shootingplace.workingTimeEvidence.WorkingTimeEvidenceRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

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
    private final AmmoInEvidenceRepository ammoInEvidenceRepository;

    private final WorkingTimeEvidenceRepository workingTimeEvidenceRepository;

    private final Logger LOG = LogManager.getLogger();

    public AmmoUsedService(PersonalEvidenceRepository personalEvidenceRepository,
                           AmmoUsedToEvidenceEntityRepository ammoUsedToEvidenceEntityRepository,
                           AmmoInEvidenceService ammoInEvidenceService,
                           AmmoUsedRepository ammoUsedRepository,
                           CaliberRepository caliberRepository,
                           MemberRepository memberRepository, OtherPersonRepository otherPersonRepository, ArmoryService armoryService, AmmoEvidenceRepository ammoEvidenceRepository, AmmoInEvidenceRepository ammoInEvidenceRepository, WorkingTimeEvidenceRepository workingTimeEvidenceRepository) {
        this.personalEvidenceRepository = personalEvidenceRepository;
        this.ammoUsedToEvidenceEntityRepository = ammoUsedToEvidenceEntityRepository;
        this.ammoInEvidenceService = ammoInEvidenceService;
        this.ammoUsedRepository = ammoUsedRepository;
        this.caliberRepository = caliberRepository;
        this.memberRepository = memberRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.armoryService = armoryService;
        this.ammoEvidenceRepository = ammoEvidenceRepository;
        this.ammoInEvidenceRepository = ammoInEvidenceRepository;
        this.workingTimeEvidenceRepository = workingTimeEvidenceRepository;
    }

    @Transactional
    public ResponseEntity<String> addAmmoUsedEntity(String caliberUUID, Integer legitimationNumber, int otherID, Integer quantity) {
        if (!workingTimeEvidenceRepository.existsByIsCloseFalse()) {
            return ResponseEntity.badRequest().body("Najpierw zarejestruj pobyt");
        }

        if (ammoEvidenceRepository.existsByOpenTrueAndForceOpenFalse()) {
            AmmoEvidenceEntity ammoEvidenceEntity = ammoEvidenceRepository.findAllByOpenTrueAndForceOpenFalse().stream().findFirst().orElseThrow(EntityNotFoundException::new);
            if (ammoEvidenceEntity.getDate().isBefore(LocalDate.now())) {
                ammoEvidenceEntity.setOpen(false);
                ammoEvidenceEntity.setForceOpen(false);
                ammoEvidenceRepository.save(ammoEvidenceEntity);
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
                    MemberEntity memberEntity = memberRepository.findByLegitimationNumber(legitimationNumber).orElseThrow(EntityNotFoundException::new);
                    LOG.info("member " + memberEntity.getMemberName());
                    name = memberEntity.getMemberName();
                    AmmoUsedPersonal ammoUsedPersonal = AmmoUsedPersonal.builder()
                            .caliberName(caliberName)
                            .counter(quantity)
                            .memberUUID(memberEntity.getUuid())
                            .caliberUUID(caliberUUID)
                            .memberName(name)
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
                        return ResponseEntity.ok("Dodano do listy " + name + " " + caliberName + " " + quantity);
                    }

                } else {
                    LOG.info("not member");
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
                        return ResponseEntity.ok("Dodano do listy " + name + " " + caliberName + "");
                    }
                }
            }
        } else {
            LOG.info("odejmuję amunicję z listy");

            String name;
            if (legitimationNumber > 0) {
                MemberEntity memberEntity = memberRepository.findByLegitimationNumber(legitimationNumber).orElseThrow(EntityNotFoundException::new);
                name = memberEntity.getMemberName();
                AmmoUsedPersonal ammoUsedPersonal = AmmoUsedPersonal.builder()
                        .caliberName(caliberName)
                        .counter(quantity)
                        .memberUUID(memberEntity.getUuid())
                        .caliberUUID(caliberUUID)
                        .memberName(name)
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
                    return ResponseEntity.ok("Zwrócono do magazynu " + name + " " + caliberName + "");
                }
            } else {
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
                    return ResponseEntity.ok("Zwrócono do magazynu " + name + " " + caliberName);
                }
            }

        }
        return ResponseEntity.badRequest().body("Coś poszło nie tak - Sprawdź stany magazynowe " + caliberName);
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
            ammoUsedRepository.save(ammoUsedEntity);
            personalEvidence.getAmmoList().add(ammoUsedEntity);
            personalEvidence.getAmmoList().sort(Comparator.comparing(AmmoUsedEntity::getCaliberName));
            personalEvidenceRepository.save(personalEvidence);
        } else {
            AmmoUsedEntity ammoUsedEntity = personalEvidence
                    .getAmmoList()
                    .stream()
                    .filter(e -> e.getCaliberUUID().equals(ammoUsedpersonal.getCaliberUUID()))
                    .findFirst()
                    .orElseThrow(EntityNotFoundException::new);

            Integer counter = (ammoUsedEntity.getCounter() != null ? ammoUsedEntity.getCounter() : 0);
            ammoUsedEntity.setCounter(counter + ammoUsedpersonal.getCounter());
            if (ammoUsedEntity.getCounter() < 0) {
                ammoUsedEntity.setCounter(0);
            }

            ammoUsedRepository.save(ammoUsedEntity);
        }

    }

    private boolean starEvidence(AmmoUsedEvidence ammoUsedEvidence) {


        return ammoInEvidenceService.addAmmoUsedEntityToAmmoInEvidenceEntity(ammoUsedToEvidenceEntityRepository.save(createAmmoUsedToEvidenceEntity(ammoUsedEvidence)));

    }

    private AmmoUsedEntity createAmmoUsedEntity(AmmoUsedPersonal ammoUsedPersonal) {

        return ammoUsedRepository.save(Mapping.map(ammoUsedPersonal));

    }

    private AmmoUsedToEvidenceEntity createAmmoUsedToEvidenceEntity(AmmoUsedEvidence ammoUsedEvidence) {
        return ammoUsedToEvidenceEntityRepository.save(Mapping.map(ammoUsedEvidence));
    }

    public void recountAmmo() {
        List<AmmoUsedToEvidenceEntity> all2 = ammoUsedToEvidenceEntityRepository.findAll();
        List<AmmoInEvidenceEntity> ail = ammoInEvidenceRepository.findAll();
        ail.forEach(e -> e.getAmmoUsedToEvidenceEntityList().forEach(all2::remove));
        List<AmmoUsedToEvidenceEntity> all3 = ammoUsedToEvidenceEntityRepository.findAll();
        all3.removeAll(all2);
        Set<String> set1 = new HashSet<>();
        all3.forEach(e -> {
            if (e.getMemberEntity() != null) {
                set1.add(e.getMemberEntity().getUuid());
            }
        });
        set1.forEach(e -> {
            MemberEntity id = memberRepository.findById(e).orElseThrow();
            List<AmmoUsedToEvidenceEntity> collect = all3.stream().filter(f -> f.getMemberEntity() != null).filter(f -> f.getMemberEntity().getUuid().equals(e)).collect(Collectors.toList());
            Map<String, Integer> map =
                    collect
                            .stream()
                            .collect(Collectors.groupingBy(AmmoUsedToEvidenceEntity::getCaliberName, Collectors.summingInt(AmmoUsedToEvidenceEntity::getCounter)));
            id.getPersonalEvidence().getAmmoList().forEach(f -> {
                f.setCounter(map.get(f.getCaliberName()));
                ammoUsedRepository.save(f);
            });
        });
        all2.forEach(ammoUsedToEvidenceEntityRepository::delete);
    }

    public List<AmmoUsedToEvidenceDTO> getPersonalAmmoFromList(String legitimationNumber, String idNumber, String evidenceID) {

        if (idNumber.equals("null")) {
            idNumber = null;
        }
        if (legitimationNumber.equals("null")) {
            legitimationNumber = null;
        }
        AmmoEvidenceEntity one = ammoEvidenceRepository.getOne(evidenceID);
        List<AmmoUsedToEvidenceDTO> collect = new ArrayList<>();
        if (legitimationNumber != null) {

            String finalLegitimationNumber = legitimationNumber;
            one.getAmmoInEvidenceEntityList().stream().map(Mapping::map)
                    .forEach(e -> {
                                List<AmmoUsedToEvidenceDTO> collect1 = e.getAmmoUsedToEvidenceDTOList()

                                        .stream()
                                        .filter(f -> f.getLegitimationNumber() != null)
                                        .filter(f -> f.getLegitimationNumber().equals(Integer.valueOf(finalLegitimationNumber)))
                                        .collect(Collectors.toList());
                                collect.addAll(collect1);
                            }
                    );
        }
        if (idNumber != null) {

            String finalIdNumber = idNumber;
            one.getAmmoInEvidenceEntityList().stream().map(Mapping::map)
                    .forEach(e -> {
                                List<AmmoUsedToEvidenceDTO> collect1 = e.getAmmoUsedToEvidenceDTOList()

                                        .stream()
                                        .filter(f -> f.getIDNumber() != null)
                                        .filter(f -> f.getIDNumber().equals(Integer.valueOf(finalIdNumber)))
                                        .collect(Collectors.toList());
                                collect.addAll(collect1);
                            }
                    );
        }


        return collect;
    }
}

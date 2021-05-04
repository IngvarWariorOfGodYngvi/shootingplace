package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.ClubEntity;
import com.shootingplace.shootingplace.domain.entities.MemberPermissionsEntity;
import com.shootingplace.shootingplace.domain.entities.OtherPersonEntity;
import com.shootingplace.shootingplace.domain.models.MemberPermissions;
import com.shootingplace.shootingplace.domain.models.OtherPerson;
import com.shootingplace.shootingplace.repositories.ClubRepository;
import com.shootingplace.shootingplace.repositories.MemberPermissionsRepository;
import com.shootingplace.shootingplace.repositories.OtherPersonRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OtherPersonService {

    private final ClubRepository clubRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final MemberPermissionsRepository memberPermissionsRepository;
    private final Logger LOG = LogManager.getLogger();


    public OtherPersonService(ClubRepository clubRepository, OtherPersonRepository otherPersonRepository, MemberPermissionsRepository memberPermissionsRepository) {
        this.clubRepository = clubRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.memberPermissionsRepository = memberPermissionsRepository;
    }

    public boolean addPerson(String club, OtherPerson person, MemberPermissions permissions) {

        MemberPermissionsEntity permissionsEntity = Mapping.map(permissions);

        boolean match = clubRepository.findAll().stream().anyMatch(a -> a.getName().equals(club));

        ClubEntity clubEntity;
        if (match) {
            clubEntity = clubRepository
                    .findAll()
                    .stream()
                    .filter(f -> f.getName()
                            .equals(club))
                    .findFirst()
                    .orElseThrow(EntityNotFoundException::new);
        } else {
            List<ClubEntity> all = clubRepository.findAll();
            all.sort(Comparator.comparing(ClubEntity::getId).reversed());
            Integer id = (all.get(0).getId()) + 1;
            clubEntity = ClubEntity.builder()
                    .id(id)
                    .name(club).build();
            clubRepository.saveAndFlush(clubEntity);
        }
        List<OtherPersonEntity> all = otherPersonRepository.findAll();
        int id;
        if (all.isEmpty()) {
            id = 1;
        } else {
            all.sort(Comparator.comparing(OtherPersonEntity::getId).reversed());
            id = (all.get(0).getId()) + 1;
        }
        if (permissions != null) {

            memberPermissionsRepository.saveAndFlush(permissionsEntity);
        }
        OtherPersonEntity otherPersonEntity = OtherPersonEntity.builder()
                .id(id)
                .firstName(person.getFirstName().substring(0, 1).toUpperCase() + person.getFirstName().substring(1).toLowerCase())
                .secondName(person.getSecondName().toUpperCase())
                .phoneNumber(person.getPhoneNumber().trim())
                .active(true)
                .email(person.getEmail())
                .permissionsEntity(permissionsEntity)
                .club(clubEntity)
                .build();
        otherPersonRepository.saveAndFlush(otherPersonEntity);
        return true;

    }

    public List<String> getAllOthers() {

        List<String> list = new ArrayList<>();
        otherPersonRepository.findAll().stream().filter(OtherPersonEntity::isActive)
                .forEach(e -> list.add(e.getSecondName().concat(" " + e.getFirstName() + " Klub: " + e.getClub().getName() + " ID: " + e.getId())));
        list.sort(Comparator.comparing(String::new));
        return list;
    }

    public List<String> getAllOthersArbiters() {

        List<String> list = new ArrayList<>();
        otherPersonRepository.findAll().stream().filter(f -> f.getPermissionsEntity() != null).filter(OtherPersonEntity::isActive)
                .forEach(e -> list.add(e.getSecondName().concat(" " + e.getFirstName() + " Klub " + e.getClub().getName() + " Klasa " + e.getPermissionsEntity().getArbiterClass() + " ID: " + e.getId())));
        list.sort(Comparator.comparing(String::new));
        return list;
    }

    public List<OtherPersonEntity> getAll() {
        LOG.info("Wywołano wszystkich Nie-Klubowiczów");
        return otherPersonRepository.findAll().stream().filter(OtherPersonEntity::isActive).sorted(Comparator.comparing(OtherPersonEntity::getSecondName).thenComparing(OtherPersonEntity::getFirstName)).collect(Collectors.toList());
    }

    public List<OtherPersonEntity> getOthersWithPermissions() {
        List<OtherPersonEntity> list = new ArrayList<>();
        otherPersonRepository.findAll().stream().filter(f -> f.getPermissionsEntity() != null)
                .forEach(list::add);
        return list;
    }

    public boolean deactivatePerson(int id) {
        OtherPersonEntity otherPersonEntity = otherPersonRepository.findById(id).orElseThrow(EntityNotFoundException::new);

        otherPersonEntity.setActive(false);
        otherPersonRepository.saveAndFlush(otherPersonEntity);
        LOG.info("Dezaktywowano Nie-Klubowicza");
        return true;
    }

    public boolean updatePerson(String id, OtherPerson otherPerson) {
        OtherPersonEntity one = otherPersonRepository.getOne(Integer.valueOf(id));
        if (otherPerson.getEmail() != null && !otherPerson.getEmail().isEmpty()) {
            LOG.info("Zmieniono email");
            one.setEmail(otherPerson.getEmail());
        }
        if (otherPerson.getPhoneNumber() != null && !otherPerson.getPhoneNumber().isEmpty()) {
            LOG.info("Zmieniono numer telefonu");
            one.setPhoneNumber(otherPerson.getPhoneNumber());
        }
        if (otherPerson.getFirstName() != null && !otherPerson.getFirstName().isEmpty()) {
            LOG.info("Zmieniono Imię");
            one.setFirstName(otherPerson.getFirstName());
        }
        if (otherPerson.getSecondName() != null && !otherPerson.getSecondName().isEmpty()) {
            LOG.info("Zmieniono nazwisko");
            one.setSecondName(otherPerson.getSecondName());
        }
//        if(!otherPerson.getClub().getName().isEmpty() || !otherPerson.getClub().getName().equals("null")){
//            one.setClub(clubRepository.findAll().stream().filter(f->f.getName().equals(otherPerson.getClub().getName())).findFirst().orElse(null));
//        }
        otherPersonRepository.saveAndFlush(one);
        return true;
    }
}

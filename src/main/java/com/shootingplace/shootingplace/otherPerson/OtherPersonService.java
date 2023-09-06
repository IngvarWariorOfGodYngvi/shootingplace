package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.address.AddressRepository;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.member.MemberPermissions;
import com.shootingplace.shootingplace.member.MemberPermissionsEntity;
import com.shootingplace.shootingplace.member.MemberPermissionsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

@Service
public class OtherPersonService {

    private final ClubRepository clubRepository;
    private final OtherPersonRepository otherPersonRepository;
    private final MemberPermissionsRepository memberPermissionsRepository;
    private final AddressRepository addressRepository;
    private final Logger LOG = LogManager.getLogger();


    public OtherPersonService(ClubRepository clubRepository, OtherPersonRepository otherPersonRepository, MemberPermissionsRepository memberPermissionsRepository, AddressRepository addressRepository) {
        this.clubRepository = clubRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.memberPermissionsRepository = memberPermissionsRepository;
        this.addressRepository = addressRepository;
    }

    public ResponseEntity<?> addPerson(String club, OtherPerson person, MemberPermissions permissions) {

        MemberPermissionsEntity permissionsEntity = null;
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
            clubRepository.save(clubEntity);
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
            permissionsEntity = Mapping.map(permissions);

            memberPermissionsRepository.save(permissionsEntity);
        }
        AddressEntity addressEntity = null;
        if (person.getAddress() != null) {
            addressEntity = addressRepository.save(AddressEntity.builder()
                    .zipCode(person.getAddress().getZipCode())
                    .postOfficeCity(person.getAddress().getPostOfficeCity())
                    .street(person.getAddress().getStreet())
                    .streetNumber(person.getAddress().getStreetNumber())
                    .flatNumber(person.getAddress().getFlatNumber())
                    .build());
        }
        OtherPersonEntity otherPersonEntity = OtherPersonEntity.builder()
                .id(id)
                .firstName(person.getFirstName().substring(0, 1).toUpperCase() + person.getFirstName().substring(1).toLowerCase())
                .secondName(person.getSecondName().toUpperCase())
                .phoneNumber(person.getPhoneNumber().trim().replaceAll(" ", ""))
                .active(true)
                .email(person.getEmail())
                .permissionsEntity(permissionsEntity)
                .weaponPermissionNumber(person.getWeaponPermissionNumber()!=null?person.getWeaponPermissionNumber().toUpperCase(Locale.ROOT):null)
                .club(clubEntity)
                .address(addressEntity)
                .build();
        otherPersonRepository.save(otherPersonEntity);
        LOG.info("Zapisano nową osobę " + otherPersonEntity.getFirstName() + " " + otherPersonEntity.getSecondName());
        return ResponseEntity.status(201).body("Zapisano nową osobę " + otherPersonEntity.getFirstName() + " " + otherPersonEntity.getSecondName());

    }

    public OtherPersonEntity addPerson(String club, OtherPerson person) {

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
            clubRepository.save(clubEntity);
        }
        AddressEntity addressEntity = addressRepository.save(AddressEntity.builder()
                .zipCode(person.getAddress().getZipCode())
                .postOfficeCity(person.getAddress().getPostOfficeCity())
                .street(person.getAddress().getStreet())
                .streetNumber(person.getAddress().getStreetNumber())
                .flatNumber(person.getAddress().getFlatNumber())
                .build());
        List<OtherPersonEntity> all = otherPersonRepository.findAll();
        int id;
        if (all.isEmpty()) {
            id = 1;
        } else {
            all.sort(Comparator.comparing(OtherPersonEntity::getId).reversed());
            id = (all.get(0).getId()) + 1;
        }
        OtherPersonEntity otherPersonEntity = OtherPersonEntity.builder()
                .id(id)
                .firstName(person.getFirstName().substring(0, 1).toUpperCase() + person.getFirstName().substring(1).toLowerCase())
                .secondName(person.getSecondName().toUpperCase())
                .phoneNumber(person.getPhoneNumber().replaceAll(" ", "").trim())
                .active(true)
                .email(person.getEmail())
                .permissionsEntity(null)
                .weaponPermissionNumber(person.getWeaponPermissionNumber()!=null?person.getWeaponPermissionNumber().toUpperCase(Locale.ROOT):null)
                .address(addressEntity)
                .club(clubEntity)
                .build();
        LOG.info("Zapisano nową osobę " + otherPersonEntity.getFirstName() + " " + otherPersonEntity.getSecondName());
        return otherPersonRepository.save(otherPersonEntity);

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

    public List<?> getAll() {
        LOG.info("Wywołano wszystkich Nie-Klubowiczów");
        return otherPersonRepository.findAll().stream().filter(OtherPersonEntity::isActive).sorted(Comparator.comparing(OtherPersonEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl"))).thenComparing(OtherPersonEntity::getFirstName)).collect(Collectors.toList());
    }

    public List<OtherPersonEntity> getOthersWithPermissions() {
        List<OtherPersonEntity> list = new ArrayList<>();
        otherPersonRepository.findAll().stream().filter(f -> f.getPermissionsEntity() != null)
                .forEach(list::add);
        return list;
    }

    public ResponseEntity<?> deactivatePerson(int id) {
        if (!otherPersonRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Nie znaleziono osoby");
        }
        OtherPersonEntity otherPersonEntity = otherPersonRepository.getOne(id);

        otherPersonEntity.setActive(false);
        otherPersonRepository.save(otherPersonEntity);
        LOG.info("Dezaktywowano Nie-Klubowicza");
        return ResponseEntity.ok("Dezaktywowano Osobę");
    }

    public ResponseEntity<?> updatePerson(String id, OtherPerson otherPerson, String clubName) {
        OtherPersonEntity one = otherPersonRepository.getOne(Integer.valueOf(id));
        if (otherPerson.getEmail() != null && !otherPerson.getEmail().isEmpty()) {
            LOG.info("Zmieniono email");
            one.setEmail(otherPerson.getEmail());
        }
        if (otherPerson.getPhoneNumber() != null && !otherPerson.getPhoneNumber().isEmpty()) {
            LOG.info("Zmieniono numer telefonu");
            one.setPhoneNumber(otherPerson.getPhoneNumber().replaceAll(" ", ""));
        }
        if (otherPerson.getFirstName() != null && !otherPerson.getFirstName().isEmpty()) {
            LOG.info("Zmieniono Imię");
            one.setFirstName(otherPerson.getFirstName());
        }
        if (otherPerson.getSecondName() != null && !otherPerson.getSecondName().isEmpty()) {
            LOG.info("Zmieniono nazwisko");
            one.setSecondName(otherPerson.getSecondName());
        }
        if (otherPerson.getWeaponPermissionNumber() != null && !otherPerson.getWeaponPermissionNumber().isEmpty()) {
            LOG.info("Zmieniono numer Pozwolenia na broń");
            one.setWeaponPermissionNumber(otherPerson.getWeaponPermissionNumber());
        }
        if (otherPerson.getAddress() != null) {
            AddressEntity address;
            if (one.getAddress() == null) {
                address = AddressEntity.builder()
                        .postOfficeCity(otherPerson.getAddress().getPostOfficeCity())
                        .zipCode(otherPerson.getAddress().getZipCode())
                        .street(otherPerson.getAddress().getStreet())
                        .streetNumber(otherPerson.getAddress().getStreetNumber())
                        .flatNumber(otherPerson.getAddress().getFlatNumber())
                        .build();
                one.setAddress(address);
            } else {
                address = one.getAddress();
                if (otherPerson.getAddress().getZipCode() != null && !otherPerson.getAddress().getZipCode().isEmpty()) {
                    address.setZipCode(otherPerson.getAddress().getZipCode());
                }
                if (otherPerson.getAddress().getZipCode() != null && !otherPerson.getAddress().getPostOfficeCity().isEmpty()) {
                    address.setPostOfficeCity(otherPerson.getAddress().getPostOfficeCity());
                }
                if (otherPerson.getAddress().getStreet() != null && !otherPerson.getAddress().getStreet().isEmpty()) {
                    address.setStreet(otherPerson.getAddress().getStreet());
                }
                if (otherPerson.getAddress().getStreetNumber() != null && !otherPerson.getAddress().getStreetNumber().isEmpty()) {
                    address.setStreetNumber(otherPerson.getAddress().getStreetNumber());
                }
                if (otherPerson.getAddress().getFlatNumber() != null && !otherPerson.getAddress().getFlatNumber().isEmpty()) {
                    address.setFlatNumber(otherPerson.getAddress().getFlatNumber());
                }
            }
            LOG.info("Zmieniono adres");
            addressRepository.save(address);
        }
        if (!one.getClub().getName().equals(clubName)) {
            ClubEntity clubEntity = clubRepository.findAll().stream().filter(f -> f.getName().equals(clubName)).findFirst().orElse(null);
            if (clubEntity == null) {
                {
                    List<ClubEntity> all = clubRepository.findAll();
                    all.sort(Comparator.comparing(ClubEntity::getId).reversed());
                    Integer clubID = (all.get(0).getId()) + 1;
                    clubEntity = ClubEntity.builder()
                            .id(clubID)
                            .name(clubName).build();
                    clubRepository.save(clubEntity);
                }
            }
            LOG.info("Zmieniono Klub");
            one.setClub(clubEntity);
        }
        otherPersonRepository.save(one);
        return ResponseEntity.ok("Zaktualizowano");
    }

    public ResponseEntity<?> getOhterByPhone(String phone) {
        OtherPersonEntity otherPerson = otherPersonRepository.findByPhoneNumber(phone.replaceAll(" ", "")).orElse(null);
        if (otherPerson != null && otherPerson.isActive()) {
            return ResponseEntity.ok(otherPerson);
        } else {
            return ResponseEntity.badRequest().body("brak takiego numeru w bazie");
        }
    }
}

package com.shootingplace.shootingplace.otherPerson;

import com.shootingplace.shootingplace.Mapping;
import com.shootingplace.shootingplace.address.Address;
import com.shootingplace.shootingplace.address.AddressEntity;
import com.shootingplace.shootingplace.address.AddressRepository;
import com.shootingplace.shootingplace.club.ClubEntity;
import com.shootingplace.shootingplace.club.ClubRepository;
import com.shootingplace.shootingplace.exceptions.NoUserPermissionException;
import com.shootingplace.shootingplace.history.HistoryService;
import com.shootingplace.shootingplace.member.MemberInfo;
import com.shootingplace.shootingplace.member.MemberPermissions;
import com.shootingplace.shootingplace.member.MemberPermissionsEntity;
import com.shootingplace.shootingplace.member.MemberPermissionsRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.text.Collator;
import java.time.LocalDateTime;
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
    private final HistoryService historyService;
    private final Logger LOG = LogManager.getLogger();


    public OtherPersonService(ClubRepository clubRepository, OtherPersonRepository otherPersonRepository, MemberPermissionsRepository memberPermissionsRepository, AddressRepository addressRepository, HistoryService historyService) {
        this.clubRepository = clubRepository;
        this.otherPersonRepository = otherPersonRepository;
        this.memberPermissionsRepository = memberPermissionsRepository;
        this.addressRepository = addressRepository;
        this.historyService = historyService;
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
                .weaponPermissionNumber(person.getWeaponPermissionNumber() != null ? person.getWeaponPermissionNumber().toUpperCase(Locale.ROOT) : null)
                .club(clubEntity)
                .address(addressEntity)
                .build();
        otherPersonEntity.setCreationDate();
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
                .weaponPermissionNumber(person.getWeaponPermissionNumber() != null ? person.getWeaponPermissionNumber().toUpperCase(Locale.ROOT) : null)
                .address(addressEntity)
                .creationDate(LocalDateTime.now())
                .club(clubEntity)
                .build();
        otherPersonEntity.setCreationDate();
        LOG.info("Zapisano nową osobę " + otherPersonEntity.getFirstName() + " " + otherPersonEntity.getSecondName());
        return otherPersonRepository.save(otherPersonEntity);

    }

    public List<String> getAllOthers() {

        List<String> list = new ArrayList<>();
        otherPersonRepository.findAllByActiveTrue()
                .forEach(e -> list.add(e.getSecondName().concat(" " + e.getFirstName() + " Klub: " + e.getClub().getName() + " ID: " + e.getId())));
        list.sort(Comparator.comparing(String::new));
        return list;
    }

    public List<MemberInfo> getAllOthersArbiters() {
        return otherPersonRepository.findAll()
                .stream()
                .filter(f -> f.getPermissionsEntity() != null && f.getPermissionsEntity().getArbiterNumber() != null)
                .map(m -> MemberInfo.builder()
                        .id(m.getId())
                        .arbiterClass(m.getPermissionsEntity().getArbiterClass())
                        .firstName(m.getFirstName())
                        .secondName(m.getSecondName())
                        .name(m.getFullName())
                        .build()
                ).collect(Collectors.toList());
    }

    public List<?> getAll() {
        return otherPersonRepository.findAll()
                .stream()
                .filter(OtherPersonEntity::isActive)
                .sorted(Comparator.comparing(OtherPersonEntity::getSecondName, Collator.getInstance(Locale.forLanguageTag("pl")))
                        .thenComparing(OtherPersonEntity::getFirstName))
                .collect(Collectors.toList());
    }

    public List<OtherPersonEntity> getOthersWithPermissions() {
        List<OtherPersonEntity> list = new ArrayList<>();
        otherPersonRepository.findAll().stream().filter(f -> f.getPermissionsEntity() != null)
                .forEach(list::add);
        return list;
    }

    public ResponseEntity<?> deactivatePerson(int id, String pinCode) throws NoUserPermissionException {
        if (!otherPersonRepository.existsById(id)) {
            return ResponseEntity.badRequest().body("Nie znaleziono osoby");
        }
        OtherPersonEntity otherPersonEntity = otherPersonRepository.getOne(id);

        otherPersonEntity.setActive(false);
        ResponseEntity<?> response = historyService.getStringResponseEntity(pinCode, otherPersonEntity, HttpStatus.OK, "Usunięta Osoba spoza Klubu " + otherPersonEntity.getFullName(), "Usunięcie osoby spoza Klubu");
        if (response.getStatusCode().equals(HttpStatus.OK)) {
            otherPersonRepository.save(otherPersonEntity);
            LOG.info("Dezaktywowano Nie-Klubowicza");
            return ResponseEntity.ok("Usunięto Osobę");
        }
        return response;
    }

    public ResponseEntity<?> updatePerson(String id, OtherPerson oP, String clubName) {
        OtherPersonEntity one = otherPersonRepository.getOne(Integer.valueOf(id));
        if ((oP.getEmail() != null && !oP.getEmail().isEmpty()) && !one.getEmail().equals(oP.getEmail())) {
            LOG.info("Zmieniono email");
            one.setEmail(oP.getEmail());
        }
        if ((oP.getPhoneNumber() != null && !oP.getPhoneNumber().isEmpty()) && !one.getPhoneNumber().equals(oP.getPhoneNumber())) {
            LOG.info("Zmieniono numer telefonu");
            one.setPhoneNumber(oP.getPhoneNumber().replaceAll(" ", ""));
        }
        if ((oP.getFirstName() != null && !oP.getFirstName().isEmpty()) && !(one.getFirstName().substring(0, 1).toUpperCase() + one.getFirstName().substring(1).toLowerCase()).equals(oP.getFirstName())) {
            LOG.info("Zmieniono Imię");
            one.setFirstName(oP.getFirstName());
        }
        if ((oP.getSecondName() != null && !oP.getSecondName().isEmpty()) && !one.getSecondName().toUpperCase().equals(oP.getSecondName())) {
            LOG.info("Zmieniono nazwisko");
            one.setSecondName(oP.getSecondName());
        }
        if ((oP.getWeaponPermissionNumber() != null && !oP.getWeaponPermissionNumber().isEmpty()) && !one.getWeaponPermissionNumber().equals(oP.getWeaponPermissionNumber())) {
            LOG.info("Zmieniono numer Pozwolenia na broń");
            one.setWeaponPermissionNumber(oP.getWeaponPermissionNumber());
        }
        Address a1 = oP.getAddress();
        AddressEntity a2 = one.getAddress() != null ? one.getAddress() : new AddressEntity();

        a2.setPostOfficeCity(a1.getPostOfficeCity() != null ? a1.getPostOfficeCity() : a2.getPostOfficeCity());
        a2.setZipCode(a1.getZipCode() != null ? a1.getZipCode() : a2.getZipCode());
        a2.setStreet(a1.getStreet() != null ? a1.getStreet() : a2.getStreet());
        a2.setStreetNumber(a1.getStreetNumber() != null ? a1.getStreetNumber() : a2.getStreetNumber());
        a2.setFlatNumber(a1.getFlatNumber() != null ? a1.getFlatNumber() : a2.getFlatNumber());
        AddressEntity save1 = addressRepository.save(a2);
        one.setAddress(save1);
        if ((clubName!=null && !clubName.isEmpty())&&!one.getClub().getName().equals(clubName)) {
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
        MemberPermissions m1 = oP.getMemberPermissions();
        MemberPermissionsEntity m2 = one.getPermissionsEntity() != null ? one.getPermissionsEntity() : new MemberPermissionsEntity();

        m2.setArbiterClass(m1.getArbiterClass() != null ? m1.getArbiterClass() : m2.getArbiterClass() != null ? m2.getArbiterClass() : null);
        m2.setArbiterNumber(m1.getArbiterNumber() != null ? m1.getArbiterNumber() : m2.getArbiterNumber() != null ? m2.getArbiterNumber() : null);
        m2.setArbiterPermissionValidThru(m1.getArbiterPermissionValidThru() != null ? m1.getArbiterPermissionValidThru() : m2.getArbiterPermissionValidThru() != null ? m2.getArbiterPermissionValidThru() : null);
        MemberPermissionsEntity save2 = memberPermissionsRepository.save(m2);
        one.setPermissionsEntity(save2);
        otherPersonRepository.save(one);
        return ResponseEntity.ok("Zaktualizowano");
    }

    public ResponseEntity<?> getOtherByPhone(String phone) {
        OtherPersonEntity otherPerson = otherPersonRepository.findAllByPhoneNumber(phone.replaceAll(" ", "")).stream().filter(OtherPersonEntity::isActive).findFirst().orElse(null);
        if (otherPerson != null && otherPerson.isActive()) {
            return ResponseEntity.ok(otherPerson);
        } else {
            return ResponseEntity.badRequest().body("brak takiego numeru w bazie");
        }
    }
}

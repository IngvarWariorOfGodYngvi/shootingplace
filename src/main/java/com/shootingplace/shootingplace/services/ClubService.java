package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.ClubEntity;
import com.shootingplace.shootingplace.domain.models.Club;
import com.shootingplace.shootingplace.repositories.ClubRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
public class ClubService {

    private final ClubRepository clubRepository;
    private final Logger LOG = LogManager.getLogger();

    public ClubService(ClubRepository clubRepository) {
        this.clubRepository = clubRepository;
    }

    public List<ClubEntity> getAllClubs() {
        List<ClubEntity> all = clubRepository.findAll();
        all.sort(Comparator.comparing(ClubEntity::getName));
        return all;
    }

    public List<String> getAllClubsToTournament() {
        List<String> list = new ArrayList<>();
        clubRepository.findAll().stream()
                .filter(f -> f.getId() != 1 && f.getId() != 2)
                .forEach(e -> list.add(e.getName()));
        list.sort(String::compareTo);
        return list;
    }

    public boolean updateClub(int id, Club club) {
        if (!clubRepository.existsById(id)) {
            return false;
        }
        if (id == 2) {
            LOG.info("Forbidden");
            return false;
        }
        ClubEntity clubEntity = clubRepository.findById(id)
                .orElseThrow(EntityNotFoundException::new);
        if (club.getName() != null && !club.getName().isEmpty()) {
            String[] s = club.getName().split(" ");
            String s1 = s[0].toUpperCase();
            StringBuilder name = new StringBuilder();
            for (int i = 1; i < s.length; i++) {
                String splinted = s[i].substring(0, 1).toUpperCase() + s[i].substring(1).toLowerCase() + " ";
                name.append(splinted);
            }
            clubEntity.setName(s1 + " " + name);
        }
        if (club.getFullName() != null && !club.getFullName().isEmpty()) {
            clubEntity.setFullName(club.getFullName().toUpperCase());
        }
        if (club.getAddress() != null && !club.getAddress().isEmpty()) {
            clubEntity.setAddress(club.getAddress());
        }
        if (club.getEmail() != null && !club.getEmail().isEmpty()) {
            clubEntity.setEmail(club.getEmail());
        }
        if (club.getPhoneNumber() != null && !club.getPhoneNumber().isEmpty()) {
            clubEntity.setPhoneNumber("+48 " + club.getPhoneNumber());
        }
        if (club.getUrl() != null && !club.getUrl().isEmpty()) {
            clubEntity.setUrl(club.getUrl());
        }
        if (id == 1) {
            if (club.getLicenseNumber() != null && !club.getLicenseNumber().isEmpty()) {
                clubEntity.setLicenseNumber(club.getLicenseNumber());
            }
        }
        clubRepository.saveAndFlush(clubEntity);
        return true;
    }

    public boolean createMotherClub(Club club) {
        if (clubRepository.findById(1).isPresent()) {
            return false;
        }
        if (club.getName().isEmpty() ||
                club.getFullName().isEmpty() ||
                club.getAddress().isEmpty() ||
                club.getEmail().isEmpty() ||
                club.getLicenseNumber().isEmpty()
        ) {
            return false;
        }
        clubRepository.saveAndFlush(ClubEntity.builder()
                .id(1)
                .name(club.getName())
                .fullName(club.getFullName())
                .phoneNumber("+48 " + club.getPhoneNumber())
                .email(club.getEmail())
                .address(club.getAddress())
                .licenseNumber(club.getLicenseNumber())
                .url(club.getUrl())
                .build());
        return true;
    }

    public ResponseEntity<?> createNewClub(Club club) {

        clubRepository.saveAndFlush(ClubEntity.builder()
                .id(1)
                .name(club.getName())
                .fullName(club.getFullName())
                .phoneNumber("+48 " + club.getPhoneNumber())
                .email(club.getEmail())
                .address(club.getAddress())
                .licenseNumber(club.getLicenseNumber())
                .url(club.getUrl())
                .build());
        return ResponseEntity.ok("\"Utworzono nowy Klub\"");
    }
}

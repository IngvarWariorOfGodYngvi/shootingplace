package com.shootingplace.shootingplace.services;

import com.shootingplace.shootingplace.domain.entities.MemberEntity;
import com.shootingplace.shootingplace.domain.entities.ShootingPatentEntity;
import com.shootingplace.shootingplace.domain.models.ShootingPatent;
import com.shootingplace.shootingplace.repositories.MemberRepository;
import com.shootingplace.shootingplace.repositories.ShootingPatentRepository;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import javax.persistence.EntityNotFoundException;

@Service
public class ShootingPatentService {

    private final ShootingPatentRepository shootingPatentRepository;
    private final MemberRepository memberRepository;
    private final HistoryService historyService;
    private final Logger LOG = LogManager.getLogger(getClass());


    public ShootingPatentService(ShootingPatentRepository shootingPatentRepository,
                                 MemberRepository memberRepository, HistoryService historyService) {
        this.shootingPatentRepository = shootingPatentRepository;
        this.memberRepository = memberRepository;
        this.historyService = historyService;
    }

    public ResponseEntity<?> updatePatent(String memberUUID, ShootingPatent shootingPatent) {

        if (!memberRepository.existsById(memberUUID)) {
            return ResponseEntity.badRequest().body("\"Nie znaleziono klubowicza\"");
        }
        MemberEntity memberEntity = memberRepository.findById(memberUUID).orElseThrow(EntityNotFoundException::new);
        ShootingPatentEntity shootingPatentEntity = memberEntity.getShootingPatent();

        if (shootingPatent.getPatentNumber() != null && !shootingPatent.getPatentNumber().isEmpty()) {

            boolean match = memberRepository.findAll()
                    .stream()
                    .filter(f -> !f.getErased())
                    .filter(f -> f.getShootingPatent().getPatentNumber() != null)
                    .anyMatch(f -> f.getShootingPatent().getPatentNumber().equals(shootingPatent.getPatentNumber()));

            if (match && !shootingPatentEntity.getPatentNumber().equals(shootingPatent.getPatentNumber())) {
                LOG.error("ktoś już ma taki numer patentu");
                return ResponseEntity.badRequest().body("\"ktoś już ma taki numer patentu\"");
            } else {
                shootingPatentEntity.setPatentNumber(shootingPatent.getPatentNumber());
                LOG.info("Wprowadzono numer patentu");
            }
        }
        if (shootingPatent.getPistolPermission() != null) {
            if (shootingPatent.getPistolPermission().equals(true)) {
                shootingPatentEntity.setPistolPermission(shootingPatent.getPistolPermission());
                LOG.info("Dodano dyscyplinę : Pistolet");
            }
        }
        if (shootingPatent.getRiflePermission() != null) {
            if (shootingPatent.getRiflePermission().equals(true)) {
                shootingPatentEntity.setRiflePermission(shootingPatent.getRiflePermission());
                LOG.info("Dodano dyscyplinę : Karabin");
            }
        }
        if (shootingPatent.getShotgunPermission() != null) {
            if (shootingPatent.getShotgunPermission().equals(true)) {
                shootingPatentEntity.setShotgunPermission(shootingPatent.getShotgunPermission());
                LOG.info("Dodano dyscyplinę : Strzelba");
            }
        }
        if (shootingPatent.getDateOfPosting() != null) {
            LOG.info("ustawiono datę przyznania patentu na : " + shootingPatent.getDateOfPosting());
            shootingPatentEntity.setDateOfPosting(shootingPatent.getDateOfPosting());

        }
        shootingPatentRepository.saveAndFlush(shootingPatentEntity);
        LOG.info("Zaktualizowano patent");
        historyService.updateShootingPatentHistory(memberUUID, shootingPatent);
        return ResponseEntity.ok("\"Zaktualizowano patent" + memberEntity.getSecondName() + " " + memberEntity.getFirstName() + "\"");
    }

    public ShootingPatent getShootingPatent() {
        return ShootingPatent.builder()
                .patentNumber(null)
                .dateOfPosting(null)
                .pistolPermission(false)
                .riflePermission(false)
                .shotgunPermission(false)
                .build();
    }
}